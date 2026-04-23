package com.patryklikus.session.jpa.it

import com.patryklikus.session.jpa.JpaHttpSessionRepository
import com.patryklikus.session.jpa.JpaSessionFactory
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import java.time.Instant

@Configuration(proxyBeanMethods = false)
@EnableAutoConfiguration
@EntityScan(basePackageClasses = [TestJpaSession::class])
@EnableJpaRepositories(basePackageClasses = [TestSessionRepository::class])
@Import(PostgresContainerConfig::class)
class TestConfig {

    @Bean
    fun sessionFactory(): JpaSessionFactory<TestJpaSession> =
        JpaSessionFactory { sessionId ->
            val now = Instant.now()
            TestJpaSession(
                sessionId = sessionId,
                createdAt = now,
                accessedAt = now,
                expireTime = now,
            )
        }

    @Bean
    fun httpSessionRepository(
        sessions: TestSessionRepository,
        factory: JpaSessionFactory<TestJpaSession>,
    ): JpaHttpSessionRepository<TestJpaSession> = JpaHttpSessionRepository(sessions, factory)
}
