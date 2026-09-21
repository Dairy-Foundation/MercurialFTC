plugins {
    id("dev.frozenmilk.teamcode") version "12.0.0-1.2.2"
    id("dev.frozenmilk.sinister.sloth.load") version "0.3.2"
}

ftc {
    sdk.TeamCode()

    dairy {
        implementation(Sloth("0.3.2"))
        implementation(MercurialFTC(""))
    }
}

dependencies {
    implementation("com.pedropathing:revhub:3.0.0")
    implementation("com.pedropathing:core:3.0.0")
}
