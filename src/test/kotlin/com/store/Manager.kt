package com.store

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.throws
import com.store.cli.managerCliApp

abstract class Manager {
    abstract fun canRegisterProductArrival(products: List<Product>)
    abstract fun needToLogIn(password: String)
    abstract fun cannotRegisterOrAnything()
}

class InMemoryManager(private val hub: StoreAppHub) : Manager() {
    override fun needToLogIn(password: String) { hub.logInAsAManager(password) }

    override fun cannotRegisterOrAnything() {
        assertThat({ hub.logInAsAManager("invalid-password") }, throws<NotAuthenticatedAsManager>())
    }

    override fun canRegisterProductArrival(products: List<Product>) = products.forEach { hub.register(it) }
}

class CliManager(repository: StorageRepository) : Manager() {
    private val allTheCommands = mutableListOf<String>()
    private val app by lazy {
        managerCliApp(repository = repository)
    }

    init {
        allTheCommands.clear()
    }

    override fun needToLogIn(password: String) {
        allTheCommands.add(password)
    }

    override fun cannotRegisterOrAnything() {
        assertThat({ interactWithSystemIn("invalid-password") { app } }, throws<NotAuthenticatedAsManager>())
    }

    override fun canRegisterProductArrival(products: List<Product>) {
        allTheCommands.add(products.size.toString())
        val productsString = products.map { "${it.id},${it.description},${it.quantity}" }
        allTheCommands.add(productsString.joinToString("\n"))

        interactWithSystemIn(allTheCommands.joinToString("\n", postfix = "\n")) { app }
    }
}

