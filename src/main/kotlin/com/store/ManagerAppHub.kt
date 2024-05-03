package com.store

import com.github.michaelbull.result.*
import java.time.LocalDate

interface StorageRepository {
    fun findAll(): List<Product>
    fun save(product: Product)
    fun save(userSession: UserSession)
    fun getLoggedUser(): UserSession
}

sealed interface UserSession
object NoSessionUser : UserSession
object AuthenticatedManager : UserSession
object AuthenticatedCustomer : UserSession

class CustomerAppHub(
    private val storage: StorageRepository
) {
    fun catalog(): Result<List<Product>, NotAuthenticated> = if (storage.getLoggedUser() !is AuthenticatedCustomer)
        Err(NotAuthenticated)
    else Ok(storage.findAll())

    fun buy(productId: Int): Result<Product, ErrorCode> = if (storage.getLoggedUser() !is AuthenticatedCustomer)
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

    fun logIn(password: String, birthday: LocalDate): Result<Unit, NotAuthenticated> =
        if (password == "customer123") {
            storage.save(AuthenticatedCustomer)

            Ok(Unit)
        } else Err(NotAuthenticated)

    // DONEXT: remove this?
    fun resetSession() {
        storage.save(NoSessionUser)
    }
}

class ManagerAppHub(
    private val storage: StorageRepository
) {
    private var userSession: UserSession = NoSessionUser

    fun register(product: Product): Result<Unit, ErrorCode> = Ok(storage.save(product))

    fun logIn(password: String): Result<Unit, NotAuthenticated> =
        if (password == "admin123") Ok(Unit) else Err(NotAuthenticated)

    fun resetSession() {
        userSession = NoSessionUser
    }
}

sealed interface ErrorCode {
    val message: String
}
data object NotAuthenticated : ErrorCode {
    override val message = "User is not authenticated"
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
