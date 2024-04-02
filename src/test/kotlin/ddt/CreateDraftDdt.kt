package ddt

import bootstrap.TestScenarioConfig
import bootstrap.newTestScenario
import org.example.UserDetails
import org.junit.jupiter.api.Test
import java.util.*

class CreateDraftDdt {
    private val scenario = newTestScenario(TestScenarioConfig.InMemory)
    private val author = scenario.newAuthor(
        UserDetails(
            id = UUID.randomUUID(),
            name = "Logged user",
            isLoggedIn = false
        )
    )

    @Test
    fun `An authorised author can create a draft`() {
        author.logsIn()

        author.canCreateADraft()
    }

    @Test
    fun `An unauthorised author can not create a draft`() {
        author.cannotCreateADraft()
    }
}