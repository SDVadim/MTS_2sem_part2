package com.example.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.servererrors.*;
import com.example.entity.UserAudit;
import com.example.entity.Action;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class UserAuditServiceTest {

  @Container
  private static final CassandraContainer<?> cassandraContainer =
      new CassandraContainer<>("cassandra:4.1") // Используем стабильную версию
          .withExposedPorts(9042)
          .waitingFor(Wait.forLogMessage(".*Startup complete.*\\n", 1));

  @Autowired
  private CqlSession session;

  @Autowired
  private UserAuditService userAuditService;

  @DynamicPropertySource
  static void cassandraProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.cassandra.contact-points", cassandraContainer::getHost);
    registry.add("spring.data.cassandra.port", () -> cassandraContainer.getMappedPort(9042));
    registry.add("spring.data.cassandra.local-datacenter", () -> "datacenter1");
    registry.add("spring.data.cassandra.keyspace-name", () -> "my_keyspace");
  }

  @BeforeEach
  void clearData() {
    session.execute("TRUNCATE my_keyspace.user_audit");
  }

  @Test
  void testInsertUserActionPositive() {
    UserAudit userAudit = UserAudit.builder()
        .id(UUID.randomUUID())
        .eventTime(Instant.now())
        .action(Action.INSERT.name())
        .eventDetails("Test insert action")
        .build();

    assertDoesNotThrow(() -> userAuditService.insertUserAction(userAudit));
  }

  @Test
  void testInsertUserActionNegative() {
    UserAudit userAudit = UserAudit.builder()
        .id(null)
        .eventTime(Instant.now())
        .action(Action.INSERT.name())
        .eventDetails("Test insert action")
        .build();

    assertThrows(InvalidQueryException.class,
        () -> userAuditService.insertUserAction(userAudit));
  }

  @Test
  void testGetUserAuditsPositive() {
    UUID userId = UUID.randomUUID();
    UserAudit userAudit = UserAudit.builder()
        .id(userId)
        .eventTime(Instant.now())
        .action(Action.SELECT.name())
        .eventDetails("Test select action")
        .build();

    userAuditService.insertUserAction(userAudit);

    List<UserAudit> audits = userAuditService.getUserAudits(userId);
    assertFalse(audits.isEmpty());
    assertEquals(userId, audits.get(0).getId());
  }

  @Test
  void testGetUserAuditsNegative() {
    UUID nonExistentUserId = UUID.randomUUID();
    List<UserAudit> audits = userAuditService.getUserAudits(nonExistentUserId);
    assertTrue(audits.isEmpty());
  }
}