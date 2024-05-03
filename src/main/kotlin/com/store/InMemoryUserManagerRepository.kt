package com.store

object InMemoryUserManagerRepository: UserManagerRepository {
    private var loggedCustomer: UserSession = NoSessionUser
    private var loggedManager: UserSession = NoSessionUser

    override fun save(userSession: UserSession) {
        when (userSession) {
            is AuthenticatedManager -> loggedManager = userSession
            is AuthenticatedCustomer -> loggedCustomer = userSession
            else -> Unit
        }
    }

    override fun getLoggedCustomer(): UserSession = loggedCustomer
    override fun getLoggedManager(): UserSession = loggedManager

    fun cleanup() {
        loggedCustomer = NoSessionUser
        loggedManager = NoSessionUser
    }
}