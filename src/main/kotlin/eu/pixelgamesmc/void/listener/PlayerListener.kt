package eu.pixelgamesmc.void.listener

import eu.pixelgamesmc.void.Void
import eu.pixelgamesmc.void.configuration.ServerConfiguration
import eu.pixelgamesmc.void.database.collection.PlayerCollection
import eu.pixelgamesmc.void.scoreboard.ScoreboardManager
import eu.pixelgamesmc.void.utils.PREFIX
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import java.util.UUID
import kotlin.random.Random

class PlayerListener: Listener {

    private val away = hashMapOf<UUID, Int>()

    @EventHandler
    fun playerJoin(event: PlayerJoinEvent) {
        val player = event.player

        event.joinMessage(PREFIX.append(Component.text(player.name, NamedTextColor.YELLOW))
            .append(Component.text(" hat das Spiel betreten", NamedTextColor.GRAY)))

        val world = Bukkit.getWorld(ServerConfiguration.getWorldLobby())

        if (world != null) {
            val location = world.spawnLocation.add(0.5, 0.0, 0.5)
            location.yaw = 90f
            location.pitch = 0f
            player.teleport(location)
        }

        PlayerCollection.playerJoin(player.uniqueId, player.name)
        ScoreboardManager.createScoreboard(player)
        ScoreboardManager.updateTablists()

        val scheduler = Bukkit.getScheduler()
        var time = 0
        scheduler.runTaskTimer(Void.INSTANCE, { first ->
            if (!player.isOnline) {
                first.cancel()
            }
            if (!Void.INSTANCE.away.contains(player.uniqueId)) {
                if (player.world.name != ServerConfiguration.getWorldLobby()) {
                    if (time == 0) {
                        val materials = Material.values()
                        val inventory = player.inventory
                        val contents = inventory.contents
                        inventory.addItem(ItemStack(materials[Random.nextInt(materials.size)]))

                        if (!contents.contentEquals(inventory.contents)) {
                            PlayerCollection.addItem(player.uniqueId)
                            ScoreboardManager.createScoreboards()
                        }
                        time = 30
                    }
                    player.sendActionBar(
                        Component.text("Nächstes Item in: ", NamedTextColor.GRAY)
                            .append(Component.text("${time--} Sek", NamedTextColor.YELLOW))
                    )
                }
            }
        }, 0, 20)
    }

    @EventHandler
    fun playerQuit(event: PlayerQuitEvent) {
        val player = event.player
        val uniqueId = player.uniqueId

        ScoreboardManager.updateTablists()

        event.quitMessage(PREFIX.append(Component.text(player.name, NamedTextColor.YELLOW))
            .append(Component.text(" hat das Spiel verlassen", NamedTextColor.GRAY)))

        if (Void.INSTANCE.locations.contains(uniqueId)) {
            Void.INSTANCE.locations.remove(uniqueId)
        }

        if (Void.INSTANCE.away.contains(uniqueId)) {
            Void.INSTANCE.away.remove(uniqueId)
        }

        val world = Bukkit.getWorld(uniqueId.toString())
        if (world != null) {
            if (world.playerCount == 1) {
                Bukkit.unloadWorld(uniqueId.toString(), true)
            }
        }
    }

    @EventHandler
    fun playerDeath(event: PlayerDeathEvent) {
        val player = event.player
        PlayerCollection.addDeath(player.uniqueId)

        event.keepInventory = true
        event.keepLevel = true

        event.deathMessage(PREFIX.append(Component.text(player.name, NamedTextColor.YELLOW))
            .append(Component.text(" ist gestorben", NamedTextColor.GRAY)))
    }

    @EventHandler
    fun playerRespawn(event: PlayerRespawnEvent) {
        event.respawnLocation = event.player.location.world.spawnLocation
    }

    @EventHandler
    fun moveListener(event: PlayerMoveEvent) {
        val uniqueId = event.player.uniqueId

        if (Void.INSTANCE.away.contains(uniqueId)) {
            Void.INSTANCE.away.remove(uniqueId)
            event.player.sendMessage(PREFIX.append(Component.text("Du bist nun nicht mehr AFK", NamedTextColor.GRAY)))
        }
    }

    @EventHandler
    fun entityDamage(event: EntityDamageEvent) {
        if (event.entity.world.name == ServerConfiguration.getWorldLobby()) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun blockBreak(event: BlockBreakEvent) {
        if (event.block.world.name == ServerConfiguration.getWorldLobby()) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun blockPlace(event: BlockPlaceEvent) {
        if (event.block.world.name == ServerConfiguration.getWorldLobby()) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun playerFood(event: FoodLevelChangeEvent) {
        if (event.entity.world.name == ServerConfiguration.getWorldLobby()) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun interact(event: PlayerInteractEvent) {
        if (event.player.world.name == ServerConfiguration.getWorldLobby()) {
            event.isCancelled = true
        }
    }
}