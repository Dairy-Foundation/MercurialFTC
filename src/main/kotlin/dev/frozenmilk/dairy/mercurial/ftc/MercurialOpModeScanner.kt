package dev.frozenmilk.dairy.mercurial.ftc

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.util.RobotLog
import dev.frozenmilk.dairy.mercurial.continuations.Scheduler
import dev.frozenmilk.sinister.sdk.opmodes.OpModeScanner
import dev.frozenmilk.sinister.targeting.WideSearch
import dev.frozenmilk.sinister.util.log.Logger
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

@Suppress("UNUSED")
object MercurialOpModeScanner : OpModeScanner() {
    override val targets = WideSearch()

    class MercurialOpModeConverter(private val cls: Class<MercurialOpMode>) :
        LinearOpMode() {
        override fun runOpMode() {
            val scheduler = Scheduler.Standard()
            val context = MercurialOpMode.Context(
                {
                    if (isStarted) if (isStopRequested) MercurialOpMode.State.STOP
                    else MercurialOpMode.State.LOOP
                    else MercurialOpMode.State.INIT
                },
                scheduler,
                hardwareMap,
                telemetry,
                gamepad1,
                gamepad2,
            )
            cons(cls, context)
        }
    }

    private fun cons(
        cls: Class<out MercurialOpMode>,
        context: MercurialOpMode.Context,
    ): MercurialOpMode = cls.getDeclaredConstructor(MercurialOpMode.Context::class.java).run {
        isAccessible = true
        newInstance(context)
    }

    @Suppress("UNCHECKED_CAST")
    override fun scan(
        loader: ClassLoader,
        cls: Class<*>,
        registrationHelper: RegistrationHelper,
    ) {
        if (MercurialOpMode::class.java.isAssignableFrom(cls)) {
            if (cls.isAnnotationPresent(TeleOp::class.java)) {
                if (cls.isAnnotationPresent(Autonomous::class.java)) {
                    val error = "class $cls is annotated with both '@TeleOp' and '@Autonomous'; please choose one at most"
                    Logger.e(
                        javaClass.simpleName,
                        "OpMode Configuration Error:\n$error",
                    )
                    RobotLog.setGlobalErrorMsg(error)
                }
                val annotation = cls.getDeclaredAnnotation(TeleOp::class.java)!!
                registrationHelper.register(
                    OpModeMeta.Builder()
                        // name
                        .setName(annotation.name.ifBlank { cls.simpleName }.apply {
                            if (!OpModeMeta.nameIsLegalForOpMode(this, false)) {
                                val error = "\"$this\" is not a legal OpMode name"
                                Logger.e(
                                    javaClass.simpleName,
                                    "OpMode Configuration Error:\n$error",
                                )
                                RobotLog.setGlobalErrorMsg(error)
                            }
                        })
                        // group
                        .setGroup(annotation.group.ifEmpty { OpModeMeta.DefaultGroup })
                        // flavour
                        .setFlavor(OpModeMeta.Flavor.TELEOP)
                        // source
                        .setSource(OpModeMeta.Source.ANDROID_STUDIO)
                        // build
                        .build()
                ) {
                    MercurialOpModeConverter(cls as Class<MercurialOpMode>)
                }
            }
            else if (cls.isAnnotationPresent(Autonomous::class.java)) {
                val annotation = cls.getDeclaredAnnotation(Autonomous::class.java)!!
                registrationHelper.register(
                    OpModeMeta.Builder()
                        // name
                        .setName(annotation.name.ifBlank { cls.simpleName }.apply {
                            if (!OpModeMeta.nameIsLegalForOpMode(this, false)) {
                                val error = "\"$this\" is not a legal OpMode name"
                                Logger.e(
                                    javaClass.simpleName,
                                    "OpMode Configuration Error:\n$error",
                                )
                                RobotLog.setGlobalErrorMsg(error)
                            }
                        })
                        // group
                        .setGroup(annotation.group.ifEmpty { OpModeMeta.DefaultGroup })
                        // flavour
                        .setFlavor(OpModeMeta.Flavor.AUTONOMOUS)
                        // source
                        .setSource(OpModeMeta.Source.ANDROID_STUDIO)
                        // build
                        .build()
                ) {
                    MercurialOpModeConverter(cls as Class<MercurialOpMode>)
                }
            }
        }
    }
}