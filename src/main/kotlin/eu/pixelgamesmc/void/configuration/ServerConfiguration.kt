package eu.pixelgamesmc.void.configuration

object ServerConfiguration: Configuration("config") {

    override fun create() {
        append("world-lobby", "lobby")
        append("local", true)
        append("srv", true)
        append("hostname", "127.0.0.1")
        append("port", 27017)
        append("database", "pixelgamesmc")
        append("username", "username")
        append("password", "password")
    }

    fun getWorldLobby(): String = get("world-lobby", String::class, "lobby")

    fun isLocal(): Boolean = get("local", Boolean::class, true)

    fun isSrv(): Boolean = get("srv", Boolean::class, true)

    fun getHostname(): String = get("hostname", String::class, "127.0.0.1")

    fun getPort(): Int = get("port", Int::class, 27017)

    fun getDatabase(): String = get("database", String::class, "pixelgamesmc")

    fun getUsername(): String = get("username", String::class, "username")

    fun getPassword(): String = get("password", String::class, "password")
}