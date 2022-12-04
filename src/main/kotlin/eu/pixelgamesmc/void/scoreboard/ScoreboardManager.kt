package eu.pixelgamesmc.void.scoreboard

import eu.pixelgamesmc.void.database.DatabasePlayer
import eu.pixelgamesmc.void.database.collection.PlayerCollection
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot

object ScoreboardManager {

    fun createScoreboard(player: Player) {
        val players = PlayerCollection.getPlayers()
        val firstPlayer = players.getOrNull(0)
        val secondPlayer = players.getOrNull(1)
        val thirdPlayer = players.getOrNull(2)

        unsafe_createScoreboard(player, firstPlayer, secondPlayer, thirdPlayer, players)
    }

    fun createScoreboards() {
        val players = PlayerCollection.getPlayers()
        val firstPlayer = players.getOrNull(0)
        val secondPlayer = players.getOrNull(1)
        val thirdPlayer = players.getOrNull(2)

        for (player in Bukkit.getOnlinePlayers()) {
            unsafe_createScoreboard(player, firstPlayer, secondPlayer, thirdPlayer, players)
        }
    }

    private fun unsafe_createScoreboard(player: Player, firstPlayer: DatabasePlayer?, secondPlayer: DatabasePlayer?, thirdPlayer: DatabasePlayer?, players: List<DatabasePlayer>) {
        val scoreboard = Bukkit.getScoreboardManager().newScoreboard
        val objective = scoreboard.registerNewObjective("sidebar", Criteria.DUMMY, Component.text("\uEff1"))
        objective.displaySlot = DisplaySlot.SIDEBAR
        objective.getScore("§c").score = 7
        objective.getScore("§d").score = 3
        objective.getScore("§e").score = 1
        objective.getScore("\uEff2 §7§l#1 ${firstPlayer?.name ?: "?"} §e${firstPlayer?.items ?: ""}").score = 6
        objective.getScore("\uEff3 §7§l#2 ${secondPlayer?.name ?: "?"} §e${secondPlayer?.items ?: ""}").score = 5
        objective.getScore("\uEff4 §7§l#3 ${thirdPlayer?.name ?: "?"} §e${thirdPlayer?.items ?: ""}").score = 4
        val databasePlayer = players.find { it.uuid == player.uniqueId } ?: return
        objective.getScore("\uEff5 §7Deine Platzierung: §7§l#${players.indexOf(databasePlayer) + 1} §e${databasePlayer.items}").score = 2
        objective.getScore("\uEff6 §7Deine Tode: §f§l${databasePlayer.deaths}").score = 0
        player.scoreboard = scoreboard
    }
}