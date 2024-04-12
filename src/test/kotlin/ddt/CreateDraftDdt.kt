package ddt

import bootstrap.TestScenarioConfig
import bootstrap.newTestScenario
import org.example.UserDetails
import org.junit.jupiter.api.Test
import java.util.*

class CreateDraftDdt {
    private val scenario = newTestScenario(TestScenarioConfig.InMemory)
    private val janeTheAuthor = scenario.newAuthor(
        UserDetails(
            id = UUID.randomUUID(),
            name = "Jane user",
            isLoggedIn = false
        )
    )
    private val bobTheAuthor = scenario.newAuthor(
        UserDetails(
            id = UUID.randomUUID(),
            name = "Bob user",
            isLoggedIn = false
        )
    )
    private val edTheEditor = scenario.newEditor(
        UserDetails(
            id = UUID.randomUUID(),
            name = "Ed Editor",
            isLoggedIn = false
        )
    )

    @Test
    fun `Authenticated author can create a draft`() {
        janeTheAuthor.logsIn()

        janeTheAuthor.canCreateADraft()
    }

    @Test
    fun `Unauthenticated author can not create a draft`() {
        janeTheAuthor.cannotCreateADraft()
    }

    @Test
    fun `Only authorised authors can edit the draft`() {
        janeTheAuthor.logsIn()
        bobTheAuthor.logsIn()

        val draftId = janeTheAuthor.canCreateADraft()
        janeTheAuthor.canEditDraft(draftId = draftId)
        bobTheAuthor.cannotEditDraft(draftId = draftId)
    }

    @Test
    fun `Editor can create an amendment draft`() {
        janeTheAuthor.logsIn()

        val draftId = janeTheAuthor.canCreateADraft()
        val amendmentDraftId = edTheEditor.canCreateAmendment(draftId = draftId)
        janeTheAuthor.canEditDraft(draftId = amendmentDraftId)
    }
}