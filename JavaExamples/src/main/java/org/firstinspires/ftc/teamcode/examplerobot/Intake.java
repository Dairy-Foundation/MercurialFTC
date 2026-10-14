package org.firstinspires.ftc.teamcode.examplerobot;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.jetbrains.annotations.NotNull;

import dev.frozenmilk.dairy.mercurial.ftc.Context;
import dev.frozenmilk.dairy.mercurial.processes.SwitchedStateMachine;

// this is a very simple intake subsystem
public class Intake extends SwitchedStateMachine {
    private final DcMotorEx motor;
    
    private final Mode on, off;
    
    private boolean enabled = true;
    
    public Intake(Context ctx) {
        motor = ctx.hardwareMap().get(DcMotorEx.class, "intake");
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setDirection(DcMotorSimple.Direction.FORWARD);
        
        // off is always off
        off = entranceMode(from -> motor.setPower(0));
        on = mode(self -> {
            // on needs to check if the intake is enabled
            // if the intake isn't enabled, wait for it to become enabled
            if (enabled) motor.setPower(1);
            else motor.setPower(0);
            return self;
        });
    }
    
    @Override
    @NotNull
    protected Mode init() {
        return off;
    }
    
    // public api
    
    public void switchOn() {
        switchTo(on);
    }
    
    public void switchOff() {
        switchTo(off);
    }
    
    public void enable(boolean enabled) {
        this.enabled = enabled;
    }
}
