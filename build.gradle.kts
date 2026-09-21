plugins {
    id("dev.frozenmilk.android-library") version "12.0.0-1.2.2"
    id("dev.frozenmilk.publish") version "0.1.0"
    id("dev.frozenmilk.doc") version "0.1.0"
    id("dev.frozenmilk.build-meta-data") version "0.1.0"
}

android.namespace = "dev.frozenmilk.dairy"

// Most FTC libraries will want the following
ftc {
    kotlin()

    sdk {
        compileOnly(RobotCore)
        compileOnly(FtcCommon)
        compileOnly(Hardware)
    }

    dairy {
        api(Sloth("0.3.2"))
        api(Mercurial("2.0.1-beta0"))
    }
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-reflect")
    compileOnly("com.pedropathing:core:3.0.1")
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
