package eu.pixelgamesmc.void.database

import kotlinx.serialization.Serializable

@Serializable
data class Credentials(
    val mongo: Mongo,
    val redis: Redis,
) {
    @Serializable
    data class Mongo(
        val local: Boolean,
        val connectionString: String,
        val database: String,
    )

    @Serializable
    data class Redis(
        val local: Boolean,
        val hostname: String,
        val port: Int,
        val username: String,
        val password: String,
    )
}