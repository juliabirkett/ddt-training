package com.manuscriptsystem.bootstrap.actors

import java.util.*

abstract class Editor {
    abstract fun canCreateAmendment(draftId: UUID): UUID
}
