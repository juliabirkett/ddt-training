package com.manuscriptsystem.bootstrap

import com.manuscriptsystem.bootstrap.actors.Author
import com.manuscriptsystem.bootstrap.actors.Editor
import com.manuscriptsystem.bootstrap.actors.InMemoryAuthor
import org.example.com.manuscriptsystem.*
import java.util.UUID

enum class TestScenarioConfig {
    InMemory;
}

fun newTestScenario(config: TestScenarioConfig) : DdtScenario = when (config) {
    TestScenarioConfig.InMemory -> InMemoryScenario(
        theHub = AppHub(
            authenticateService = InMemoryAuthenticateService(),
            db = InMemoryDraftDatabase()
        )
    )
}

abstract class DdtScenario {
    abstract fun newAuthor(userDetails: UserDetails): Author
    abstract fun newEditor(userDetails: UserDetails): Editor
}

class InMemoryScenario(
    private val theHub: AppHub,
): DdtScenario() {
    override fun newAuthor(userDetails: UserDetails): Author =
        InMemoryAuthor(
            userDetails = userDetails,
            theHub = theHub,
        )

    override fun newEditor(userDetails: UserDetails): Editor {
        TODO("Not yet implemented")
    }
}

fun <T> Result<T>.expectSuccess() = if (isSuccess) this else throw Exception("Expected success, was failure! $this")
fun <T> Result<T>.expectFailure() = if (isSuccess) throw Exception("Expected failure, was success! $this") else this

class InMemoryDraftDatabase : DraftDatabase {
    private var repository: MutableList<Draft> = mutableListOf()

    override fun findById(id: UUID): Result<Draft> = repository.find { it.id == id }?.let {
        Result.success(it)
    } ?: Result.failure(DraftDatabase.DraftNotFoundException())

    override fun save(draft: Draft): Result<Draft> {
        repository = repository.plus(draft).toMutableList()
        return Result.success(draft)
    }
}

