package eu.pixelgamesmc.void.command

import eu.pixelgamesmc.void.configuration.ServerConfiguration
import eu.pixelgamesmc.void.database.DatabasePlayer
import eu.pixelgamesmc.void.database.collection.DatabasePlayerCollection
import eu.pixelgamesmc.void.utils.PREFIX
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SpawnCommand: CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            return false
        }
        val world = (Bukkit.getWorld(ServerConfiguration.getWorldLobby())
            ?: throw RuntimeException("Spawn not found"))
        sender.teleport(world.spawnLocation)
        return false
    }
}