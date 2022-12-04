package eu.pixelgamesmc.void.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import eu.pixelgamesmc.void.configuration.ServerConfiguration
import eu.pixelgamesmc.void.database.collection.PlayerCollection
import org.bson.UuidRepresentation
import org.litote.kmongo.KMongo
import org.litote.kmongo.util.KMongoJacksonFeature

object Database {

    private lateinit var client: MongoClient
    lateinit var database: MongoDatabase
        private set

    fun connect() {
        System.setProperty("org.litote.mongo.test.mapping.service", "org.litote.kmongo.jackson.JacksonClassMappingTypeService")
        KMongoJacksonFeature.setUUIDRepresentation(UuidRepresentation.STANDARD)

        val builder = MongoClientSettings.builder()
            .uuidRepresentation(UuidRepresentation.STANDARD)

        client = if (ServerConfiguration.isLocal()) {
            KMongo.createClient(builder
                .build())
        } else {
            KMongo.createClient(builder
                .applyConnectionString(
                    if (ServerConfiguration.isSrv()) {
                        ConnectionString("mongodb+srv://${ServerConfiguration.getUsername()}:${ServerConfiguration.getPassword()}@${ServerConfiguration.getHostname()}/?retryWrites=true&w=majority")
                    } else {
                        ConnectionString("mongodb://${ServerConfiguration.getUsername()}:${ServerConfiguration.getPassword()}@${ServerConfiguration.getHostname()}:${ServerConfiguration.getPort()}/?retryWrites=true&w=majority")
                    }
                ).build())
        }
        database = client.getDatabase(ServerConfiguration.getDatabase())

        PlayerCollection.load()
    }

    fun disconnect() {
        client.close()
    }
}