package com.patryklikus.session.jpa

import org.springframework.session.Session
import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Spring Session [Session] view over a consumer-provided [JpaSession] entity.
 *
 * Holds deserialized attributes in memory; serialization to `ByteArray` happens
 * at flush time inside [JpaHttpSessionRepository].
 */
class JpaHttpSession<S : JpaSession>(
    /** Backing entity. `null` for a fresh, not-yet-saved session. */
    internal var entity: S? = null,
    internal var sessionId: String = generateSessionId(),
    internal val attributes: MutableMap<String, Any> = mutableMapOf(),
    internal val createdAt: Instant = Instant.now(),
    internal var lastAccessedAt: Instant = createdAt,
    internal var maxInactive: Duration = Duration.ofMinutes(30),
) : Session {
    override fun getId(): String = sessionId

    override fun changeSessionId(): String {
        sessionId = generateSessionId()
        return sessionId
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> getAttribute(name: String?): T = attributes[name] as T

    override fun getAttributeNames(): Set<String> = attributes.keys

    override fun setAttribute(name: String?, value: Any?) {
        if (name == null) return
        if (value == null) attributes.remove(name) else attributes[name] = value
    }

    override fun removeAttribute(name: String?) {
        if (name != null) attributes.remove(name)
    }

    override fun getCreationTime(): Instant = createdAt

    override fun setLastAccessedTime(time: Instant?) {
        if (time != null) lastAccessedAt = time
    }

    override fun getLastAccessedTime(): Instant = lastAccessedAt

    override fun setMaxInactiveInterval(interval: Duration?) {
        if (interval != null) maxInactive = interval
    }

    override fun getMaxInactiveInterval(): Duration = maxInactive

    override fun isExpired(): Boolean =
        lastAccessedAt.plus(maxInactive).isBefore(Instant.now())

    private companion object {
        fun generateSessionId(): String = UUID.randomUUID().toString().replace("-", "")
    }
}
