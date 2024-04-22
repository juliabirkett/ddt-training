package com.store

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows

enum class TestScenarioConfig {
    InMemory;
}

object InMemoryStorageRepository : StorageRepository {
    private val products = mutableListOf<Product>()

    override fun findAll(): List<Product> = products
    override fun save(product: Product) {
        products += product
    }
}

fun newTestScenario(config: TestScenarioConfig) : DdtScenario = when (config) {
    TestScenarioConfig.InMemory -> InMemoryScenario(
        hub = StoreAppHub(InMemoryStorageRepository)
    )
}

abstract class DdtScenario {
    abstract fun newCustomer(): Customer
    abstract fun newManager(): Manager
}

class InMemoryScenario(val hub: StoreAppHub) : DdtScenario() {
    override fun newCustomer(): Customer = InMemoryCustomer(hub)
    override fun newManager(): Manager = InMemoryManager(hub)
}

abstract class Manager {
    abstract fun canRegisterProductArrival(products: List<Product>)
}

class InMemoryManager(private val hub: StoreAppHub) : Manager() {
    override fun canRegisterProductArrival(products: List<Product>) = products.forEach { hub.register(it) }
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
        assertThrows<Exception> { hub.buy(productId) }
    }

    override fun canSeeProductsCatalog(productIds: List<Int>) {
        assertEquals(productIds, hub.catalog().map { it.id })
    }
}