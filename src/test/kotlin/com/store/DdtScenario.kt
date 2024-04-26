package com.store

import com.store.cli.InMemoryStorageRepository
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
}

fun captureSystemOut(operation: () -> Unit) : String = ByteArrayOutputStream().use {
    System.setOut(PrintStream(it))
    operation()
    it.flush()
    return String(it.toByteArray())
}

fun interactWithSystemIn(command: String, operation: () -> Unit) : String = ByteArrayInputStream(command.toByteArray()).use {
    System.setIn(it)
    operation()
    return it.toString()
}
