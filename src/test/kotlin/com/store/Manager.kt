package com.store

import com.github.michaelbull.result.Err
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.contains
import com.natpryce.hamkrest.equalTo
import com.store.cli.managerCliApp

abstract class Manager {
    abstract fun canRegisterProductArrival(products: List<Product>)
    abstract fun logsIn(password: String)
    abstract fun cannotRegisterProducts(dueTo: NotAuthenticated)
}

class InMemoryManager(private val hub: StoreAppHub) : Manager() {
    override fun logsIn(password: String) { hub.logInAsAManager(password) }

    override fun cannotRegisterProducts(dueTo: NotAuthenticated) {
        assertThat(hub.logInAsAManager("invalid-password"), equalTo(Err(dueTo)))
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

    override fun logsIn(password: String) {
        allTheCommands.add(password)
    }

    override fun cannotRegisterProducts(dueTo: NotAuthenticated) {
        val output = captureSystemOut {
            interactWithSystemIn("invalid-password") { app }
        }

        assertThat(output, contains(Regex(dueTo.message)))
    }

    override fun canRegisterProductArrival(products: List<Product>) {
        allTheCommands.add(products.size.toString())
        val productsString = products.map { "${it.id},${it.description},${it.quantity}" }
        allTheCommands.add(productsString.joinToString("\n"))

        interactWithSystemIn(allTheCommands.joinToString("\n", postfix = "\n")) { app }
    }
}

