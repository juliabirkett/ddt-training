package com.store

object InMemoryStorageRepository : StorageRepository {
    private val products = mutableListOf<Product>()

    override fun findAll(): List<Product> = products
    override fun save(product: Product) {
        products.find { it.id == product.id }?.let { existingProduct ->
            products.remove(existingProduct)
        }

        products += product
    }

    fun cleanup() {
        products.clear()
    }
}
