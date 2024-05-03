package com.store

import com.github.michaelbull.result.Err
import com.natpryce.hamkrest.allElements
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.contains
import com.natpryce.hamkrest.equalTo
import com.store.cli.app

abstract class Manager {
    abstract fun canRegisterProductArrival(products: List<Product>)
    abstract fun logsIn(password: String)
    abstract fun cannotRegisterProducts(dueTo: NotAuthenticated)
}

class InMemoryManager(private val hub: ManagerAppHub) : Manager() {
    override fun logsIn(password: String) { hub.logIn(password) }

    override fun cannotRegisterProducts(dueTo: NotAuthenticated) {
        assertThat(hub.logIn("invalid-password"), equalTo(Err(dueTo)))
    }

    override fun canRegisterProductArrival(products: List<Product>) = products.forEach { hub.register(it) }
}

class CliManager(private val repository: StorageRepository) : Manager() {
    override fun logsIn(password: String) {
        val output = captureSystemOut {
            interactWithSystemIn("manager-login $password") { app(repository) }
        }

        assertThat(output, contains(Regex("Logged in successfully!")))
    }

    override fun cannotRegisterProducts(dueTo: NotAuthenticated) {
        TODO()
    }

    override fun canRegisterProductArrival(products: List<Product>) {
        val outputs: List<String> = products.map { product ->
            captureSystemOut {
                interactWithSystemIn("register-product ${product.id},${product.description},${product.quantity}") { app(repository) }
            }
        }

        assertThat(outputs, allElements(contains(Regex("Product registered successfully"))))
    }
}

