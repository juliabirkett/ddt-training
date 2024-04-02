package org.example

import java.util.UUID

class AppHub {
    fun createDraft(command: CreateDraftCommand): Result<Draft> =
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
)

data class Draft(
    val id: UUID,
    val title: String,
    val abstract: String,
    val creatorId: UUID,
)