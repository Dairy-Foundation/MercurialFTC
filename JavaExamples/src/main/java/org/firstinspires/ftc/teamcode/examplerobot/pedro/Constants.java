package org.firstinspires.ftc.teamcode.examplerobot.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.robotcore.hardware.HardwareMap;

import dev.frozenmilk.dairy.mercurial.pedro.MercurialFollower;
import dev.frozenmilk.dairy.mercurial.pedro.MercurialLocalizer;

public class Constants {
    private static final PinpointConfig pinpointConfig = new PinpointConfig(c -> {
        c.name.set("pinpoint");
        // TODO: this is a stub!
    });
    private static final MecanumConfig mecanumConfig = new MecanumConfig(c -> {
        c.backLeftName.set("bl");
        // TODO: this is a stub!
    });
    private static final ForesightConfig foresightConfig = new ForesightConfig(c -> {
        // TODO: this is a stub!
    });
    
    public static MercurialFollower createFollower(HardwareMap h) {
        return new MercurialFollower(
            new Mecanum(h, mecanumConfig),
            new Foresight(foresightConfig)
        );
    }
    
    public static MercurialLocalizer createLocalizer(HardwareMap h) {
        return new MercurialLocalizer(
            new PinpointLocalizer(h, pinpointConfig)
        );
    }
}
