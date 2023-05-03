package eu.pixelgamesmc.void.scoreboard

import eu.pixelgamesmc.void.database.DatabasePlayer
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class PixelSidebar(val player: Player, val firstPlayer: DatabasePlayer?, val secondPlayer: DatabasePlayer?, val thirdPlayer: DatabasePlayer?, val players: List<DatabasePlayer>): Sidebar("pixel_sidebar", player, Component.text("\uE002")) {

    init {
        updateScore(6, "\uEff2 §7#§7§l1 ${firstPlayer?.name ?: "?"} §e${firstPlayer?.items ?: ""}")
        updateScore(5, "\uEff3 §7#§7§l2 ${secondPlayer?.name ?: "?"} §e${secondPlayer?.items ?: ""}")
        updateScore(4, "\uEff4 §7#§7§l3 ${thirdPlayer?.name ?: "?"} §e${thirdPlayer?.items ?: ""}")
        val databasePlayer = players.find { it.uuid == player.uniqueId } ?: throw RuntimeException()
        updateScore(2, "\uEff5 §7Deine Platzierung: §7#§7§l${players.indexOf(databasePlayer) + 1} §e${databasePlayer.items}")
        updateScore(0, "\uEff6 §7Deine Tode: §f§l${databasePlayer.deaths}")
        updateScore(7, "§c")
        updateScore(3, "§x")
        updateScore(1, "§e")
    }

    fun update( firstPlayer: DatabasePlayer?,  secondPlayer: DatabasePlayer?,  thirdPlayer: DatabasePlayer?,  players: List<DatabasePlayer>) {
        updateScore(6, "\uEff2 §7#§7§l1 ${firstPlayer?.name ?: "?"} §e${firstPlayer?.items ?: ""}")
        updateScore(5, "\uEff3 §7#§7§l2 ${secondPlayer?.name ?: "?"} §e${secondPlayer?.items ?: ""}")
        updateScore(4, "\uEff4 §7#§7§l3 ${thirdPlayer?.name ?: "?"} §e${thirdPlayer?.items ?: ""}")
        val databasePlayer = players.find { it.uuid == player.uniqueId } ?: throw RuntimeException()
        updateScore(2, "\uEff5 §7Deine Platzierung: §7#§7§l${players.indexOf(databasePlayer) + 1} §e${databasePlayer.items}")
        updateScore(0, "\uEff6 §7Deine Tode: §f§l${databasePlayer.deaths}")
    }
}