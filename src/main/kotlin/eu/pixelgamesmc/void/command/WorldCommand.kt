package eu.pixelgamesmc.void.command

import eu.pixelgamesmc.void.Void
import eu.pixelgamesmc.void.configuration.ServerConfiguration
import eu.pixelgamesmc.void.database.collection.DatabasePlayerCollection
import eu.pixelgamesmc.void.utils.PREFIX
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.*
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.generator.ChunkGenerator
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

class WorldCommand: CommandExecutor {

    private val invites = hashMapOf<UUID, MutableList<UUID>>()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (args.isEmpty() || args.size > 2) {
                sender.sendMessage(PREFIX.append(Component.text("Falsche Argumente", NamedTextColor.RED)))
                return false
            }

            if (args[0].equals("create", ignoreCase = true)) {
                var world: World? = Bukkit.getWorld(sender.uniqueId.toString())

                if (world == null) {
                    sender.sendMessage(PREFIX.append(Component.text("Welt wird geladen...", NamedTextColor.GRAY)))
                    world = loadWorld(sender.uniqueId)

                    if (world != null) {
                        sender.teleport(world.spawnLocation)
                    } else {
                        sender.sendMessage(PREFIX.append(Component.text("Fehler beim Laden der Welt", NamedTextColor.RED)))
                    }
                } else {
                    sender.sendMessage(PREFIX.append(Component.text("Welt existiert bereits", NamedTextColor.RED)))
                    sender.sendMessage(PREFIX.append(Component.text("Du wirst zu deiner Welt teleportiert", NamedTextColor.GRAY)))
                    sender.performCommand("world teleport")
                }
            } else if (args[0].equals("teleport", ignoreCase = true) || args[0].equals("tp", ignoreCase = true)) {
                if (args.size == 1) {
                    val world: World? = Bukkit.getWorld(sender.uniqueId.toString())

                    if (world == null) {
                        sender.sendMessage(PREFIX.append(Component.text("Welt ist noch nicht geladen", NamedTextColor.RED)))
                        sender.performCommand("world create")
                    } else {
                        sender.teleport(world.spawnLocation)
                    }
                } else {
                    val name = args[1]

                    if (name.equals(sender.name, ignoreCase = true)) {
                        sender.performCommand("world teleport")
                        return false
                    }

                    val uuid = DatabasePlayerCollection.getInstance().getUuid(name)

                    if (uuid == null) {
                        sender.sendMessage(PREFIX.append(Component.text("Dieser Spieler war noch nie online", NamedTextColor.RED)))
                        return false
                    }

                    if (!DatabasePlayerCollection.getInstance().getFriends(uuid).contains(sender.uniqueId.toString()) && !sender.hasPermission("pixelgamesmc.void.tp")) {
                        sender.sendMessage(PREFIX.append(Component.text("Du hast keine Berechtigung", NamedTextColor.RED)))
                        return false
                    }

                    var world: World? = Bukkit.getWorld(uuid.toString())

                    if (world == null) {
                        if (!Files.exists(Path.of(uuid.toString()))) {
                            sender.sendMessage(PREFIX.append(Component.text("Dieser Spieler hat keine Welt")))
                            return false
                        }
                        sender.sendMessage(PREFIX.append(Component.text("Welt wird geladen...", NamedTextColor.GRAY)))
                        world = loadWorld(uuid)

                        if (world != null) {
                            sender.teleport(world.spawnLocation)
                        } else {
                            sender.sendMessage(PREFIX.append(Component.text("Fehler beim Laden der Welt", NamedTextColor.RED)))
                        }
                    } else {
                        sender.teleport(world.spawnLocation)
                    }
                }
            } else if (args[0].equals("remove", ignoreCase = true)) {
                if (args.size != 2) {
                    sender.sendMessage(PREFIX.append(Component.text("Falsche Argumente", NamedTextColor.RED)))
                    return false
                }

                val name = args[1]
                val player = Bukkit.getPlayer(name)

                if (player != null) {
                    if (!DatabasePlayerCollection.getInstance().getFriends(sender.uniqueId).contains(player.uniqueId.toString())) {
                        sender.sendMessage(PREFIX.append(Component.text("Dieser Spieler hat keine Berechtigung", NamedTextColor.RED)))
                        return false
                    }
                    DatabasePlayerCollection.getInstance().removeFriend(sender.uniqueId, player.uniqueId)

                    if (player.world.name == sender.uniqueId.toString()) {
                        val world = Bukkit.getWorld(ServerConfiguration.getWorldLobby())

                        val component = PREFIX.append(
                            Component.text(sender.name, NamedTextColor.YELLOW)
                                .append(Component.text(" hat dich entfernt", NamedTextColor.RED))
                        )

                        if (world != null) {
                            player.teleport(world.spawnLocation)
                            player.sendMessage(component)
                        } else {
                            player.kick()
                            player.sendMessage(component)
                        }
                    }
                } else {
                    val uuid = DatabasePlayerCollection.getInstance().getUuid(name)

                    if (uuid == null) {
                        sender.sendMessage(PREFIX.append(Component.text("Dieser Spieler war noch nie online", NamedTextColor.RED)))
                        return false
                    }

                    if (!DatabasePlayerCollection.getInstance().getFriends(uuid).contains(sender.uniqueId.toString())) {
                        sender.sendMessage(PREFIX.append(Component.text("Dieser Spieler hat keine Berechtigung", NamedTextColor.RED)))
                        return false
                    }

                    DatabasePlayerCollection.getInstance().removeFriend(sender.uniqueId, uuid)
                }
                sender.sendMessage(PREFIX.append(Component.text(name, NamedTextColor.YELLOW))
                    .append(Component.text(" wurde entfernt", NamedTextColor.GRAY)))
            } else if (args[0].equals("invite", ignoreCase = true) || args[0].equals("accept", ignoreCase = true) || args[0].equals("deny", ignoreCase = true)) {
                if (args.size != 2) {
                    sender.sendMessage(PREFIX.append(Component.text("Falsche Argumente", NamedTextColor.RED)))
                    return false
                }
                val name = args[1]

                if (sender.name.equals(name, ignoreCase = true)) {
                    sender.sendMessage(PREFIX.append(Component.text("Du kannst dich nicht selbst einladen", NamedTextColor.RED)))
                    return false
                }

                val player = Bukkit.getPlayer(args[1])
                if (player == null) {
                    sender.sendMessage(PREFIX.append(Component.text("Dieser Spieler ist nicht online", NamedTextColor.RED)))
                    return false
                }

                if (args[0].equals("invite", ignoreCase = true)) {
                    if (DatabasePlayerCollection.getInstance().getFriends(sender.uniqueId).contains(player.uniqueId.toString())) {
                        sender.sendMessage(Component.text("Dieser Spieler ist schon Teil deiner Insel", NamedTextColor.RED))
                        return false
                    }

                    invites[sender.uniqueId]?.add(player.uniqueId) ?: run { invites[sender.uniqueId] = mutableListOf(player.uniqueId) }

                    sender.sendMessage(PREFIX.append(Component.text("Einladung verschickt", NamedTextColor.GRAY)))

                    player.sendMessage(
                        PREFIX.append(Component.text("Du hast eine Einladung von ", NamedTextColor.GRAY))
                            .append(Component.text(sender.name, NamedTextColor.YELLOW))
                            .append(Component.text(" erhalten. Möchtest du diese Anfrage ", NamedTextColor.GRAY))
                            .append(Component.text("[annehmen]", NamedTextColor.GREEN, TextDecoration.BOLD)
                                .clickEvent(ClickEvent.runCommand("/world accept ${sender.name}")))
                            .append(Component.text(" oder ", NamedTextColor.GRAY))
                            .append(Component.text("[ablehnen]", NamedTextColor.RED, TextDecoration.BOLD)
                                .clickEvent(ClickEvent.runCommand("/world deny ${sender.name}")))
                    )

                    Bukkit.getScheduler().runTaskLater(Void.INSTANCE, Runnable {
                        if (invites[player.uniqueId] != null && invites[player.uniqueId]!!.contains(player.uniqueId)) {
                            if (sender.isOnline) {
                                sender.sendMessage(
                                    PREFIX.append(Component.text("Deine Einladung an ", NamedTextColor.GRAY))
                                    .append(Component.text(player.name, NamedTextColor.YELLOW))
                                    .append(Component.text(" ist abgelaufen", NamedTextColor.GRAY)))
                            }

                            if (player.isOnline) {
                                player.sendMessage(
                                    PREFIX.append(Component.text("Deine Einladung von ", NamedTextColor.GRAY))
                                    .append(Component.text(sender.name, NamedTextColor.YELLOW))
                                    .append(Component.text(" ist abgelaufen", NamedTextColor.GRAY)))
                            }

                            invites[player.uniqueId]!!.remove(player.uniqueId)
                        }
                    }, 20*30)
                } else {
                    if (invites[player.uniqueId] != null && invites[player.uniqueId]!!.contains(sender.uniqueId)) {
                        invites[player.uniqueId]!!.remove(sender.uniqueId)

                        if (args[0].equals("accept", ignoreCase = true)) {
                            val world = Bukkit.getWorld(player.uniqueId.toString()) ?: return false

                            DatabasePlayerCollection.getInstance().addFriend(player.uniqueId, sender.uniqueId)

                            sender.teleport(world.spawnLocation)
                            player.sendMessage(
                                PREFIX.append(Component.text(sender.name, NamedTextColor.YELLOW))
                                .append(Component.text(" hat deine Einladung angenommen", NamedTextColor.GRAY))
                            )
                        } else {
                            sender.sendMessage(PREFIX.append(Component.text("Du hast die Einladung abgelehnt", NamedTextColor.GRAY)))
                            player.sendMessage(
                                PREFIX.append(Component.text(sender.name, NamedTextColor.YELLOW))
                                .append(Component.text(" hat deine Einladung abgelehnt", NamedTextColor.GRAY))
                            )
                        }
                    } else {
                        sender.sendMessage(PREFIX.append(Component.text("Du hast keine Einladung von dem Spieler erhalten oder sie ist bereits abgelaufen", NamedTextColor.RED)))
                    }
                }
            }
        }
        return false
    }

    private fun loadWorld(uuid: UUID): World? {
        val worldCreator = WorldCreator(uuid.toString())
        worldCreator.environment(World.Environment.NORMAL)
        worldCreator.type(WorldType.NORMAL)
        worldCreator.generator(object : ChunkGenerator() { })
        val world = Bukkit.createWorld(worldCreator) ?: return null
        world.difficulty = Difficulty.NORMAL

        world.spawnLocation = Location(world, 0.0, 42.0, 0.0)
        world.getBlockAt(world.spawnLocation.subtract(0.0, 2.0, 0.0)).type = Material.BEDROCK
        return world
    }
}