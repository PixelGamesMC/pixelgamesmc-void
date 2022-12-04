package eu.pixelgamesmc.void.database

import java.util.UUID

data class DatabasePlayer(val uuid: UUID, val name: String, val items: Long, val deaths: Int, val friends: List<UUID>)
