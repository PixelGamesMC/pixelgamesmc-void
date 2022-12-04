package eu.pixelgamesmc.void.scoreboard

import eu.pixelgamesmc.void.database.DatabasePlayer
import eu.pixelgamesmc.void.database.collection.PlayerCollection
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot

object ScoreboardManager {

    fun updateTablists() {
        val players = Bukkit.getOnlinePlayers()
        for (player in players) {
            player.sendPlayerListHeaderAndFooter(
                Component.text("\uEFF1")
                    .append(Component.text("\nDein ", NamedTextColor.GRAY))
                    .append(Component.text("Minecraft-Netzwerk", NamedTextColor.GREEN))
                    .append(Component.text(" » ", NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
                    .append(Component.text("${players.size}", NamedTextColor.DARK_AQUA))
                    .append(Component.text("/", NamedTextColor.DARK_GRAY))
                    .append(Component.text("100", NamedTextColor.DARK_AQUA))
                    .append(Component.text("\n\nAktueller Server", NamedTextColor.GRAY))
                    .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                    .append(Component.text("Random-Items-1\n", NamedTextColor.GREEN, TextDecoration.BOLD)),
                Component.text("\nDiscord", NamedTextColor.GRAY)
                    .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                    .append(Component.text("discord.gg/CjfY7HKG4e", NamedTextColor.GREEN))
            )
        }
    }

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
        objective.getScore("\uEff2 §7#§7§l1 ${firstPlayer?.name ?: "?"} §e${firstPlayer?.items ?: ""}").score = 6
        objective.getScore("\uEff3 §7#§7§l2 ${secondPlayer?.name ?: "?"} §e${secondPlayer?.items ?: ""}").score = 5
        objective.getScore("\uEff4 §7#§7§l3 ${thirdPlayer?.name ?: "?"} §e${thirdPlayer?.items ?: ""}").score = 4
        val databasePlayer = players.find { it.uuid == player.uniqueId } ?: return
        objective.getScore("\uEff5 §7Deine Platzierung: §7#§7§l${players.indexOf(databasePlayer) + 1} §e${databasePlayer.items}").score = 2
        objective.getScore("\uEff6 §7Deine Tode: §f§l${databasePlayer.deaths}").score = 0
        player.scoreboard = scoreboard
    }
}