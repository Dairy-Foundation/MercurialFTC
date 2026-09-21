package org.firstinspires.ftc.teamcode.examplerobot;

import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.*;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.localization.MotionState;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.PathTracker;

import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import dev.frozenmilk.dairy.mercurial.continuations.Continuation;
import dev.frozenmilk.dairy.mercurial.continuations.Continuation.Builder;
import dev.frozenmilk.dairy.mercurial.continuations.Continuations;
import dev.frozenmilk.dairy.mercurial.continuations.Expression;
import dev.frozenmilk.dairy.mercurial.environments.Reference;
import dev.frozenmilk.dairy.mercurial.pedro.MercurialFollower;
import dev.frozenmilk.dairy.mercurial.processes.Channel;
import dev.frozenmilk.dairy.mercurial.processes.EventManager;
import dev.frozenmilk.dairy.mercurial.processes.EventManager.EventHandled;
import dev.frozenmilk.dairy.mercurial.processes.StateMachine;
import kotlin.time.TimeMark;

public class Drivetrain extends StateMachine {
    private final MercurialFollower pedro;

    public Drivetrain(MercurialFollower pedro) {
        this.pedro = pedro;
        // we tell the localiser to send events to us
        pedro.localizationEventManager().addHandler(motionStateForwarder);
    }

    // we need some channels for communicating with the drivetrain
    // drive powers are manual drive inputs
    private final Channel<DrivePowers> drivePowers = Channel.single();
    // and motion states are updates from the localiser
    private final Channel<MotionState> motionStates = Channel.single();
    // we add our own mode switching system, which will come in over this channel
    // we will just send the mode directly to the drivetrain when we want it to switch
    private final Channel<Mode> switches = Channel.queue();

    // lets set up some modes, this tutorial will just add the 4 pedro modes
    private final Mode manual = new Mode() {
        @Override
        @NotNull
        public Mode eval() {
            // if there are drive powers, we use the pedro helper to drive the robot
            DrivePowers powers = drivePowers.poll();
            if (powers != null) pedro.manual(powers);

            // either switch to a new mode, or continue doing this
            Mode nextMode = switches.poll();
            if (nextMode == null) return this;
            else return nextMode;
        }
    };

    private final Mode idle = new Mode() {
        // when we enter idle, we want to stop the drivetrain
        // this helper will do so for us
        @Override
        public void enter(@NotNull Mode previousMode) {
            pedro.idle();
        }

        @Override
        @NotNull
        public Mode eval() {
            // just see if there is a mode switch request
            Mode nextMode = switches.poll();
            if (nextMode == null) return this;
            else return nextMode;
        }
    };

    // unlike manual and idle, hold and follow have some extra data
    // so we will make them inner classes instead

    private class Hold implements Mode {
        // we need the pose to hold at
        public final Pose pose;
        // if to use scaling
        public final boolean useScaling;
        // the timeMark is to tell us how long has passed since last update
        // you don't need to worry about it much
        public final TimeMark timeMark;

        private Hold(Pose pose, boolean useScaling, TimeMark timeMark) {
            this.pose = pose;
            this.useScaling = useScaling;
            this.timeMark = timeMark;
        }

        public Hold(Pose pose, boolean useScaling) {
            this.pose = pose;
            this.useScaling = useScaling;
            this.timeMark = null;
        }

        @Override
        @NotNull
        public Mode eval() {
            // but hold uses localisation updates
            MotionState motionState = motionStates.poll();
            if (motionState != null) {
                // the helper for holding is a bit more complex
                // it returns the next timeMark for use to use
                TimeMark nextTimeMark = pedro.hold(
                    pose,
                    useScaling,
                    motionState,
                    timeMark
                );

                // so we need to go into a new version of hold
                Mode nextMode = switches.poll();
                if (nextMode == null) return new Hold(pose, useScaling, nextTimeMark);
                else return nextMode;
            }
            // we use the same switching logic
            Mode nextMode = switches.poll();
            if (nextMode == null) return this;
            else return nextMode;
        }
    }


    public enum FollowResult {
        Finished,
        Cancelled,
    }

    // follow is the most complex
    private class Follow implements Mode {
        // when a different bit of code asks us to follow a path
        // it may want to know when we are finished
        // so it can give us a response channel
        // this is optional
        public final Channel<FollowResult> respondTo;
        // the path tracker is part of pedro
        public final PathTracker pathTracker;
        // if to hold the end position afterward
        public final boolean holdEnd;
        // the same time mark as hold
        public final TimeMark timeMark;

