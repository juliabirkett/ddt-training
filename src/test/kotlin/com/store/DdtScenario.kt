package com.store

import com.store.cli.InMemoryStorageRepository
import com.store.cli.customerCliApp
import com.store.cli.managerCliApp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream

enum class TestScenarioConfig {
    InMemory, Cli;
}

fun newTestScenario(config: TestScenarioConfig) : DdtScenario = when (config) {
    TestScenarioConfig.InMemory -> InMemoryScenario(
        hub = StoreAppHub(InMemoryStorageRepository)
    )
    TestScenarioConfig.Cli -> CliScenario()
}

abstract class DdtScenario {
    abstract fun newCustomer(): Customer
    abstract fun newManager(): Manager
}

class InMemoryScenario(val hub: StoreAppHub) : DdtScenario() {
    override fun newCustomer(): Customer = InMemoryCustomer(hub)
    override fun newManager(): Manager = InMemoryManager(hub)
}

class CliScenario: DdtScenario() {
    private val repository = InMemoryStorageRepository

    override fun newCustomer(): Customer = CliCustomer(repository)

    override fun newManager(): Manager = CliManager(repository)


    companion object {
        private const val COMMAND_DELIMITER = "|==========================================================================|"
    }
}

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

       interactWithSystemIn(productsString) {
           app
       }
    }
}

abstract class Customer {
    abstract fun canBuy(productId: Int)
    abstract fun cannotBuy(productId: Int)
    abstract fun canSeeProductsCatalog(productIds: List<Int>)
}

class InMemoryCustomer(private val hub: StoreAppHub) : Customer() {
    override fun canBuy(productId: Int) {
        hub.buy(productId)
    }

    override fun cannotBuy(productId: Int) {
        assertThrows<ProductIsOutOfStock> { hub.buy(productId) }
    }

    override fun canSeeProductsCatalog(productIds: List<Int>) {
        assertEquals(productIds, hub.catalog().map { it.id })
    }
}

class CliCustomer(repository: StorageRepository) : Customer() {
    private val app by lazy {
        customerCliApp(repository = repository)
    }

    override fun canBuy(productId: Int) {
        interactWithSystemIn(productId.toString()) { app }
    }

    override fun cannotBuy(productId: Int) {
        TODO("Not yet implemented")
    }

    override fun canSeeProductsCatalog(productIds: List<Int>) {
        val output = captureSystemOut { app }

        productIds.forEach { id ->
            assertTrue(output.contains(id.toString()))
        }
    }
}

private fun captureSystemOut(operation: () -> Unit) : String = ByteArrayOutputStream().use {
    System.setOut(PrintStream(it))
    operation()
    it.flush()
    return String(it.toByteArray())
}

private fun interactWithSystemIn(command: String, operation: () -> Unit) : String = ByteArrayInputStream(command.toByteArray()).use {
    System.setIn(it)
    operation()
    return it.toString()
}
