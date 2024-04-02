package org.example

import java.util.UUID

class AppHub(
    private val authenticateService: AuthenticateService,
    private val db: DraftDatabase,
) {
    fun createDraft(command: CreateDraftCommand): Result<Draft> {
        authenticateService.authenticate(command)
            .map { authorisedCommand ->
                return db.save(
                    Draft(
                        id = UUID.randomUUID(),
                        title = authorisedCommand.title,
                        abstract = authorisedCommand.abstract,
                        creatorId = authorisedCommand.actor.id,
                    )
                )
            }

        return Result.failure(CreateDraftException())
    }

    fun editDraft(command: EditDraftCommand): Result<Unit> {
        db
            .findById(command.draftId)
            .map { draft ->
                return if (!draft.isEditableBy(command.actor))
                    Result.failure(EditDraftException())
                else
                    Result.success(Unit)
            }

        throw Exception("should not get here")
    }

    class CreateDraftException : Throwable("Could not create draft!")
    class EditDraftException : Throwable("Could not edit draft due to lack of permissions!")
}

interface Command {
    val actor: UserDetails
}

data class CreateDraftCommand(
    val title: String,
    val abstract: String,
    override val actor: UserDetails,
): Command

data class EditDraftCommand(
    val draftId: UUID,
    override val actor: UserDetails,
): Command

interface DraftDatabase {
    fun findById(id: UUID): Result<Draft>
    fun save(draft: Draft): Result<Draft>

    class DraftNotFoundException : Throwable("Couldn't find draft")
}

data class Draft(
    val id: UUID,
    val title: String,
    val abstract: String,
    val creatorId: UUID,
) {
    fun isEditableBy(actor: UserDetails): Boolean = creatorId == actor.id
}

interface AuthenticateService {
    fun authenticate(command: CreateDraftCommand): Result<CreateDraftCommand>
}

class InMemoryAuthenticateService : AuthenticateService {
    override fun authenticate(command: CreateDraftCommand): Result<CreateDraftCommand> =
        if (!command.actor.isLoggedIn) Result.failure(UserNotAuthorisedForAction())
        else Result.success(command)
}

class UserNotAuthorisedForAction : Throwable(message = "User is not authorised to perform action")