package eu.pixelgamesmc.void

import eu.pixelgamesmc.void.command.SpawnCommand
import eu.pixelgamesmc.void.database.PixelDatabase
import eu.pixelgamesmc.void.command.ToplistCommand
import eu.pixelgamesmc.void.command.WorldCommand
import eu.pixelgamesmc.void.configuration.ServerConfiguration
import eu.pixelgamesmc.void.database.Credentials
import eu.pixelgamesmc.void.database.collection.DatabasePlayerCollection
import eu.pixelgamesmc.void.listener.PlayerListener
import eu.pixelgamesmc.void.utils.PREFIX
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.SpawnerSpawnEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.plugin.java.JavaPlugin
import org.litote.kmongo.getCollection
import java.util.*

class Void: JavaPlugin() {

    companion object {
        lateinit var INSTANCE: Void
            private set
    }

    val locations = hashMapOf<UUID, Location>()
    val away = Collections.synchronizedList(mutableListOf<UUID>())

    override fun onEnable() {
        INSTANCE = this

        ServerConfiguration.load()

        PixelDatabase.connect(Credentials(
            Credentials.Mongo(ServerConfiguration.isLocal(), ServerConfiguration.getConnection(), ServerConfiguration.getDatabase()),
            Credentials.Redis(true, "", 2324, "", "")))

        PixelDatabase.registerCollection { mongoDatabase ->
            DatabasePlayerCollection(mongoDatabase.getCollection())
        }

        val world: World = Bukkit.getWorld(ServerConfiguration.getWorldLobby()) ?: run {
            Bukkit.createWorld(WorldCreator(ServerConfiguration.getWorldLobby())) ?: error("Spawn could not be loaded")
        }

        world.difficulty = Difficulty.PEACEFUL
        world.time = 1000
        world.clearWeatherDuration = Int.MAX_VALUE

        Bukkit.getPluginManager().registerEvents(PlayerListener(), this)
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler(priority = EventPriority.HIGHEST)
            fun entitySpawn(event: EntitySpawnEvent) {
                if (event.entity is Player) {
                    return
                }
                event.isCancelled = event.location.world.name == ServerConfiguration.getWorldLobby()
            }

            @EventHandler(priority = EventPriority.HIGHEST)
            fun spawnerSpawn(event: SpawnerSpawnEvent) {
                if (event.entity is Player) {
                    return
                }
                event.isCancelled = event.location.world.name == ServerConfiguration.getWorldLobby()
            }

            @EventHandler(priority = EventPriority.HIGHEST)
            fun changeWeather(event: WeatherChangeEvent) {
                if (event.world.name == ServerConfiguration.getWorldLobby()) {
                    event.isCancelled = true
                }
            }
        }, this)

        getCommand("world")?.setExecutor(WorldCommand()) ?: error("Could not load world command")
        getCommand("toplist")?.setExecutor(ToplistCommand()) ?: error("Could not load toplist command")
        getCommand("spawn")?.setExecutor(SpawnCommand()) ?: error("Could not load spawn command")

        val scheduler = Bukkit.getScheduler()

        scheduler.runTaskTimer(this, Runnable {
            for (player in Bukkit.getOnlinePlayers()) {
                val uniqueId = player.uniqueId
                if (locations[uniqueId] != null) {
                    if (locations[uniqueId] == player.location) {
                        away.add(uniqueId)
                        player.sendMessage(PREFIX.append(Component.text("Du bist nun AFK", NamedTextColor.RED)))
                    }
                }
                locations[uniqueId] = player.location
            }
        }, 0, 20*60*5)

        var next = 0

        scheduler.runTaskTimer(this, Runnable {
            val prefix = Component.text("Info", NamedTextColor.GREEN, TextDecoration.BOLD)
                .append(Component.text(" » ", NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
            for (player in Bukkit.getOnlinePlayers()) {
                if (next == 0) {
                    player.sendMessage(
                        prefix
                            .append(Component.text("Wenn du runterfällst, behältst du alle ", NamedTextColor.GRAY))
                            .append(Component.text("Items", NamedTextColor.YELLOW))
                    )
                    next = 1
                } else if (next == 1) {
                    player.sendMessage(
                        prefix
                            .append(Component.text("Du erhälst alle ", NamedTextColor.GRAY))
                            .append(Component.text("30 Sekunden ", NamedTextColor.YELLOW))
                            .append(Component.text("ein ", NamedTextColor.GRAY))
                            .append(Component.text("Item", NamedTextColor.YELLOW))
                    )
                    next = 2
                } else if (next == 2) {
                    player.sendMessage(
                        prefix
                            .append(Component.text("Mit ", NamedTextColor.GRAY))
                            .append(Component.text("/world invite <name> ", NamedTextColor.YELLOW))
                            .append(Component.text("kannst du Spieler auf deine Insel einladen", NamedTextColor.GRAY))
                    )
                    next = 0
                }
            }
        }, 0, 20*60*10)
    }

    override fun onDisable() {
        for (world in Bukkit.getWorlds()) {
            if (!world.isAutoSave) {
                world.save()
            }
        }
    }
}