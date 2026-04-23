package com.patryklikus.session.jpa

import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.NoRepositoryBean
import java.time.Instant

/**
 * Base Spring Data repository the consumer extends for their [JpaSession] entity.
 */
@NoRepositoryBean
interface JpaSessionRepository<S : JpaSession, ID : Any> : CrudRepository<S, ID> {
    fun findBySessionId(sessionId: String): S?

    fun deleteBySessionId(sessionId: String): Int

    fun deleteByExpireTimeBefore(threshold: Instant): Int
}
