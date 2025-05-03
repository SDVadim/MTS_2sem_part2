package com.example.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.servererrors.InvalidQueryException;
import com.example.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.*;
import org.testcontainers.cassandra.CassandraContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
class UserAuditServiceTest {

  @Container
  @ServiceConnection
  private static final CassandraContainer cassandraContainer = new CassandraContainer("cassandra:5.0.3")
      .waitingFor(Wait.forListeningPort()) ;

  @Autowired
  private CqlSession session;

  @Autowired
  private UserAuditService userAuditService;

  @BeforeEach
  void clearContainer() {
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

    assertThrows(Exception.class, () -> userAuditService.insertUserAction(userAudit));
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