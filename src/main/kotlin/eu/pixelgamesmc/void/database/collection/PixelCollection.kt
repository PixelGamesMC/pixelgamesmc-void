package eu.pixelgamesmc.void.database.collection

import com.mongodb.client.MongoCollection

open class PixelCollection<T: Any>(
    protected val collection: MongoCollection<T>
)