package eu.pixelgamesmc.void.scoreboard

import eu.pixelgamesmc.void.database.DatabasePlayer
import eu.pixelgamesmc.void.database.collection.DatabasePlayerCollection
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

object ScoreboardManager {

    fun updateScoreboard(player: Player) {
        val players = DatabasePlayerCollection.getInstance().getPlayers()
        val firstPlayer = players.getOrNull(0)
        val secondPlayer = players.getOrNull(1)
        val thirdPlayer = players.getOrNull(2)

        unsafeUpdateScoreboard(player, firstPlayer, secondPlayer, thirdPlayer, players)
    }

    fun updateScoreboards() {
        val players = DatabasePlayerCollection.getInstance().getPlayers()
        val firstPlayer = players.getOrNull(0)
        val secondPlayer = players.getOrNull(1)
        val thirdPlayer = players.getOrNull(2)

        for (player in Bukkit.getOnlinePlayers()) {
            unsafeUpdateScoreboard(player, firstPlayer, secondPlayer, thirdPlayer, players)
        }
    }

    private val map = mutableMapOf<UUID, PixelSidebar>()

    fun removeScoreboard(player: Player) {
        map.remove(player.uniqueId)
    }

    private fun unsafeUpdateScoreboard(player: Player, firstPlayer: DatabasePlayer?, secondPlayer: DatabasePlayer?, thirdPlayer: DatabasePlayer?, players: List<DatabasePlayer>) {
        val pixelObjective = map[player.uniqueId] ?: kotlin.run {
            PixelSidebar(player, firstPlayer, secondPlayer, thirdPlayer, players)
        }
        pixelObjective.updateDisplay()
        pixelObjective.update(firstPlayer, secondPlayer, thirdPlayer, players)
    }
}