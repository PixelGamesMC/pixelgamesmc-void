package eu.pixelgamesmc.void.configuration

object ServerConfiguration: Configuration("config") {

    override fun create() {
        append("world-lobby", "lobby")
        append("local", true)
        append("connection", "mongodb://127.0.0.1:27017/")
        append("database", "pixelgamesmc_void")
    }

    fun getWorldLobby(): String = get("world-lobby", String::class, "lobby")

    fun getConnection(): String = get("connection", String::class, "mongodb://127.0.0.1:27017/")

    fun getDatabase(): String = get("database", String::class, "pixelgamesmc_void")

    fun isLocal(): Boolean = get("local", Boolean::class, true)
}