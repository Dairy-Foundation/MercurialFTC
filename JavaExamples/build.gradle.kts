plugins {
	id("dev.frozenmilk.teamcode") version "10.3.0-0.1.4"
}

ftc {

}
repositories {
    maven("https://repo.dairy.foundation/releases")
}

dependencies {
    api("dev.frozenmilk.dairy:MercurialFTC")
}