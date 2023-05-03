package eu.pixelgamesmc.void.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import eu.pixelgamesmc.void.database.collection.PixelCollection
import org.bson.UuidRepresentation
import org.litote.kmongo.KMongo
import org.litote.kmongo.serialization.SerializationClassMappingTypeService
import org.litote.kmongo.util.CollectionNameFormatter
import kotlin.reflect.KClass

object PixelDatabase {

    private lateinit var mongoClient: MongoClient
    private lateinit var mongoDatabase: MongoDatabase

    private val collections: MutableList<PixelCollection<*>> = mutableListOf()

    internal fun connect(credentials: Credentials) {
        connectMongo(credentials)
    }

    internal fun disconnect() {
        mongoClient.close()
    }

    private fun connectMongo(credentials: Credentials) {
        prepareMongo()

        val settings = MongoClientSettings.builder().uuidRepresentation(UuidRepresentation.STANDARD)
        mongoClient = if (credentials.mongo.local) {
            KMongo.createClient(settings.build())
        } else {
            KMongo.createClient(settings.applyConnectionString(ConnectionString(credentials.mongo.connectionString)).build())
        }

        mongoDatabase = mongoClient.getDatabase(credentials.mongo.database)
    }

    private fun prepareMongo() {
        System.setProperty("org.litote.mongo.mapping.service", SerializationClassMappingTypeService::class.qualifiedName!!)

        CollectionNameFormatter.useSnakeCaseCollectionNameBuilder()
    }

    fun registerCollection(creator: (MongoDatabase) -> PixelCollection<*>) {
        val collection = creator.invoke(mongoDatabase)

        collections.add(collection)
    }

    fun registerCollections(creator: (MongoDatabase) -> List<PixelCollection<*>>) {
        val collections = creator.invoke(mongoDatabase)

        PixelDatabase.collections.addAll(collections)
    }

    fun <T: Any> getCollection(clazz: KClass<T>): T {
        return collections.filterIsInstance(clazz.java).single()
    }

    fun <T: Any> getCollections(clazz: KClass<T>): List<T> {
        return collections.filterIsInstance(clazz.java)
    }
}