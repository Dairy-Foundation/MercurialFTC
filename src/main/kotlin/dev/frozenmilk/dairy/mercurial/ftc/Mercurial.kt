package dev.frozenmilk.dairy.mercurial.ftc

import com.qualcomm.ftccommon.FtcEventLoop
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl
import dev.frozenmilk.sinister.sdk.apphooks.OnCreateEventLoop
import dev.frozenmilk.sinister.sdk.opmodes.SinisterRegisteredOpModes
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

object Mercurial {
    fun interface Program {
        fun Context.exec()
    }

    fun interface PipelineProgram {
        fun Context.exec(): Program
    }

    class RegisterableProgram(
        val name: String?,
        val group: String?,
        val type: OpModeMeta.Flavor,
        val transitionTarget: ((name: String) -> String)?,
        val program: Program,
    )

    object UntypedProgramBuilder {
        fun withType(type: OpModeMeta.Flavor) = ProgramBuilder(type)
    }

    class ProgramBuilder(
        val type: OpModeMeta.Flavor,
        val name: String? = null,
        val group: String? = null,
        val transitionTarget: ((name: String) -> String)? = null,
    ) {
        fun withName(name: String) = ProgramBuilder(
            type,
            name,
            group,
            transitionTarget,
        )

        fun withGroup(group: String) = ProgramBuilder(
            type,
            name,
            group,
            transitionTarget,
        )

        fun withTransitionTarget(transitionTarget: (name: String) -> String) = ProgramBuilder(
            type,
            name,
            group,
            transitionTarget,
        )

        fun withProgram(program: Program) = RegisterableProgram(
            name,
            group,
            type,
            transitionTarget,
            program,
        )
    }

    @JvmStatic
    fun buildProgram() = UntypedProgramBuilder

    //
    // TeleOp
    //

    @JvmStatic
    fun teleop(program: Program) = buildProgram() //
        .withType(OpModeMeta.Flavor.TELEOP) //
        .withProgram(program)

    @JvmStatic
    fun teleop(
        name: String,
        program: Program,
    ) = buildProgram() //
        .withType(OpModeMeta.Flavor.TELEOP) //
        .withName(name) //
        .withProgram(program)

    @JvmStatic
    fun teleop(
        name: String,
        group: String,
        program: Program,
    ) = buildProgram() //
        .withType(OpModeMeta.Flavor.TELEOP) //
        .withName(name) //
        .withGroup(group) //
        .withProgram(program)

    //
    // Autonomous
    //

    @JvmStatic
    fun autonomous(program: Program) = buildProgram() //
        .withType(OpModeMeta.Flavor.AUTONOMOUS) //
        .withProgram(program)

    @JvmStatic
    fun autonomous(
        name: String,
        program: Program,
    ) = buildProgram() //
        .withType(OpModeMeta.Flavor.AUTONOMOUS) //
        .withName(name) //
        .withProgram(program)

    @JvmStatic
    fun autonomous(
        name: String,
        group: String,
        program: Program,
    ) = buildProgram() //
        .withType(OpModeMeta.Flavor.AUTONOMOUS) //
        .withName(name) //
        .withGroup(group) //
        .withProgram(program)

    //
    // Pipeline Autonomous
    //

    private object OpModeManager : OnCreateEventLoop {
        var opModeManager: OpModeManagerImpl? = null
        override fun onCreateEventLoop(
            context: android.content.Context,
            ftcEventLoop: FtcEventLoop,
        ) {
            opModeManager = ftcEventLoop.opModeManager
        }
    }

    private fun pipelineName(name: String) = "$name |> teleop"

    fun liftPipeLine(program: PipelineProgram) = Program {
        val name = pipelineName(metadata.name)
        val metadata = OpModeMeta.Builder() //
            .setName("$$name$") //
            .setSystemOpModeBaseDisplayName(name) //
            .setTransitionTarget(metadata.name) //
            .setFlavor(OpModeMeta.Flavor.SYSTEM) //
            .build()

        try {
            val nextProgram = program.run { exec() }

            // register temporary opmode
            SinisterRegisteredOpModes.register(metadata) {
                MercurialProgramScanner.MercurialProgramConverter(
                    metadata
                ) {
                    try {
                        nextProgram.run { exec() }
                    } finally {
                        SinisterRegisteredOpModes.unregister(metadata)
                    }
                }
            }

            // immediately switch to it
            OpModeManager.opModeManager?.initOpMode(metadata.name)
        } catch (e: Throwable) {
            SinisterRegisteredOpModes.unregister(metadata)
            throw e
        }
    }

    @JvmStatic
    fun pipelineAutonomous(program: PipelineProgram) = buildProgram() //
        .withType(OpModeMeta.Flavor.AUTONOMOUS) //
        .withProgram(liftPipeLine(program))

    @JvmStatic
    fun pipelineAutonomous(
        name: String,
        program: PipelineProgram,
    ) = buildProgram() //
        .withType(OpModeMeta.Flavor.AUTONOMOUS) //
        .withName(name) //
        .withProgram(liftPipeLine(program))

    @JvmStatic
    fun autonomous(
        name: String,
        group: String,
        program: PipelineProgram,
    ) = buildProgram() //
        .withType(OpModeMeta.Flavor.AUTONOMOUS) //
        .withName(name) //
        .withGroup(group) //
        .withProgram(liftPipeLine(program))

    //
    // Manual Registration
    //

    fun interface ProgramRegistrationHelper {
        fun register(program: RegisterableProgram)
    }

    /**
     * static instances of [ProgramRegistrar] will be found by Sloth
     * and [register] will be called when the robot starts up
     */
    fun interface ProgramRegistrar {
        fun register(helper: ProgramRegistrationHelper)
    }
}
