package com.patryklikus.session.jpa

import java.time.Instant

/** Implement on your JPA entity to make it a Spring Session record. */
interface JpaSession {
    var sessionId: String
    val createdAt: Instant
    var accessedAt: Instant

    var expireTime: Instant

    var attributes: MutableMap<String, ByteArray>
}
