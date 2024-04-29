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
            .toResultOr { ProductNotFound }
            .map { product ->
                if (product.quantity <= 0) return Err(ProductIsOutOfStock)
                if (product.isForAdultsOnly()) return Err(ProductForAdultsOnly)
                else Ok(product)

                storage.save(product.reduceStock())
                product
            }

    fun register(product: Product) {
        storage.save(product)
    }

    fun logInAsAManager(password: String): Result<Unit, NotAuthenticatedAsManager> =
        if (password == "admin123") Ok(Unit) else Err(NotAuthenticatedAsManager)
}

sealed interface ErrorCode {
    val message: String
}
data object NotAuthenticatedAsManager : ErrorCode {
    override val message = "Manager is not authenticated"
}
data object ProductNotFound : ErrorCode {
    override val message = "ERROR! Product wasn't found in the catalog"
}
data object ProductIsOutOfStock : ErrorCode {
    override val message = "ERROR! Product is out of stock at the moment"
}
data object ProductForAdultsOnly: ErrorCode {
    override val message = "ERROR! Product can only be sold to adults"
}
