package com.patryklikus.session.jpa.it

import com.patryklikus.session.jpa.JpaSessionRepository

interface TestSessionRepository : JpaSessionRepository<TestJpaSession, String>
