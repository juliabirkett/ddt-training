package com.store

import com.store.cli.managerCliApp
import org.junit.jupiter.api.assertThrows

abstract class Manager {
    abstract fun canRegisterProductArrival(products: List<Product>)
    abstract fun needToLogIn(password: String)
    abstract fun cannotRegisterOrAnything()
}

class InMemoryManager(private val hub: StoreAppHub) : Manager() {
    override fun needToLogIn(password: String) { hub.logInAsAManager(password) }

    override fun cannotRegisterOrAnything() {
        assertThrows<NotAuthenticatedAsManager> {
            hub.logInAsAManager("invalid-password")
        }
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
        assertThrows<NotAuthenticatedAsManager> {
            interactWithSystemIn("invalid-password") { app }
        }
    }

    override fun canRegisterProductArrival(products: List<Product>) {
        allTheCommands.add(products.size.toString())
        val productsString = products.map { "${it.id},${it.description},${it.quantity}" }
        allTheCommands.add(productsString.joinToString("\n"))

        interactWithSystemIn(allTheCommands.joinToString("\n", postfix = "\n")) { app }
    }
}

