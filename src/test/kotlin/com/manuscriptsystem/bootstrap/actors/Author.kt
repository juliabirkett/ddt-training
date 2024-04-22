package com.manuscriptsystem.bootstrap.actors

import com.manuscriptsystem.bootstrap.expectFailure
import com.manuscriptsystem.bootstrap.expectSuccess
import org.example.com.manuscriptsystem.AppHub
import org.example.com.manuscriptsystem.CreateDraftCommand
import org.example.com.manuscriptsystem.EditDraftCommand
import org.example.com.manuscriptsystem.UserDetails
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
