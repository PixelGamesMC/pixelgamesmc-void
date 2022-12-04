package eu.pixelgamesmc.void.configuration

import com.google.gson.JsonObject
import eu.pixelgamesmc.void.Void
import eu.pixelgamesmc.void.utils.GSON
import java.nio.file.Files
import java.nio.file.Path
import kotlin.reflect.KClass

abstract class Configuration(name: String) {

    private val path: Path = Path.of("plugins/${Void.INSTANCE.name}/$name.json")

    private lateinit var container: JsonObject

    fun append(key: String, value: Any) {
        container.add(key, GSON.toJsonTree(value))
        save()
    }

    fun <T: Any> get(key: String, type: KClass<T>): T = GSON.fromJson(container.get(key), type.java)

    fun <T: Any> get(key: String, type: KClass<T>, default: T): T {
        if (!contains(key)) {
            append(key, default)
            return default
        }
        return get(key, type)
    }

    fun contains(key: String): Boolean = container.has(key)

    abstract fun create()

    fun load() {
        if (Files.exists(path)) {
            container = GSON.fromJson(Files.newBufferedReader(path), JsonObject::class.java)
        } else {
            container = JsonObject()
            create()
        }
    }

    private fun save() {
        val writer = Files.newBufferedWriter(path)
        writer.write(GSON.toJson(container))
        writer.close()
    }
}