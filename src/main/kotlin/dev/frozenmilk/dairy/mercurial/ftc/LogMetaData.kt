package dev.frozenmilk.dairy.mercurial.ftc

import dev.frozenmilk.sinister.loading.Preload
import dev.frozenmilk.sinister.util.log.Logger
import dev.frozenmilk.dairy.MercurialFTCBuildMetaData

@Preload
object LogMetaData {
    init {
        Logger.d("Meta", """
name: ${MercurialFTCBuildMetaData.name}
version: ${MercurialFTCBuildMetaData.version}
ref: ${MercurialFTCBuildMetaData.gitRef}
""")
    }
}
