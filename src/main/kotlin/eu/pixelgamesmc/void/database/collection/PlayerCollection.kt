package eu.pixelgamesmc.void.database.collection

import com.mongodb.client.MongoCollection
import eu.pixelgamesmc.void.database.Database
import eu.pixelgamesmc.void.database.DatabasePlayer
import org.litote.kmongo.*
import java.util.UUID

object PlayerCollection {

    private lateinit var collection: MongoCollection<DatabasePlayer>

    fun load() {
        collection = Database.database.getCollectionOfName("players")
    }

    fun playerJoin(uuid: UUID, name: String) {
        if (collection.find(DatabasePlayer::uuid eq uuid).first() != null) {
            collection.updateOne(DatabasePlayer::uuid eq uuid, DatabasePlayer::name setTo name)
            return
        }
        collection.insertOne(DatabasePlayer(uuid, name, 0, 0, emptyList()))
    }

    fun addItem(uuid: UUID) {
        collection.updateOne(DatabasePlayer::uuid eq uuid, inc(DatabasePlayer::items, 1))
    }

    fun addDeath(uuid: UUID) {
        collection.updateOne(DatabasePlayer::uuid eq uuid, inc(DatabasePlayer::deaths, 1))
    }

    fun getUuid(name: String): UUID? {
        return collection.find(DatabasePlayer::name eq name).first()?.uuid
    }

    fun addFriend(uuid: UUID, friend: UUID) {
        val friends = getMutableFriends(uuid)
        friends.add(friend)
        collection.updateOne(DatabasePlayer::uuid eq uuid, DatabasePlayer::friends setTo friends)
    }

    fun removeFriend(uuid: UUID, friend: UUID) {
        val friends = getMutableFriends(uuid)
        friends.remove(friend)
        collection.updateOne(DatabasePlayer::uuid eq uuid, DatabasePlayer::friends setTo friends)
    }

    fun getFriends(uuid: UUID): List<UUID> {
        return collection.find(DatabasePlayer::uuid eq uuid).first()?.friends ?: emptyList()
    }

    private fun getMutableFriends(uuid: UUID): MutableList<UUID> {
        return getFriends(uuid).toMutableList()
    }

    fun getPlayers(): List<DatabasePlayer> {
        return collection.aggregate<DatabasePlayer>(
            sort(
                descending(DatabasePlayer::items)
            )
        ).toList()
    }
}