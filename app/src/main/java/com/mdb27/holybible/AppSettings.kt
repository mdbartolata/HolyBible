package com.mdb27.holybible

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val initState: InitState = InitState.CHECKING,
    val selectedDir: String = ""
)