        private Follow(
            Channel<FollowResult> respondTo,
            PathTracker pathTracker,
            boolean holdEnd,
            TimeMark timeMark
        ) {
            this.respondTo = respondTo;
            this.pathTracker = pathTracker;
            this.holdEnd = holdEnd;
            this.timeMark = timeMark;
        }

        // this constructor converts paths to path trackers
        public Follow(
            Channel<FollowResult> respondTo,
            Path path,
            boolean holdEnd
        ) {
            this(respondTo, new PathTracker(path), holdEnd, null);
        }

        @Override
        @NotNull
        public Mode eval() {
            // the follow helper is the most complex
            MotionState motionState = motionStates.poll();
            if (motionState != null) {
                TimeMark nextTimeMark = pedro.follow(
                    pathTracker,
                    motionState,
                    timeMark
                );
                // the helper will return null if the tracker is finished
                if (nextTimeMark == null) {
                    // let the caller know that we have finished successfully
                    if (respondTo != null) respondTo.send(FollowResult.Finished);

                    Mode nextMode = switches.poll();
                    if (nextMode != null) return nextMode;
                        // if hold end is true, we need to switch to hold
                    else if (holdEnd) return new Hold(pathTracker.endPose(), true);
                        // otherwise we idle
                    else return idle;
                }
                // if the tracker didn't finish, then we keep going
                else {
                    Mode nextMode = switches.poll();
                    if (nextMode != null) {
                        // release the path tracker
                        pathTracker.release();
                        // let the caller know that we didn't complete following the path
                        if (respondTo != null) respondTo.send(FollowResult.Cancelled);
                        // return the new mode
                        return nextMode;
                    }
                    return new Follow(respondTo, pathTracker, holdEnd, nextTimeMark);
                }
            }
            Mode nextMode = switches.poll();
            if (nextMode != null) {
                // release the path tracker
                pathTracker.release();
                // let the caller know that we didn't complete following the path
                if (respondTo != null) respondTo.send(FollowResult.Cancelled);
                // return the new mode
                return nextMode;
            }
            return this;
        }
    }

    // this event handler will forward motion state events to the drivetrain
    private EventManager.Handler<MotionState> motionStateForwarder = motionState -> {
        motionStates.send(motionState);
        // if the drivetrain finishes, remove this handler
        if (fiber().status().alive()) return EventHandled.ok();
        else return EventHandled.remove();
    };

    @Override
    @NotNull
    protected Mode init() {
        // we init into the idle mode
        return idle;
    }

    // NOTE:
    // everything so far is a suggestion
    // you might want to make it that following or holding can be cancelled by incoming driver powers
    // or add another mode to use a pid controller for aiming the robot while manually driving
    // or add slow mode instructions over another channel
    // there is no "correct" way to do this in mercurial, you can take this and adjust it to your needs

    // public api:
    public void manual() {
        switches.send(manual);
    }

    public void idle() {
        switches.send(idle);
    }

    public void hold(Pose pose, boolean useScaling) {
        switches.send(new Hold(pose, useScaling));
    }

    public void followAsync(Path path, boolean holdEnd) {
        switches.send(new Follow(null, path, holdEnd));
    }

    public void followAsync(Channel<FollowResult> respondTo, Path path, boolean holdEnd) {
        switches.send(new Follow(respondTo, path, holdEnd));
    }

    public Builder<FollowResult> followSync(Supplier<Path> path, BooleanSupplier holdEnd) {
        return expression((scope, _self) -> {
            Reference.O<Channel<FollowResult>> channel = scope.o(Channel::single);
            scope.runExec(() -> switches.send(new Follow(channel.get(), path.get(), holdEnd.getAsBoolean())));
            return Continuations.<FollowResult>receive()
                .receive(
                    channel,
                    (Expression<?> _scope, Reference.O<FollowResult> result) ->
                        value.o(result)
                );
        });
    }


    public Builder<FollowResult>  followSync(Path path, BooleanSupplier holdEnd) {
        return followSync(() -> path, holdEnd);
    }
    public Builder<FollowResult>  followSync(Supplier<Path> path, boolean holdEnd) {
        return followSync(path, () -> holdEnd);
    }
    public Builder<FollowResult>  followSync(Path path, boolean holdEnd) {
        return followSync(() -> path, () -> holdEnd);
    }

    public void sendPowers(DrivePowers drivePowers) {
        this.drivePowers.send(drivePowers);
    }
}
