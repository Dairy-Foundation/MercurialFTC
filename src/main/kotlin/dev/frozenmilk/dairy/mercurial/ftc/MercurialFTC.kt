package dev.frozenmilk.dairy.mercurial.ftc

import dev.frozenmilk.dairy.mercurial.Mercurial
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import org.jetbrains.annotations.Contract

object MercurialFTC {
    fun interface Program {
        fun settings(): Mercurial.Settings = Mercurial.Settings()
        fun Context.exec(): Program?
    }

    class RegisterableProgram(
        val name: String?,
        val group: String?,
        val type: OpModeMeta.Flavor,
        val transitionTarget: String?,
        val program: Program,
    ): Program by program {
        @Contract(pure = true)
        fun withName(name: String) = RegisterableProgram(
            name,
            group,
            type,
            transitionTarget,
            program,
        )

        @Contract(pure = true)
        fun withGroup(group: String) = RegisterableProgram(
            name,
            group,
            type,
            transitionTarget,
            program,
        )

        @Contract(pure = true)
        fun withTransitionTarget(transitionTarget: String) = RegisterableProgram(
            name,
            group,
            type,
            transitionTarget,
            program,
        )
    }

    object UntypedProgramBuilder {
        fun withType(type: OpModeMeta.Flavor) = ProgramBuilder(type)
    }

    class ProgramBuilder(
        val type: OpModeMeta.Flavor,
        val name: String? = null,
        val group: String? = null,
        val transitionTarget: String? = null,
    ) {
        @Contract(pure = true)
        fun withName(name: String) = ProgramBuilder(
            type,
            name,
            group,
            transitionTarget,
        )

        @Contract(pure = true)
        fun withGroup(group: String) = ProgramBuilder(
            type,
            name,
            group,
            transitionTarget,
        )

        @Contract(pure = true)
        fun withTransitionTarget(transitionTarget: String) = ProgramBuilder(
            type,
            name,
            group,
            transitionTarget,
        )

        @Contract(pure = true)
        fun withProgram(program: Program) = RegisterableProgram(
            name,
            group,
            type,
            transitionTarget,
            program,
        )
    }

    @JvmStatic
    @Contract(pure = true)
    fun buildProgram() = UntypedProgramBuilder

    //
    // TeleOp
    //

    @JvmStatic
    @Contract(pure = true)
    fun teleop(program: Program) = buildProgram() //
        .withType(OpModeMeta.Flavor.TELEOP) //
        .withProgram(program)

    //
    // Autonomous
    //

    @JvmStatic
    @Contract(pure = true)
    fun autonomous(program: Program) = buildProgram() //
        .withType(OpModeMeta.Flavor.AUTONOMOUS) //
        .withProgram(program)

    //
    // Utility
    //

    @JvmStatic
    @Contract(pure = true)
    fun utility(program: Program) = buildProgram() //
        .withType(OpModeMeta.Flavor.UTILITY) //
        .withProgram(program)

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
