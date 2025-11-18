package dev.frozenmilk.dairy.mercurial.ftc

import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.scope
import dev.frozenmilk.dairy.mercurial.continuations.Fiber
import dev.frozenmilk.dairy.mercurial.continuations.IntoContinuation
import dev.frozenmilk.dairy.mercurial.continuations.Scheduler
import org.firstinspires.ftc.robotcore.external.Telemetry
import java.util.function.BooleanSupplier
import java.util.function.Supplier

/**
 * NOTE: not an [com.qualcomm.robotcore.eventloop.opmode.OpMode].
 *
 * [MercurialOpMode]s act much like [com.qualcomm.robotcore.eventloop.opmode.LinearOpMode]
 *
 * differences:
 * 1. all symbols are valid from construction
 *    * this means you can access the [HardwareMap] immediately
 * 2. once this class is constructed, the OpMode ends
 *    * put your code in the constructor / class initialisation blocks
 * 3. the [Context] will be provided to the subclass
 * 4. `@TeleOp` and `@Autonomous` work like expected
 *    * see [MercurialOpModeScanner] for how [MercurialOpMode] is converted
 *      to an OpMode and how the context is supplied
 */
abstract class MercurialOpMode(private val context: Context) {
    enum class State {
        INIT, LOOP, STOP,
    }

    class Context(
        val state: Supplier<State>,
        val scheduler: Scheduler,
        val hardwareMap: HardwareMap,
        val telemetry: Telemetry,
        val gamepad1: Gamepad,
        val gamepad2: Gamepad,
    )

    //
    // state
    //

    @get:JvmName("state")
    val state
        get() = context.state.get()

    @get:JvmName("scheduler")
    val scheduler = context.scheduler

    @get:JvmName("hardwareMap")
    val hardwareMap = context.hardwareMap

    @get:JvmName("telemetry")
    val telemetry = context.telemetry

    @get:JvmName("gamepad1")
    val gamepad1 = context.gamepad1

    @get:JvmName("gamepad2")
    val gamepad2 = context.gamepad2

    //
    // flow
    //

    @get:JvmName("isActive")
    val isActive
        get() = state != State.STOP

    @get:JvmName("inInit")
    val inInit
        get() = state == State.INIT

    @get:JvmName("inLoop")
    val inLoop
        get() = state == State.LOOP

    /**
     * puts the opmode into scheduler mode until start is pressed
     */
    fun waitForStart() = scheduler.start(::inInit)

    /**
     * puts the opmode into scheduler mode until it stops
     */
    fun dropToScheduler() {
        scheduler.start(::isActive)
        scheduler.shutdown()
    }

    //
    // binding helpers
    //

    /**
     * adds a rising edge filter to [cond]
     */
    fun risingEdge(cond: BooleanSupplier) = object : BooleanSupplier {
        private var prev = false
        override fun getAsBoolean(): Boolean {
            val next = cond.asBoolean
            val res = !prev && next
            prev = next
            return res
        }
    }

    /**
     * immediately schedules [k]
     */
    fun schedule(k: IntoContinuation) = scheduler.schedule(k.intoContinuation())

    /**
     * WARNING: do not call this in a loop, as it sets up a process that runs until the opmode ends
     *
     * binds [k] to be spawned when [cond] returns true
     *
     * if [k] is still running, the previously spawned [Fiber] will be cancelled
     */
    fun bindExec(
        cond: BooleanSupplier,
        k: IntoContinuation,
    ) = run {
        val k = k.intoContinuation()
        scheduler.schedule(
            scope {
                val fiber = variable<Fiber?> { null }
                loop(exec {
                    if (cond.asBoolean) fiber.map { fiber ->
                        if (fiber == null) scheduler.schedule(k)
                        else {
                            Fiber.CANCEL(fiber)
                            scheduler.schedule(k)
                        }
                    }
                })
            }.intoContinuation()
        )
    }

    /**
     * binds [k] to be spawned when [cond] returns true
     *
     * if [k] is still running, the previously spawned [Fiber] will not cancelled,
     * instead, another process will be spawned
     */
    fun bindSpawn(
        cond: BooleanSupplier,
        k: IntoContinuation,
    ) = run {
        val k = k.intoContinuation()
        scheduler.schedule(
            loop(exec { if (cond.asBoolean) scheduler.schedule(k) }).intoContinuation()
        )
    }

    /**
     * binds [k] to be spawned whenever [cond] returns true
     *
     * when [cond] becomes false, cancels the running [Fiber]
     */
    fun bindWhileTrue(
        cond: BooleanSupplier,
        k: IntoContinuation,
    ) = run {
        val k = k.intoContinuation()
        scheduler.schedule(
            scope {
                val fiber = variable<Fiber?> { null }
                loop(exec {
                    if (cond.asBoolean) fiber.set(scheduler.schedule(k))
                    else fiber.map { fiber ->
                        if (fiber != null) Fiber.CANCEL(fiber)
                        null
                    }
                })
            }.intoContinuation()
        )
    }
}