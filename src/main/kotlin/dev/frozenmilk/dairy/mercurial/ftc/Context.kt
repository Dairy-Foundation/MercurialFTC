package dev.frozenmilk.dairy.mercurial.ftc

import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.frozenmilk.dairy.mercurial.Mercurial
import dev.frozenmilk.dairy.mercurial.processes.Scheduler
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import java.util.function.Supplier

open class Context(
    @get:JvmName("metadata") val metadata: OpModeMeta,
    private val stateSupplier: Supplier<State>,
    @get:JvmName("hardwareMap") val hardwareMap: HardwareMap,
    @get:JvmName("telemetry") val telemetry: Telemetry,
    @get:JvmName("gamepadD") val gamepadD: GamepadD,
    @get:JvmName("gamepad1") val gamepad1: Gamepad,
    @get:JvmName("gamepad2") val gamepad2: Gamepad,
) {
    constructor(context: Context) : this(
        context.metadata,
        context.stateSupplier,
        context.hardwareMap,
        context.telemetry,
        context.gamepadD,
        context.gamepad1,
        context.gamepad2,
    )

    @get:JvmName("scheduler")
    val scheduler: Scheduler
        get() = Mercurial.scheduler

    @get:JvmName("state")
    val state
        get() = stateSupplier.get()

    //
    // flow
    //

    @get:JvmName("isActive")
    val isActive
        get() = state !== State.STOP

    @get:JvmName("inInit")
    val inInit
        get() = state === State.INIT

    @get:JvmName("inLoop")
    val inLoop
        get() = state === State.LOOP

    /**
     * puts the opmode into scheduler mode until start is pressed
     */
    fun waitForStart() {
        while (inInit) scheduler.poll()
    }

    /**
     * puts the opmode into scheduler mode until it stops
     */
    fun dropToScheduler() {
        while (isActive) scheduler.poll()
    }
}