package eu.pixelgamesmc.void.command

import eu.pixelgamesmc.void.database.DatabasePlayer
import eu.pixelgamesmc.void.database.collection.DatabasePlayerCollection
import eu.pixelgamesmc.void.utils.PREFIX
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ToplistCommand: CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        val players = DatabasePlayerCollection.getInstance().getPlayers()

        sender.sendMessage(PREFIX.append(Component.text("Top 10", NamedTextColor.YELLOW))
            .append(Component.text(" Spieler", NamedTextColor.YELLOW)))
        for (i in 0 until 10) {
            var player: DatabasePlayer? = null
            if (players.size - 1 >= i) {
                player = players[i]
            }
            val logo = when (i) {
                0 -> "\uEFF2"
                1 -> "\uEFF3"
                2 -> "\uEFF4"
                else -> "\uEFF7"
            }
            val spacing = when (i) {
                9 -> ""
                else -> "0"
            }
            sender.sendMessage("    $logo §7#§7§l$spacing${i+1} ${player?.name ?: "?"} §e${player?.items ?: ""}")
        }
        sender.sendMessage(PREFIX.append(Component.text("Top 10", NamedTextColor.YELLOW))
            .append(Component.text(" Spieler", NamedTextColor.YELLOW)))
        return false
    }
}