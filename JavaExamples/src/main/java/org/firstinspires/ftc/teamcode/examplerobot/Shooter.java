package org.firstinspires.ftc.teamcode.examplerobot;

import androidx.annotation.NonNull;

import com.pedropathing.localization.MotionState;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.jetbrains.annotations.NotNull;

import dev.frozenmilk.dairy.mercurial.processes.Channel;
import dev.frozenmilk.dairy.mercurial.processes.EventManager;
import dev.frozenmilk.dairy.mercurial.processes.EventManager.EventHandled;
import dev.frozenmilk.dairy.mercurial.processes.StateMachine;

public class Shooter extends StateMachine {
    private final DcMotorEx motor;

    public Shooter(
        HardwareMap hardwareMap,
        EventManager<MotionState> localizationEventManager
    ) {
        motor = hardwareMap.get(DcMotorEx.class, "shooter");
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        // we tell the localiser to send events to us
        localizationEventManager.addHandler(motionStateForwarder);
    }

    // the channel we will receive motion states into
    private final Channel<MotionState> motionStates = Channel.single();

    // we need the ability to switch modes
    private final Channel<Mode> switches = Channel.single();

    // a fixed power
    private class Fixed implements Mode {
        public final double power;

        public Fixed(double power) {
            this.power = power;
        }

        @Override
        public void enter(@NotNull Mode previousMode) {
            motor.setPower(power);
        }

        @Override
        @NotNull
        public Mode eval() {
            Mode nextMode = switches.poll();
            if (nextMode == null) return this;
            else return nextMode;
        }
    }

    // idle
    private final Mode idle = new Mode() {
        @Override
        public void enter(@NotNull Mode previousMode) {
            motor.setPower(0);
        }

        @Override
        @NotNull
        public Mode eval() {
            Mode nextMode = switches.poll();
            if (nextMode == null) return this;
            else return nextMode;
        }
    };

    // calculate the power from the position of the robot and the target position
    private class Target implements Mode {
        public final Pose target;

        private Target(Pose target) {
            this.target = target;
        }

        @Override
        @NotNull
        public Mode eval() {
            MotionState motionState = motionStates.poll();
            if (motionState != null) {
                motor.setPower(
                    0.1 + 0.001 * Math.hypot(
                        target.x() - motionState.pose().x(),
                        target.y() - motionState.pose().y()
                    )
                );
            }
            Mode nextMode = switches.poll();
            if (nextMode == null) return this;
            else return nextMode;
        }
    }

    // this event handler will forward motion state events to the shooter
    private final EventManager.Handler<MotionState> motionStateForwarder = motionState -> {
        motionStates.send(motionState);
        // if the shooter finishes, remove this handler
        if (fiber().status().alive()) return EventHandled.ok();
        else return EventHandled.remove();
    };

    @Override
    @NotNull
    protected Mode init() {
        // we init into the idle mode
        return idle;
    }

    // public api:
    public void target(Pose pose) {
        switches.send(new Target(pose));
    }

    public void idle() {
        switches.send(idle);
    }

    public void fixed(double power) {
        switches.send(new Fixed(power));
    }
}
