<a href="https://repo.dairy.foundation/#/releases/dev/frozenmilk/dairy/MercurialFTC" target="_blank">
<img src="https://repo.dairy.foundation/api/badge/latest/releases/dev/frozenmilk/dairy/MercurialFTC?color=40c14a&name=MercurialFTC" />
</a>

[Mercurial](https://github.com/Dairy-Foundation/Mercurial/tree/continuations) 2.0 is currently in beta!

Take a look at the [Java](./JavaExamples/src/main/java/org/firstinspires/ftc/teamcode/beginners/README.md)
or the [Kotlin](./KotlinExamples/src/main/kotlin/org/firstinspires/ftc/teamcode/beginners/README.md)
Documentation.

This library serves to provide bindings to assist in using Mercurial with
the FTC SDK, it is considered unstable, as it will be replaced by the Dairy SDK.

This library also provides examples for using Mercurial.

Issues should generally be filed with the [Mercurial repository](https://github.com/Dairy-Foundation/Mercurial).

# Installation

more details here:
https://repo.dairy.foundation/#/releases/dev/frozenmilk/dairy/MercurialFTC

groovy:
```groovy
repositories {
    maven {
        name "dairyReleases"
        url "https://repo.dairy.foundation/releases"
    }
}
```

```groovy
dependencies {
    implementation("dev.frozenmilk.dairy:MercurialFTC:2.0.0-beta5")
}
```

This library will automatically supply Sloth, Mercurial and Util.
