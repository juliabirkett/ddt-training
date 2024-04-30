package com.store

import com.github.michaelbull.result.*
import java.time.LocalDate

interface StorageRepository {
    fun findAll(): List<Product>
    fun save(product: Product)
}

sealed interface UserSession
object NoSessionUser : UserSession
object AuthenticatedManager : UserSession
object AuthenticatedCustomer : UserSession

class StoreAppHub(
    private val storage: StorageRepository
) {
    private var userSession: UserSession = NoSessionUser

    fun catalog(): Result<List<Product>, NotAuthenticated> = if (userSession !is AuthenticatedCustomer)
        Err(NotAuthenticated)
    else Ok(storage.findAll())

    fun buy(productId: Int): Result<Product, ErrorCode> = if (userSession !is AuthenticatedCustomer)
        Err(NotAuthenticated)
    else
        storage.findAll().find { it.id == productId }
            .toResultOr { ProductNotFound }
            .map { product ->
                if (product.quantity <= 0) return Err(ProductIsOutOfStock)
                if (product.isForAdultsOnly()) return Err(ProductForAdultsOnly)
                else Ok(product)

                storage.save(product.reduceStock())
                product
            }

    fun buy(productId: Int, customerAge: Int): Result<Product, ErrorCode> =
        buy(productId)
            .recoverIf(
                { it is ProductForAdultsOnly && customerAge > 18 },
                {
                    val product = storage.findAll().find { it.id == productId }!!

                    storage.save(product.reduceStock())
                    product
                }
            )

    fun register(product: Product) { storage.save(product) }

    fun logInAsAManager(password: String): Result<Unit, NotAuthenticated> =
        if (password == "admin123") Ok(Unit) else Err(NotAuthenticated)

    fun logInAsACustomer(password: String, birthday: LocalDate): Result<Unit, NotAuthenticated> =
        if (password == "customer123") {
            userSession = AuthenticatedCustomer

            Ok(Unit)
        } else Err(NotAuthenticated)

    fun resetSession() {
        userSession = NoSessionUser
    }
}

sealed interface ErrorCode {
    val message: String
}
data object NotAuthenticated : ErrorCode {
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
