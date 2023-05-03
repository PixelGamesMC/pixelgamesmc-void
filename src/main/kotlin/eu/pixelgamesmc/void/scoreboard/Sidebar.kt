package eu.pixelgamesmc.void.scoreboard

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.*
import java.util.concurrent.atomic.AtomicInteger

open class Sidebar(name: String, private val player: Player, displayName: Component = Component.empty()) {

    private val counter = AtomicInteger()

    private val scoreboard: Scoreboard = if (player.scoreboard != Bukkit.getScoreboardManager().mainScoreboard)
        player.scoreboard
    else Bukkit.getScoreboardManager().newScoreboard
    private val objective: Objective

    private val scores: MutableMap<Int, Score> = mutableMapOf()

    init {
        scoreboard.getObjective(DisplaySlot.SIDEBAR)?.unregister()

        objective = scoreboard
            .registerNewObjective(name, "dummy", displayName).also {
                it.displaySlot = DisplaySlot.SIDEBAR
            }
    }

    fun updateDisplay() {
        player.scoreboard = scoreboard
    }

    fun updateDisplayName(component: Component) {
        objective.displayName(component)
    }

    fun updateScore(index: Int, text: String) {
        scores[index]?.resetScore()

        val score = objective.getScore("§l".repeat(counter.getAndIncrement()) + text)
        score.score = index
        scores[index] = score
    }

    fun removeScore(index: Int) {
        scores.remove(index)?.resetScore()
    }
}