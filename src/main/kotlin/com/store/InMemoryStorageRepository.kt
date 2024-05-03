package com.store

object InMemoryStorageRepository : StorageRepository {
    private val products = mutableListOf<Product>()
    private var loggedUser: UserSession = NoSessionUser

    override fun findAll(): List<Product> = products
    override fun save(product: Product) {
        products.find { it.id == product.id }?.let { existingProduct ->
            products.remove(existingProduct)
        }

        products += product
    }

    override fun save(userSession: UserSession) {
        loggedUser = userSession
    }

    override fun getLoggedUser(): UserSession = loggedUser

    fun cleanup() {
        products.clear()
        loggedUser = NoSessionUser
    }
}
