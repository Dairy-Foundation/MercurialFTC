package org.firstinspires.ftc.teamcode.examplerobot;

import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.expression;

import androidx.annotation.NonNull;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.localization.MotionState;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.PathTracker;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.examplerobot.pedro.Constants;

import dev.frozenmilk.dairy.mercurial.continuations.Continuation;
import dev.frozenmilk.dairy.mercurial.continuations.Continuations;
import dev.frozenmilk.dairy.mercurial.environments.Reference;
import dev.frozenmilk.dairy.mercurial.ftc.Context;
import dev.frozenmilk.dairy.mercurial.pedro.MercurialFollower;
import dev.frozenmilk.dairy.mercurial.processes.Channel;
import dev.frozenmilk.dairy.mercurial.processes.SwitchedStateMachine;
import kotlin.time.TimeMark;

public class Drivetrain extends SwitchedStateMachine {
    private final MercurialFollower follower;
    // localization updates
    private final Channel<MotionState> motionStates = linkedSingleChannel();
    // driver inputs
    private final Channel<DrivePowers> drivePowers = linkedSingleChannel();
    // constant modes
    private final Mode idle;
    private final Mode fieldCentric;
    
    private final Telemetry.Item stateTelemetry;
    
    public Drivetrain(Context ctx) {
        // create the follower helper, which has a pedro drivetrain and algorithm
        follower = Constants.createFollower(ctx.hardwareMap());
        
        // Mercurial does not use telemetry the same way the SDK does
        // instead, you retain the entry, and update the value over time
        stateTelemetry = ctx.telemetry().addData("Drivetrain/State", "unknown");
        
        // the idle mode just stops the robot when you enter it
        idle = entranceMode(from -> {
            follower.idle();
            stateTelemetry.setValue("idle");
        });
        
        // field centric uses the most recent motion state data to drive the robot
        fieldCentric = mode(self -> {
            stateTelemetry.setValue("field centric");
            // consume the next inputs from the driver
            DrivePowers input = drivePowers.poll();
            // we only query motion states, which doesn't remove the data
            MotionState motionState = motionStates.query();
            
            if (input != null && motionState != null) {
                // calculate the field centric inputs
                DrivePowers fieldCentricInput = ManualDrive.fieldCentric(
                    input,
                    motionState.pose().heading()
                );
                // drive!
                follower.manual(fieldCentricInput);
            }
            
            return self;
        });
    }
    
    // init into idle
    
    @NonNull
    @Override
    protected Mode init() {
        return idle;
    }
    
    // public api
    
    public void sendPowers(DrivePowers powers) {
        drivePowers.send(powers);
    }
    
    public void sendPowers(double forward, double strafe, double turn) {
        drivePowers.send(new DrivePowers(forward, strafe, turn));
    }
    
    public void sendMotionState(MotionState motionState) {
        motionStates.send(motionState);
    }
    
    public void fieldCentric() {
        switchTo(fieldCentric);
    }
    
    public void idle() {
        switchTo(idle);
    }
    
    public void holdPose(Pose pose) {
        switchTo(holdPoseMode(pose));
    }
    
    public void followPathAsync(Path path, boolean holdEnd, Channel<FollowResult> resultChannel) {
        switchTo(followPathMode(holdEnd, path, resultChannel));
    }
    
    public void followPathAsync(Path path, boolean holdEnd) {
        followPathAsync(path, holdEnd, null);
    }
    
    public Continuation.Builder<FollowResult> followPathSync(Path path, boolean holdEnd) {
        return expression((scope, self) -> {
            // local channel variable
            Reference.O<Channel<FollowResult>> channel = scope.o(Channel::single);
            // run async
            scope.runExec(() -> followPathAsync(path, holdEnd, channel.get()));
            
            // wait for a response
            return Continuations.<FollowResult>receive().receive(channel);
        });
    }
    
    // parametric modes
    
    private Mode holdPoseMode(TimeMark timeMark, Pose pose) {
        return mode(self -> {
            stateTelemetry.setValue("holding pose");
            // we poll the motion state,
            // so that we only update when a new motion state arrives
            MotionState motionState = motionStates.poll();
            
            // if there is a new motion state
            if (motionState != null) {
                // we calculate an update
                TimeMark nextTimeMark = follower.hold(
                    pose,
                    true,
                    motionState,
                    timeMark
                );
                return holdPoseMode(nextTimeMark, pose);
            }
            
            return self;
        });
    }
    
    private Mode holdPoseMode(Pose pose) {
        return holdPoseMode(null, pose);
    }
    
    private Mode followPathMode(
        TimeMark timeMark,
        boolean holdEnd,
        PathTracker tracker,
        Channel<FollowResult> resultChannel
    ) {
        return egressMode(
            self -> {
                stateTelemetry.setValue("following path");
                // we poll the motion state,
                // so that we only update when a new motion state arrives
                MotionState motionState = motionStates.poll();
                if (motionState != null) {
                    TimeMark nextTimeMark = follower.follow(
                        tracker,
                        motionState,
                        timeMark
                    );
                    // follower.follow will return null when its finished
                    if (nextTimeMark == null) {
                        // tell the awaiting process to move on!
                        if (resultChannel != null) resultChannel.sendIfEmpty(FollowResult.Ok);
                        // if hold end, move to holding the end
                        if (holdEnd) return holdPoseMode(tracker.endPose());
                            // or idle
                        else return idle;
                    }
                    // if we're not finished, move to the next state
                    else return followPathMode(
                        nextTimeMark,
                        holdEnd,
                        tracker,
                        resultChannel
                    );
                }
                return self;
            },
            toMode -> {
                // this function gets run when we exit following due to a switch
                if (resultChannel != null) resultChannel.sendIfEmpty(FollowResult.Cancelled);
                // make sure to release the tracker
                tracker.release();
            }
        );
    }
    
    private Mode followPathMode(
        boolean holdEnd,
        Path path,
        Channel<FollowResult> resultChannel
    ) {
        return followPathMode(null, holdEnd, new PathTracker(path), resultChannel);
    }
    
    // you might want to add more results, like timeouts
    public enum FollowResult {
        Ok,
        Cancelled,
    }
}
