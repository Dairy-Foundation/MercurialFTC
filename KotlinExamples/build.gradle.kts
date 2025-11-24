plugins {
    id("dev.frozenmilk.teamcode") version "10.3.0-0.1.4"
    id("dev.frozenmilk.sinister.sloth.load") version "0.2.4"
}
// Most FTC libraries will want the following
ftc {
    kotlin // if you don't want to use kotlin, remove this
}

repositories {
    maven("https://repo.dairy.foundation/releases")
}

dependencies {
    api("dev.frozenmilk.dairy:MercurialFTC")
}