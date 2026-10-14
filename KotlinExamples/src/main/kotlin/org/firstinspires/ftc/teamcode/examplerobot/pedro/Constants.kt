package org.firstinspires.ftc.teamcode.examplerobot.pedro

import com.pedropathing.algorithm.Foresight
import com.pedropathing.algorithm.ForesightConfig
import com.pedropathing.revhub.drivetrains.Mecanum
import com.pedropathing.revhub.drivetrains.MecanumConfig
import com.pedropathing.revhub.localizers.PinpointConfig
import com.pedropathing.revhub.localizers.PinpointLocalizer
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.frozenmilk.dairy.mercurial.pedro.MercurialFollower
import dev.frozenmilk.dairy.mercurial.pedro.MercurialLocalizer

object Constants {
    val pinpointConfig = PinpointConfig { c ->
        c.name.set("pinpoint")
        // TODO: this is a stub!
    }
    val mecanumConfig = MecanumConfig { c ->
        c.backLeftName.set("bl")
        // TODO: this is a stub!
    }
    val foresightConfig = ForesightConfig { c ->
        // TODO: this is a stub!
    }

    fun createLocalizer(h: HardwareMap) = MercurialLocalizer(
        PinpointLocalizer(h, pinpointConfig),
    )

    fun createFollower(h: HardwareMap) = MercurialFollower(
        Mecanum(h, mecanumConfig),
        Foresight(foresightConfig),
    )
}
