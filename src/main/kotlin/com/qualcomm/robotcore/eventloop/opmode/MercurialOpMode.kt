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
import dev.frozenmilk.sinister.sdk.opmodes.SinisterRegisteredOpModes
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

class MercurialOpMode(
    private val name: String,
    private val metadata: OpModeMeta,
    private val dynamicPhase: Int,
    private val program: MercurialFTC.Program,
) : LinearOpMode() {
    private object OpModeManager : OnCreateEventLoop {
        @SuppressLint("StaticFieldLeak")
        var opModeManager: OpModeManagerImpl? = null
        override fun onCreateEventLoop(
            context: android.content.Context,
            ftcEventLoop: FtcEventLoop,
        ) {
            opModeManager = ftcEventLoop.opModeManager
        }
    }
    private companion object {
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
            {
                if (isStopRequested) State.STOP
                else if (isStarted) State.LOOP
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
            context.scheduler.shutdown()
            if (nextProgram === null) return

            val name = "$name phase ${dynamicPhase + 1}"

            val metadata = OpModeMeta.Builder() //
                .setName("$$name$") //
                .setSystemOpModeBaseDisplayName(name) //
                .setFlavor(OpModeMeta.Flavor.SYSTEM) //
                .build()

            SinisterRegisteredOpModes.register(
                metadata,
                MercurialOpMode(
                    this.name,
                    metadata,
                    dynamicPhase + 1,
                    nextProgram,
                )
            )

            OpModeManager.opModeManager?.initOpMode(metadata.name)
        } finally {
            if (dynamicPhase > 0) SinisterRegisteredOpModes.unregister(metadata)
        }
    }
}