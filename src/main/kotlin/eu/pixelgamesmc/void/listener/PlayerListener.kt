package eu.pixelgamesmc.void.listener

import eu.pixelgamesmc.void.Void
import eu.pixelgamesmc.void.configuration.ServerConfiguration
import eu.pixelgamesmc.void.database.PixelDatabase
import eu.pixelgamesmc.void.database.collection.DatabasePlayerCollection
import eu.pixelgamesmc.void.database.collection.PlayerCollection
import eu.pixelgamesmc.void.scoreboard.ScoreboardManager
import eu.pixelgamesmc.void.utils.PREFIX
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

class PlayerListener: Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun asyncPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        PixelDatabase.getCollections(PlayerCollection::class).forEach { playerCollection ->
            playerCollection.playerLogin(event.uniqueId, event.name, event.playerProfile.textures.skin.toString())
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun playerJoin(event: PlayerJoinEvent) {
        val player = event.player

        event.joinMessage(PREFIX.append(Component.text(player.name, NamedTextColor.YELLOW))
            .append(Component.text(" hat das Spiel betreten", NamedTextColor.GRAY)))

        val world = Bukkit.getWorld(ServerConfiguration.getWorldLobby())

        if (world != null) {
            val location = world.spawnLocation.add(0.5, 0.0, 0.5)
            player.teleport(location)
        }

        ScoreboardManager.updateScoreboard(player)

        val scheduler = Bukkit.getScheduler()
        var time = 0
        scheduler.runTaskTimer(Void.INSTANCE, { first ->
            if (!player.isOnline) {
                first.cancel()
            }
            if (!Void.INSTANCE.away.contains(player.uniqueId)) {
                if (player.world.name != ServerConfiguration.getWorldLobby()) {
                    if (time == 0) {
                        val materials = Material.values().filter { !it.isAir }
                        val inventory = player.inventory
                        val contents = inventory.contents
                        inventory.addItem(ItemStack(materials[Random.nextInt(1, materials.size)]))

                        if (!contents.contentEquals(inventory.contents)) {
                            DatabasePlayerCollection.getInstance().addItem(player.uniqueId)
                            ScoreboardManager.updateScoreboards()
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

        ScoreboardManager.removeScoreboard(player)

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
        DatabasePlayerCollection.getInstance().addDeath(player.uniqueId)

        event.keepInventory = true
        event.keepLevel = true
        event.drops.clear()

        event.deathMessage(PREFIX.append(Component.text(player.name, NamedTextColor.YELLOW))
            .append(Component.text(" ist gestorben", NamedTextColor.GRAY)))

        ScoreboardManager.updateScoreboard(player)
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

        if (event.player.world.name == ServerConfiguration.getWorldLobby()) {
            if (event.to.y <= 40) {
                event.player.teleport(event.player.world.spawnLocation)
            }
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

    @EventHandler(priority = EventPriority.HIGHEST)
    fun drop(event: PlayerDropItemEvent) {
        if (event.player.world.name == ServerConfiguration.getWorldLobby()) {
            event.isCancelled = true
        }
    }
}