package com.patryklikus.session.jpa.it

import com.patryklikus.session.jpa.JpaHttpSession
import com.patryklikus.session.jpa.JpaHttpSessionRepository
import com.patryklikus.session.jpa.JpaSessionFactory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@JpaIntegration
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
class JpaHttpSessionRepositoryIT(
    private val httpSessions: JpaHttpSessionRepository<TestJpaSession>,
    private val sessions: TestSessionRepository,
    txManager: PlatformTransactionManager,
) {

    private val tx = TransactionTemplate(txManager)

    private fun <T> inTx(block: () -> T): T = tx.execute { block() } as T

    @BeforeEach
    fun wipe() {
        tx.executeWithoutResult { sessions.deleteAll() }
    }

    @Nested
    inner class Save {
        @Test
        fun `persists a new session and findById returns it with attributes`() {
            val session = httpSessions.createSession().apply {
                setAttribute("user", "alice")
                setAttribute("count", 42)
            }

            httpSessions.save(session)
            val loaded = httpSessions.findById(session.id)

            assertNotNull(loaded)
            assertEquals(session.id, loaded.id)
            assertEquals("alice", loaded.getAttribute("user"))
            assertEquals(42, loaded.getAttribute("count"))
        }

        @Test
        fun `updates attributes on an already-persisted session`() {
            val session = httpSessions.createSession().apply {
                setAttribute("role", "guest")
            }
            httpSessions.save(session)

            val reloaded = httpSessions.findById(session.id)!!
            reloaded.setAttribute("role", "admin")
            reloaded.setAttribute("extra", "value")
            httpSessions.save(reloaded)

            val after = httpSessions.findById(session.id)!!
            assertEquals("admin", after.getAttribute("role"))
            assertEquals("value", after.getAttribute("extra"))
        }
    }

    @Nested
    inner class FindById {
        @Test
        fun `returns null and deletes row when session has expired`() {
            val session = expiredSession()
            httpSessions.save(session)
            assertNotNull(inTx { sessions.findBySessionId(session.id) })

            val loaded = httpSessions.findById(session.id)

            assertNull(loaded)
            assertNull(inTx { sessions.findBySessionId(session.id) })
        }
    }

    @Nested
    inner class DeleteByExpireTimeBefore {
        @Test
        fun `returns affected row count when rows match`() {
            httpSessions.save(expiredSession())
            httpSessions.save(expiredSession())
            val live = httpSessions.createSession().apply {
                maxInactiveInterval = Duration.ofHours(1)
            }
            httpSessions.save(live)

            val removed = inTx { sessions.deleteByExpireTimeBefore(Instant.now()) }

            assertEquals(2, removed)
            assertNotNull(inTx { sessions.findBySessionId(live.id) })
        }
    }

    @Nested
    inner class PersistedExpiry {
        @Test
        fun `stored expireTime is honored across a repository restart`() {
            val session = expiredSession()
            httpSessions.save(session)

            val freshHttpSessions = JpaHttpSessionRepository(
                sessions = sessions,
                factory = JpaSessionFactory { sid -> TestJpaSession(sessionId = sid) },
            )

            assertNull(inTx { freshHttpSessions.findById(session.id) })
            assertNull(inTx { sessions.findBySessionId(session.id) })
        }

        @Test
        fun `stored expireTime keeps a live session visible across a repository restart`() {
            val session = httpSessions.createSession().apply {
                maxInactiveInterval = Duration.ofHours(1)
                setAttribute("k", "v")
            }
            httpSessions.save(session)

            val freshHttpSessions = JpaHttpSessionRepository(
                sessions = sessions,
                factory = JpaSessionFactory { sid -> TestJpaSession(sessionId = sid) },
            )
            val loaded = inTx { freshHttpSessions.findById(session.id) }

            assertNotNull(loaded)
            assertEquals("v", loaded.getAttribute("k"))
            assertEquals(Duration.ofHours(1), loaded.maxInactiveInterval)
        }
    }

    private fun expiredSession(): JpaHttpSession<TestJpaSession> =
        httpSessions.createSession().apply {
            maxInactiveInterval = Duration.ofSeconds(1)
            lastAccessedTime = Instant.now().minusSeconds(3600)
        }
}
