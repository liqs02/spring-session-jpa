package com.patryklikus.session.jpa.it

import com.patryklikus.session.jpa.JpaSession
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.MapKeyColumn
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "test_sessions")
open class TestJpaSession(
    @Id override var sessionId: String = "",
    @Column(updatable = false) override val createdAt: Instant = Instant.now(),
    override var accessedAt: Instant = Instant.now(),
    override var expireTime: Instant = Instant.now(),
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "test_session_attributes")
    @MapKeyColumn(length = 200)
    @Column(columnDefinition = "bytea")
    override var attributes: MutableMap<String, ByteArray> = mutableMapOf(),
) : JpaSession
