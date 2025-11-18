pluginManagement {
	repositories {
		gradlePluginPortal()
		mavenCentral()
		google()
		maven("https://repo.dairy.foundation/releases")
	}
}

includeBuild("..") {
    dependencySubstitution {
        substitute(module("dev.frozenmilk.dairy:MercurialFTC")).using(project(":"))
    }
}
