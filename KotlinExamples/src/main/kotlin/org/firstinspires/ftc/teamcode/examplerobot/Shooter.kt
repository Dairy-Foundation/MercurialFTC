package org.firstinspires.ftc.teamcode.examplerobot

import com.pedropathing.localization.MotionState
import com.pedropathing.math.Pose
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.frozenmilk.dairy.mercurial.processes.Channel
import dev.frozenmilk.dairy.mercurial.processes.Channel.Companion.tryPoll
import dev.frozenmilk.dairy.mercurial.processes.EventManager
import dev.frozenmilk.dairy.mercurial.processes.StateMachine
import kotlin.math.hypot

class Shooter(
    hardwareMap: HardwareMap,
    localizationEventManager: EventManager<MotionState>,
) : StateMachine() {
    private val motor = hardwareMap[DcMotorEx::class.java, "shooter"]

    init {
        motor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
    }

    // the channel we will receive motion states into
    private val motionStates = Channel.single<MotionState>()

    // we need the ability to switch modes
    private val switches = Channel.single<Mode>()

    // a fixed power
    private inner class Fixed(val power: Double) : Mode {
        override fun enter(previousMode: Mode) {
            motor.power = power
        }

        override fun eval(): Mode = switches.poll() ?: this
    }

    // idle
    private val idle = object : Mode {
        override fun enter(previousMode: Mode) {
            motor.power = 0.0
        }

        override fun eval(): Mode = switches.poll() ?: this
    }

    // calculate the power from the position of the robot and the target position
    private inner class Target(val target: Pose) : Mode {
        override fun eval(): Mode {
            motionStates.tryPoll { motionState ->
                motor.power = 0.1 + 0.001 * hypot(
                    target.x() - motionState.pose().x(),
                    target.y() - motionState.pose().y(),
                )
            }
            return switches.poll() ?: this
        }
    }

    // this event handler will forward motion state events to the shooter
    private val motionStateForwarder = object : EventManager.Handler<MotionState> {
        override fun handleEvent(event: MotionState): EventManager.EventHandled<MotionState> {
            motionStates.send(event)
            // if the shooter finishes, remove this handler
            return if (fiber.status.alive) EventManager.EventHandled.Ok
            else EventManager.EventHandled.Remove
        }
    }

    init {
        // we tell the localiser to send events to us
        localizationEventManager.addHandler(motionStateForwarder)
    }

    override fun init(): Mode {
        // we init into the idle mode
        return idle
    }

    // public api:
    fun target(pose: Pose) {
        switches.send(Target(pose))
    }

    fun idle() {
        switches.send(idle)
    }

    fun fixed(power: Double) {
        switches.send(Fixed(power))
    }
}