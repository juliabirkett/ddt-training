package com.store

interface StorageRepository {
    fun findAll(): List<Product>
    fun save(product: Product)
}

class StoreAppHub(
    private val storage: StorageRepository
) {
    fun catalog(): List<Product> = storage.findAll()
    fun buy(productId: Int): Product {
        val product = storage.findAll().find { it.id == productId }
            ?: throw ProductNotFound("ERROR! Product with id $productId wasn't found in the catalog")

        if (product.quantity <= 0)
            throw ProductIsOutOfStock("ERROR! Product with id $productId is out of stock at the moment")

        storage.save(product.reduceStock())

        return product
    }

    fun register(product: Product) {
        storage.save(product)
    }

    fun logInAsAManager(password: String) {
        if (password != "admin123") throw NotAuthenticatedAsManager
    }
}

object NotAuthenticatedAsManager : Throwable()
data class ProductNotFound(override val message: String) : Throwable()
data class ProductIsOutOfStock(override val message: String) : Throwable()
