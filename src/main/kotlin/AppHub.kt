package org.example

import java.util.UUID

class AppHub(
    private val authenticateService: AuthenticateService
) {
    fun createDraft(command: CreateDraftCommand): Result<Draft> =
        authenticateService.authenticate(command)
            .map { authorisedCommand ->
                Draft(
                    id = UUID.randomUUID(),
                    title = authorisedCommand.title,
                    abstract = authorisedCommand.abstract,
                    creatorId = authorisedCommand.actor.id,
                )
            }
}

interface Command {
    val actor: UserDetails
}

data class CreateDraftCommand(
    val title: String,
    val abstract: String,
    override val actor: UserDetails,
): Command

data class Draft(
    val id: UUID,
    val title: String,
    val abstract: String,
    val creatorId: UUID,
)

interface AuthenticateService {
    fun authenticate(command: CreateDraftCommand): Result<CreateDraftCommand>
}

class InMemoryAuthenticateService : AuthenticateService {
    override fun authenticate(command: CreateDraftCommand): Result<CreateDraftCommand> =
        if (!command.actor.isLoggedIn) Result.failure(UserNotAuthorisedForAction())
        else Result.success(command)
}

class UserNotAuthorisedForAction : Throwable(message = "User is not authorised to perform action")