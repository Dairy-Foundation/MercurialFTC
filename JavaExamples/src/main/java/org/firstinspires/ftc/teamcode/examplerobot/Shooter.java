package org.firstinspires.ftc.teamcode.examplerobot;


import com.pedropathing.localization.MotionState;
import com.pedropathing.math.Vector2D;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.jetbrains.annotations.NotNull;

import dev.frozenmilk.dairy.mercurial.ftc.Context;
import dev.frozenmilk.dairy.mercurial.processes.Channel;
import dev.frozenmilk.dairy.mercurial.processes.SwitchedStateMachine;

public class Shooter extends SwitchedStateMachine {
    // the channel we will receive motion states into
    private final Channel<MotionState> motionStates = linkedSingleChannel();
    
    // motor
    private final DcMotorEx motor;
    // idle
    private final Mode idle;
    
    public Shooter(Context ctx) {
        motor = ctx.hardwareMap().get(DcMotorEx.class, "shooter");
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        
        // when you enter the idle mode, turn off the motor
        idle = entranceMode(from -> motor.setPower(0));
    }
    
    // a fixed power
    private Mode fixedMode(double power) {
        return entranceMode(from -> motor.setPower(power));
    }
    
    // targeting a pose
    private Mode targetMode(Vector2D target) {
        return mode(self -> {
            // we poll the motion states,
            // so that we only do work when we get a new motion state
            MotionState motionState = motionStates.poll();
            if (motionState != null) {
                // calculate the power from the position of the robot and the target position
                motor.setPower(
                    0.1 + 0.001 * Math.hypot(
                        target.x() - motionState.pose().x(),
                        target.y() - motionState.pose().y()
                    )
                );
            }
            return self;
        });
    }
    
    @Override
    @NotNull
    protected Mode init() {
        // we init into the idle mode
        return idle;
    }
    
    // public api:
    
    public void sendMotionState(MotionState motionState) {
        motionStates.send(motionState);
    }
    
    public void target(Vector2D point) {
        switchTo(targetMode(point));
    }
    
    public void idle() {
        switchTo(idle);
    }
    
    public void fixed(double power) {
        switchTo(fixedMode(power));
    }
}
