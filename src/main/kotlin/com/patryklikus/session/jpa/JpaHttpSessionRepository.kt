package com.patryklikus.session.jpa

import org.springframework.core.convert.converter.Converter
import org.springframework.core.serializer.support.DeserializingConverter
import org.springframework.core.serializer.support.SerializingConverter
import org.springframework.session.SessionRepository
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

/**
 * Adapter between Spring Session and a JPA-backed store.
 *
 * Translates [JpaHttpSession] to/from the consumer's [JpaSession] entity,
 * using the supplied converters to (de)serialize attribute values.
 */
open class JpaHttpSessionRepository<S : JpaSession>(
    private val sessions: JpaSessionRepository<S, *>,
    private val factory: JpaSessionFactory<S>,
    private val serializer: Converter<Any, ByteArray> = SerializingConverter(),
    private val deserializer: Converter<ByteArray, Any> = DeserializingConverter(),
) : SessionRepository<JpaHttpSession<S>> {

    override fun createSession(): JpaHttpSession<S> = JpaHttpSession()

    @Transactional
    override fun save(session: JpaHttpSession<S>) {
        val existing = session.entity
        if (existing == null) {
            val created = factory.create(session.sessionId).apply {
                accessedAt = session.lastAccessedAt
                expireTime = session.lastAccessedAt.plus(session.maxInactive)
                attributes = serializeAll(session.attributes)
            }
            sessions.save(created)
            session.entity = created
        } else {
            existing.sessionId = session.sessionId
            existing.accessedAt = session.lastAccessedAt
            existing.expireTime = session.lastAccessedAt.plus(session.maxInactive)
            existing.attributes = serializeAll(session.attributes)
            // explicit save — entity is detached when spring.jpa.open-in-view=false, so JPA dirty-checking alone would drop the update
            sessions.save(existing)
        }
    }

    @Transactional
    override fun findById(id: String?): JpaHttpSession<S>? {
        if (id == null) return null
        val entity = sessions.findBySessionId(id) ?: return null
        val session = JpaHttpSession(
            entity = entity,
            sessionId = entity.sessionId,
            attributes = deserializeAll(entity.attributes),
            createdAt = entity.createdAt,
            lastAccessedAt = entity.accessedAt,
            maxInactive = Duration.between(entity.accessedAt, entity.expireTime),
        )
        if (session.isExpired) {
            sessions.deleteBySessionId(id)
            return null
        }
        return session
    }

    @Transactional
    override fun deleteById(id: String?) {
        if (id != null) sessions.deleteBySessionId(id)
    }

    private fun serializeAll(source: Map<String, Any>): MutableMap<String, ByteArray> =
        source.mapValues { serializer.convert(it.value) }.toMutableMap()

    private fun deserializeAll(source: Map<String, ByteArray>): MutableMap<String, Any> =
        source.mapValues { deserializer.convert(it.value) }.toMutableMap()
}
