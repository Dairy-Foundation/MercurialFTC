package dev.frozenmilk.dairy.mercurial.ftc

import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.frozenmilk.dairy.mercurial.continuations.Continuations
import dev.frozenmilk.dairy.mercurial.continuations.Fiber
import dev.frozenmilk.dairy.mercurial.continuations.IntoContinuation
import dev.frozenmilk.dairy.mercurial.continuations.Scheduler
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import java.util.function.BooleanSupplier
import java.util.function.Supplier

open class Context(
    @get:JvmName("metadata") val metadata: OpModeMeta,
    private val stateSupplier: Supplier<State>,
    @get:JvmName("scheduler") val scheduler: Scheduler,
    @get:JvmName("hardwareMap") val hardwareMap: HardwareMap,
    @get:JvmName("telemetry") val telemetry: Telemetry,
    @get:JvmName("gamepad1") val gamepad1: Gamepad,
    @get:JvmName("gamepad2") val gamepad2: Gamepad,
) {
    @get:JvmName("state")
    val state
        get() = stateSupplier.get()

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
     * if [k] is still running, the previously spawned [dev.frozenmilk.dairy.mercurial.continuations.Fiber] will be cancelled
     */
    fun bindExec(
        cond: BooleanSupplier,
        k: IntoContinuation,
    ) = run {
        val k = k.intoContinuation()
        scheduler.schedule(
            Continuations.scope {
                val fiber = variable<Fiber?> { null }
                Continuations.loop(Continuations.exec {
                    if (cond.asBoolean) fiber.map { fiber ->
                        if (fiber == null) scheduler.schedule(k)
                        else {
                            Fiber.Companion.CANCEL(fiber)
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
            Continuations.loop(Continuations.exec { if (cond.asBoolean) scheduler.schedule(k) })
                .intoContinuation()
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
            Continuations.scope {
                val fiber = variable<Fiber?> { null }
                Continuations.loop(Continuations.exec {
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