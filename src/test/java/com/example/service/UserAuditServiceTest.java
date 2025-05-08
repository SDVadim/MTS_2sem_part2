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

import java.time.*;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.profiles.active=test")
@Testcontainers
class UserAuditServiceTest {

  @Container
  private static final CassandraContainer<?> cassandraContainer =
      new CassandraContainer<>("cassandra:4.1")
          .withExposedPorts(9042)
          .withStartupTimeout(Duration.ofMinutes(3))
          .waitingFor(Wait.forLogMessage(".*Startup complete.*\\n", 1));

  @Autowired
  private CqlSession session;

  @Autowired
  private UserAuditService userAuditService;

  @DynamicPropertySource
  static void cassandraProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.cassandra.contact-points", () -> cassandraContainer.getHost() + ":" + cassandraContainer.getMappedPort(9042));
    registry.add("spring.data.cassandra.local-datacenter", () -> "datacenter1");
    registry.add("spring.data.cassandra.keyspace-name", () -> "my_keyspace");
  }

  @BeforeEach
  void setupKeyspace() {
    session.execute("""
        CREATE KEYSPACE IF NOT EXISTS my_keyspace
        WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
    """);
    session.execute("TRUNCATE my_keyspace.user_audit");
  }

  @BeforeEach
  void clearData() {
    session.execute("TRUNCATE my_keyspace.user_audit");
  }

  @Test
  void testInsertUserActionPositive() {
    UserAudit userAudit = UserAudit.builder()
        .userId(UUID.randomUUID())
        .eventTime(Instant.now())
        .eventType(Action.INSERT.name())
        .eventDetails("Test insert action")
        .build();

    assertDoesNotThrow(() -> userAuditService.insertUserAction(userAudit));
  }

  @Test
  void testInsertUserActionNegative() {
    UserAudit userAudit = UserAudit.builder()
        .userId(null)
        .eventTime(Instant.now())
        .eventType(Action.INSERT.name())
        .eventDetails("Test insert action")
        .build();

    assertThrows(InvalidQueryException.class,
        () -> userAuditService.insertUserAction(userAudit));
  }

  @Test
  void testGetUserAuditsPositive() {
    UUID userId = UUID.randomUUID();
    UserAudit userAudit = UserAudit.builder()
        .userId(userId)
        .eventTime(Instant.now())
        .eventType(Action.SELECT.name())
        .eventDetails("Test select action")
        .build();

    userAuditService.insertUserAction(userAudit);

    List<UserAudit> audits = userAuditService.getUserAudits(userId);
    assertFalse(audits.isEmpty());
    assertEquals(userId, audits.get(0).getUserId());
  }

  @Test
  void testGetUserAuditsNegative() {
    UUID nonExistentUserId = UUID.randomUUID();
    List<UserAudit> audits = userAuditService.getUserAudits(nonExistentUserId);
    assertTrue(audits.isEmpty());
  }
}