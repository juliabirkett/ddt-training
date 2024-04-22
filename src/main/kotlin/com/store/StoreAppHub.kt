package com.store

interface StorageRepository {
    fun findAll(): List<Product>
    fun save(product: Product)
}

class StoreAppHub(
    private val storage: StorageRepository
) {
    fun catalog(): List<Product> = storage.findAll()
    fun buy(productId: Int) = storage.findAll().find { it.id == productId }
        ?: throw ProductNotFound("ERROR! Product with id $productId wasn't found in the catalog")

    fun register(product: Product) {
        storage.save(product)
    }
}

class ProductNotFound(override val message: String) : Throwable()
