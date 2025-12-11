plugins {
    id("dev.frozenmilk.android-library") version "10.3.0-0.1.4"
    id("dev.frozenmilk.publish") version "0.0.5"
    id("dev.frozenmilk.doc") version "0.0.5"
    id("dev.frozenmilk.build-meta-data") version "0.0.2"
}

android.namespace = "dev.frozenmilk.dairy"

// Most FTC libraries will want the following
ftc {
    kotlin // if you don't want to use kotlin, remove this

    sdk {
        RobotCore
        FtcCommon {
            configurationNames += "testImplementation"
        }
    }
}

repositories {
    maven("https://repo.dairy.foundation/releases")
}

dependencies {
    api("dev.frozenmilk.sinister:Sloth:0.2.4")
    api("dev.frozenmilk.dairy:Mercurial:2.0.0-beta6")
    api("org.jetbrains.kotlin:kotlin-reflect")
}

meta {
    packagePath = "dev.frozenmilk.dairy"
    name = "MercurialFTC"
    registerField("name", "String", "\"dev.frozenmilk.dairy.MercurialFTC\"")
    registerField("clean", "Boolean") { "${dairyPublishing.clean}" }
    registerField("gitRef", "String") { "\"${dairyPublishing.gitRef}\"" }
    registerField("snapshot", "Boolean") { "${dairyPublishing.snapshot}" }
    registerField("version", "String") { "\"${dairyPublishing.version}\"" }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "dev.frozenmilk.dairy"
            artifactId = "MercurialFTC"

            artifact(dairyDoc.dokkaHtmlJar)
            artifact(dairyDoc.dokkaJavadocJar)

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
