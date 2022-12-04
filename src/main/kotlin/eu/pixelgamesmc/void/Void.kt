package eu.pixelgamesmc.void

import eu.pixelgamesmc.void.command.WorldCommand
import eu.pixelgamesmc.void.configuration.ServerConfiguration
import eu.pixelgamesmc.void.database.Database
import eu.pixelgamesmc.void.listener.PlayerListener
import eu.pixelgamesmc.void.utils.PREFIX
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.*
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

class Void: JavaPlugin() {

    companion object {
        lateinit var INSTANCE: Void
            private set
    }

    val locations = hashMapOf<UUID, Location>()
    val away = mutableListOf<UUID>()

    override fun onEnable() {
        INSTANCE = this

        ServerConfiguration.load()
        Database.connect()

        val world: World = Bukkit.getWorld(ServerConfiguration.getWorldLobby()) ?: run {
            Bukkit.createWorld(WorldCreator(ServerConfiguration.getWorldLobby())) ?: error("Spawn could not be loaded")
        }

        world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        world.time = 1000

        Bukkit.getPluginManager().registerEvents(PlayerListener(), this)

        getCommand("world")?.setExecutor(WorldCommand()) ?: error("Could not load world command")

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

        scheduler.runTaskTimer(this, Runnable {
            val prefix = Component.text("Info", NamedTextColor.GREEN, TextDecoration.BOLD)
                .append(Component.text(" » ", NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
            for (player in Bukkit.getOnlinePlayers()) {
                player.sendMessage(
                    prefix
                        .append(Component.text("Wenn du runterfällst, behältst du alle ", NamedTextColor.GRAY))
                        .append(Component.text("Items", NamedTextColor.YELLOW))
                )
                player.sendMessage(
                    prefix
                        .append(Component.text("Dieser ", NamedTextColor.GRAY))
                        .append(Component.text("Server ", NamedTextColor.GREEN))
                        .append(Component.text("wird nur solange genutzt, bis die ", NamedTextColor.GRAY))
                        .append(Component.text("Wartungsarbeiten ", NamedTextColor.RED))
                        .append(Component.text("zu Ende sind", NamedTextColor.GRAY))
                )
                player.sendMessage(
                    prefix
                        .append(Component.text("Du erhälst alle ", NamedTextColor.GRAY))
                        .append(Component.text("30 Sekunden ", NamedTextColor.YELLOW))
                        .append(Component.text("ein ", NamedTextColor.GRAY))
                        .append(Component.text("Item", NamedTextColor.YELLOW))
                )
            }
        }, 0, 20*60*10)
    }

    override fun onDisable() {
        for (world in Bukkit.getWorlds()) {
            if (!world.isAutoSave) {
                world.save()
            }
        }

        Database.disconnect()
    }
}