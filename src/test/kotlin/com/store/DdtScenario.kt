package com.store

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream

val scenario by lazy {
    when (System.getenv("DDT_CONFIG")) {
        "cli" -> newTestScenario(TestScenarioConfig.Cli)
        else -> newTestScenario(TestScenarioConfig.InMemory)
    }
}

enum class TestScenarioConfig {
    InMemory, Cli;
}

fun newTestScenario(config: TestScenarioConfig) : DdtScenario = when (config) {
    TestScenarioConfig.InMemory -> {
        val storage = InMemoryStorageRepository
        val userManager = InMemoryUserManagerRepository

        InMemoryScenario(
            customerAppHub = CustomerAppHub(storage, userManager),
            managerHub = ManagerAppHub(storage, userManager),
        )
    }
    TestScenarioConfig.Cli -> CliScenario()
}

abstract class DdtScenario {
    abstract fun newCustomer(): Customer
    abstract fun newManager(): Manager
}

class InMemoryScenario(val customerAppHub: CustomerAppHub, val managerHub: ManagerAppHub) : DdtScenario() {
    override fun newCustomer(): Customer = InMemoryCustomer(customerAppHub)
    override fun newManager(): Manager = InMemoryManager(managerHub)
}

class CliScenario: DdtScenario() {
    private val repository = InMemoryStorageRepository
    private val userRepository = InMemoryUserManagerRepository

    override fun newCustomer(): Customer = CliCustomer(repository, userRepository)

    override fun newManager(): Manager = CliManager(repository, userRepository)
}

fun captureSystemOut(operation: () -> Unit) : String = ByteArrayOutputStream().use {
    System.setOut(PrintStream(it))
    operation()
    it.flush()
    return String(it.toByteArray())
}

fun interactWithSystemIn(command: String, operation: () -> Unit) : String = ByteArrayInputStream("$command\n".toByteArray()).use {
    System.setIn(it)
    operation()
    return it.toString()
}
