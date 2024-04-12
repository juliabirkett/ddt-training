package bootstrap.actors

import bootstrap.expectFailure
import bootstrap.expectSuccess
import org.example.AppHub
import org.example.CreateDraftCommand
import org.example.EditDraftCommand
import org.example.UserDetails
import java.util.*

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
