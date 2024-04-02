package bootstrap

import org.example.AppHub
import org.example.CreateDraftCommand
import org.example.InMemoryAuthenticateService
import org.example.UserDetails

enum class TestScenarioConfig {
    InMemory;
}

fun newTestScenario(config: TestScenarioConfig) : DdtScenario = when (config) {
    TestScenarioConfig.InMemory -> InMemoryScenario(
        theHub = AppHub(
            authenticateService = InMemoryAuthenticateService()
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
    abstract fun canCreateADraft()
    abstract fun cannotCreateADraft()
}

class InMemoryAuthor(
    private var userDetails: UserDetails,
    private val theHub: AppHub,
) : Author() {
    override fun logsIn()  {
        userDetails = userDetails.copy(isLoggedIn = true)
    }

    override fun canCreateADraft() {
        theHub.createDraft(
            CreateDraftCommand(
                title = "Amazing title",
                abstract = "Something",
                actor = userDetails,
            )
        ).expectSuccess()
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

fun <T> Result<T>.expectSuccess() = if (isSuccess) this else Result.failure(Exception("Expected success, was failure! $this"))
fun <T> Result<T>.expectFailure() = if (isSuccess) throw Exception("Expected failure, was success! $this") else this

