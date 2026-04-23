package com.patryklikus.session.jpa

/**
 * Builds a new, unsaved [JpaSession] entity for the given session id.
 *
 * The consumer's implementation owns the FK wiring (e.g. loads the current Account
 * from `SecurityContextHolder`) and any custom fields.
 */
fun interface JpaSessionFactory<S : JpaSession> {
    fun create(sessionId: String): S
}
