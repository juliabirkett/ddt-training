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

class CliManager(
    private val storage: StorageRepository,
    private val userManager: UserManagerRepository,
) : Manager() {
    override fun logsIn(password: String) {
        val output = captureSystemOut {
            interactWithSystemIn("manager-login $password") { app(storage, userManager) }
        }

        assertThat(output, contains(Regex("Logged in successfully!")))
    }

    override fun cannotRegisterProducts(dueTo: NotAuthenticated) {
        val output = captureSystemOut {
            interactWithSystemIn("register-product 1,testing,10") { app(storage, userManager) }
        }

        assertThat(output, contains(Regex(dueTo.message)))
    }

    override fun canRegisterProductArrival(products: List<Product>) {
        val outputs: List<String> = products.map { product ->
            captureSystemOut {
                interactWithSystemIn("register-product ${product.id},${product.description},${product.quantity}") { app(storage, userManager) }
            }
        }

        assertThat(outputs, allElements(contains(Regex("Product registered successfully"))))
    }
}

