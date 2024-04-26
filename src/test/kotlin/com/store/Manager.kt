package com.store

import com.store.cli.managerCliApp

abstract class Manager {
    abstract fun canRegisterProductArrival(products: List<Product>)
}

class InMemoryManager(private val hub: StoreAppHub) : Manager() {
    override fun canRegisterProductArrival(products: List<Product>) = products.forEach { hub.register(it) }
}

class CliManager(repository: StorageRepository) : Manager() {
    private val app by lazy {
        managerCliApp(repository = repository)
    }

    override fun canRegisterProductArrival(products: List<Product>) {
        val productsString = products.joinToString("\n") { "${it.id},${it.description},${it.quantity}" }

        interactWithSystemIn(productsString) { app }
    }
}
