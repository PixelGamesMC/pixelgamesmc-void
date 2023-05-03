package eu.pixelgamesmc.void.database

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class DatabasePlayer(
    @Contextual @SerialName("_id") val uuid: UUID,
    val name: String,
    val items: Long,
    val deaths: Int,
    val friends: List<String>
)
