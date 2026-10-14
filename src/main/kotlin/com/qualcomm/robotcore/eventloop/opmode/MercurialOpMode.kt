package com.qualcomm.robotcore.eventloop.opmode

import android.annotation.SuppressLint
import com.qualcomm.ftccommon.FtcEventLoop
import com.qualcomm.robotcore.hardware.Gamepad
import dev.frozenmilk.dairy.mercurial.Mercurial
import dev.frozenmilk.dairy.mercurial.ftc.Context
import dev.frozenmilk.dairy.mercurial.ftc.GamepadD
import dev.frozenmilk.dairy.mercurial.ftc.MercurialFTC
import dev.frozenmilk.dairy.mercurial.ftc.State
import dev.frozenmilk.sinister.sdk.apphooks.OnCreateEventLoop
import dev.frozenmilk.sinister.sdk.apphooks.OnDestroy
import dev.frozenmilk.sinister.sdk.opmodes.SinisterRegisteredOpModes
import dev.frozenmilk.sinister.util.log.Logger
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

class MercurialOpMode(
    val metadata: OpModeMeta,
    val dynamicPhase: Int,
    val originalProgram: MercurialFTC.Program,
    val program: MercurialFTC.Program,
) : LinearOpMode() {
    private object OpModeManager {
        @SuppressLint("StaticFieldLeak")
        var opModeManager: OpModeManagerImpl? = null

        @Suppress("unused")
        private object OnCreateEventLoopHandler : OnCreateEventLoop {
            override fun onCreateEventLoop(
                context: android.content.Context,
                ftcEventLoop: FtcEventLoop,
            ) {
                opModeManager = ftcEventLoop.opModeManager
            }
        }

        @Suppress("unused")
        private object OnDestroyHandler : OnDestroy {
            override fun onDestroy(context: android.content.Context) {
                opModeManager = null
            }
        }
    }

    private companion object {
        val TAG: String = MercurialOpMode::class.java.simpleName
        val blankData = GamepadD.Data()
    }

    var gamepadD: GamepadD? = null
    private var gamepad1Data = blankData
    private var gamepad2Data = blankData

    override fun newGamepadDataAvailable(
        latestGamepad1Data: Gamepad,
        latestGamepad2Data: Gamepad,
    ) {
        super.newGamepadDataAvailable(latestGamepad1Data, latestGamepad2Data)
        synchronized(this) {
            val data1 = GamepadD.Data(gamepad1)
            gamepadD?.notify(
                GamepadD.Delta(
                    GamepadD.Id.One,
                    latestGamepad1Data.type,
                    gamepad1Data,
                    data1,
                )
            )
            gamepad1Data = data1
            val data2 = GamepadD.Data(gamepad2)
            gamepadD?.notify(
                GamepadD.Delta(
                    GamepadD.Id.Two,
                    latestGamepad2Data.type,
                    gamepad2Data,
                    data2,
                )
            )
            gamepad2Data = data2
        }
    }

    override fun runOpMode() {
        telemetry.isAutoClear = false
        Mercurial.initialiseThread(program.settings())
        gamepadD = GamepadD()
        val context = Context(
            metadata,
            @Suppress("UsePropertyAccessSyntax")
            {
                if (isStopRequested()) State.STOP
                else if (isStarted()) State.LOOP
                else State.INIT
            },
            hardwareMap,
            telemetry,
            gamepadD!!,
            gamepad1,
            gamepad2,
        )

        try {
            val nextProgram = program.run { context.exec() }

            // if no next program,
            // or if the program was stopped in the init phase,
            // do not move to the next stage
            if (nextProgram === null || !this.isStarted) {
                Logger.d(
                    TAG,
                    "dynamic pipeline ${metadata.name} dynamic phase $dynamicPhase terminated or didn't return next phase, re-registering original"
                )
                // re-register the original
                SinisterRegisteredOpModes.unregister(metadata)
                SinisterRegisteredOpModes.register(metadata) {
                    MercurialOpMode(
                        metadata,
                        0,
                        originalProgram,
                        originalProgram,
                    )
                }
            } else {
                Logger.d(TAG, "re-registering ${metadata.name} dynamic phase $dynamicPhase")
                // replace the current version of the opmode with a dynamic one
                SinisterRegisteredOpModes.unregister(metadata)
                SinisterRegisteredOpModes.register(
                    metadata,
                    MercurialOpMode(
                        metadata,
                        dynamicPhase + 1,
                        originalProgram,
                        nextProgram,
                    )
                )

                OpModeManager.opModeManager?.initOpMode(metadata.name)
            }
        } catch (e: Throwable) {
            Logger.d(
                TAG,
                "opmode ${metadata.name} dynamic phase $dynamicPhase crashed, re-registering original"
            )
            // if something goes wrong, re-register the original
            SinisterRegisteredOpModes.unregister(metadata)
            SinisterRegisteredOpModes.register(metadata) {
                MercurialOpMode(
                    metadata,
                    0,
                    originalProgram,
                    originalProgram,
                )
            }
            throw e
        } finally {
            // always shutdown
            context.scheduler.shutdown()
        }
    }
}
