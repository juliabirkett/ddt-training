package com.store

import com.github.michaelbull.result.*

interface StorageRepository {
    fun findAll(): List<Product>
    fun save(product: Product)
}

class StoreAppHub(
    private val storage: StorageRepository
) {
    fun catalog(): List<Product> = storage.findAll()
    fun buy(productId: Int): Result<Product, ErrorCode> =
        storage.findAll().find { it.id == productId }
            .toResultOr { ProductNotFound("ERROR! Product with id $productId wasn't found in the catalog") }
            .map { product ->
                if (product.quantity <= 0) return Err(ProductIsOutOfStock("ERROR! Product with id $productId is out of stock at the moment"))
                else Ok(product)

                storage.save(product.reduceStock())
                product
            }

    fun register(product: Product) {
        storage.save(product)
    }

    fun logInAsAManager(password: String): Result<Unit, NotAuthenticatedAsManager> =
        if (password == "admin123") Ok(Unit) else Err(NotAuthenticatedAsManager())
}

sealed interface ErrorCode {
    val message: String
}
data class NotAuthenticatedAsManager(override val message: String = "Manager is not authenticated") : ErrorCode
data class ProductNotFound(override val message: String) : ErrorCode
data class ProductIsOutOfStock(override val message: String) : ErrorCode
