package com.patryklikus.session.jpa.it

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor

@SpringBootTest(classes = [TestConfig::class])
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
annotation class JpaIntegration
