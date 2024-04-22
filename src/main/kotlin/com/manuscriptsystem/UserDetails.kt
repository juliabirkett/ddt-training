package org.example.com.manuscriptsystem

import java.util.UUID

data class UserDetails(
    val id: UUID,
    val name: String,
    val isLoggedIn: Boolean
)