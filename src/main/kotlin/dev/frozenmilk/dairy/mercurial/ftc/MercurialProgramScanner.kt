package dev.frozenmilk.dairy.mercurial.ftc

import com.qualcomm.robotcore.eventloop.opmode.MercurialOpMode
import dev.frozenmilk.sinister.isPublic
import dev.frozenmilk.sinister.isStatic
import dev.frozenmilk.sinister.sdk.opmodes.OpModeScanner
import dev.frozenmilk.sinister.targeting.WideSearch
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import kotlin.reflect.KVisibility
import kotlin.reflect.jvm.kotlinProperty

@Suppress("UNUSED")
object MercurialProgramScanner : OpModeScanner() {
    override val targets = WideSearch()

    private fun isProgram(cls: Class<*>) =
        MercurialFTC.RegisterableProgram::class.java.isAssignableFrom(cls)

    override fun scan(
        loader: ClassLoader,
        cls: Class<*>,
        registrationHelper: RegistrationHelper,
    ) {
        cls.declaredFields.filter { field ->
            field.type == MercurialFTC.ProgramRegistrar::class.java //
                    && field.isStatic()
        }.forEach { field ->
            field.isAccessible = true
            val registrar = field.get(null) as MercurialFTC.ProgramRegistrar

            registrar.register { registerableProgram ->
                val metadata = OpModeMeta.Builder() //
                    .setName(requireNotNull(registerableProgram.name) { "dynamic registration of a program must provide a name" }) //
                    .setGroup(registerableProgram.group ?: OpModeMeta.DefaultGroup) //
                    .setFlavor(registerableProgram.type) //
                    .setTransitionTarget(registerableProgram.transitionTarget) //
                    .setSource(OpModeMeta.Source.ANDROID_STUDIO) //
                    .build() //

                registrationHelper.register(metadata) {
                    MercurialOpMode(
                        metadata.name,
                        metadata,
                        0,
                        registerableProgram.program,
                    )
                }
            }
        }

        cls.declaredFields.filter { field ->
            field.type == MercurialFTC.RegisterableProgram::class.java //
                    && field.kotlinProperty?.let { it.visibility == KVisibility.PUBLIC } ?: field.isPublic() //
                    && field.isStatic() //
        }.forEach {
            it.isAccessible = true
            val registerableProgram = it.get(null) as MercurialFTC.RegisterableProgram

            val metadata = OpModeMeta.Builder() //
                .setName(registerableProgram.name ?: it.name) //
                .setGroup(registerableProgram.group ?: OpModeMeta.DefaultGroup) //
                .setFlavor(registerableProgram.type) //
                .setTransitionTarget(registerableProgram.transitionTarget) //
                .setSource(OpModeMeta.Source.ANDROID_STUDIO) //
                .build() //

            registrationHelper.register(metadata) {
                MercurialOpMode(
                    metadata.name,
                    metadata,
                    0,
                    registerableProgram.program,
                )
            }
        }
    }
}