package bootstrap

import org.example.*
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
}

class InMemoryScenario(
    private val theHub: AppHub,
): DdtScenario() {
    override fun newAuthor(userDetails: UserDetails): Author =
        InMemoryAuthor(
            userDetails = userDetails,
            theHub = theHub,
        )
}

abstract class Author {
    abstract fun logsIn()
    abstract fun canCreateADraft(): UUID
    abstract fun cannotCreateADraft()
    abstract fun canEditDraft(draftId: UUID)
    abstract fun cannotEditDraft(draftId: UUID)
}

class InMemoryAuthor(
    private var userDetails: UserDetails,
    private val theHub: AppHub,
) : Author() {
    override fun logsIn()  {
        userDetails = userDetails.copy(isLoggedIn = true)
    }

    override fun canCreateADraft(): UUID =
        theHub.createDraft(
            CreateDraftCommand(
                title = "Amazing title",
                abstract = "Something",
                actor = userDetails,
            )
        )
            .map { draft ->
                draft.id
            }
            .expectSuccess()
            .getOrElse { throw it }

    override fun canEditDraft(draftId: UUID) {
        theHub.editDraft(
            EditDraftCommand(
                draftId = draftId,
                actor = userDetails,
            )
        ).expectSuccess()
    }

    override fun cannotEditDraft(draftId: UUID) {
        theHub.editDraft(
            EditDraftCommand(
                draftId = draftId,
                actor = userDetails,
            )
        ).expectFailure()
    }

    override fun cannotCreateADraft() {
        theHub.createDraft(
            CreateDraftCommand(
                title = "Amazing title",
                abstract = "Something",
                actor = userDetails
            )
        ).expectFailure()
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

