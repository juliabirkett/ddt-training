package org.example

import java.util.UUID

class AppHub {
    fun createDraft(command: CreateDraftCommand): Result<Draft> =
        if (!command.actor.isLoggedIn) Result.failure(UserNotAuthorisedForAction())
        else
            Result.success(
                Draft(
                    id = UUID.randomUUID(),
                    title = command.title,
                    abstract = command.abstract,
                    creatorId = UUID.randomUUID(),
                )
            )
}

data class CreateDraftCommand(
    val title: String,
    val abstract: String,
    val actor: UserDetails,
)

data class Draft(
    val id: UUID,
    val title: String,
    val abstract: String,
    val creatorId: UUID,
)

class UserNotAuthorisedForAction : Throwable(message = "User is not authorised to perform action")