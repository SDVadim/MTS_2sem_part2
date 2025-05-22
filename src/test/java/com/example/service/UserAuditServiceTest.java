//package com.example.service;
//
//import com.datastax.oss.driver.api.core.CqlSession;
//import com.datastax.oss.driver.api.core.servererrors.InvalidQueryException;
//import com.example.entity.*;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.containers.CassandraContainer;
//import org.testcontainers.containers.wait.strategy.Wait;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import java.net.*;
//import java.time.Instant;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Testcontainers
//class UserAuditServiceTest {
//
//  @Container
//  private static final CassandraContainer<?> cassandraContainer =
//      new CassandraContainer<>("cassandra:latest")
//          .withExposedPorts(9042)
//          .withStartupTimeout(java.time.Duration.ofMinutes(3))
//          .waitingFor(Wait.forListeningPort());
//
//  @Autowired
//  private CqlSession session;
//
//  @Autowired
//  private UserMessageService userMessageService;
//
//  @BeforeAll
//  static void setupCassandraSchema() {
//    try (CqlSession session = CqlSession.builder()
//        .addContactPoint(new InetSocketAddress(cassandraContainer.getHost(), cassandraContainer.getMappedPort(9042)))
//        .withLocalDatacenter("datacenter1")
//        .build()) {
//      session.execute("CREATE KEYSPACE IF NOT EXISTS test_keyspace WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};");
//      session.execute("CREATE TABLE IF NOT EXISTS test_keyspace.user_audit ("
//          + "user_id UUID, "
//          + "event_time TIMESTAMP, "
//          + "event_type TEXT, "
//          + "event_details TEXT, "
//          + "PRIMARY KEY (user_id,event_time));");
//    }
//  }
//
//  @DynamicPropertySource
//  static void cassandraProperties(DynamicPropertyRegistry registry) {
//    registry.add("cassandra.host", cassandraContainer::getHost);
//    registry.add("cassandra.port", () -> cassandraContainer.getMappedPort(9042));
//  }
//
//  @Test
//  void testInsertUserActionPositive() {
//    DtoMessage userAudit = DtoMessage.builder()
//        .userId(UUID.randomUUID())
//        .eventTime(Instant.now())
//        .eventType(Action.INSERT.name())
//        .eventDetails("Test insert action")
//        .build();
//
//    assertDoesNotThrow(() -> userMessageService.insertUserAction(userAudit));
//  }
//
//  @Test
//  void testInsertUserActionNegative() {
//    DtoMessage userAudit = DtoMessage.builder()
//        .userId(null)
//        .eventTime(Instant.now())
//        .eventType(Action.INSERT.name())
//        .eventDetails("Test insert action")
//        .build();
//
//    assertThrows(InvalidQueryException.class,
//        () -> userMessageService.insertUserAction(userAudit));
//  }
//
//  @Test
//  void testGetUserAuditsPositive() {
//    UUID userId = UUID.randomUUID();
//    DtoMessage userAudit = DtoMessage.builder()
//        .userId(userId)
//        .eventTime(Instant.now())
//        .eventType(Action.SELECT.name())
//        .eventDetails("Test select action")
//        .build();
//
//    userMessageService.insertUserAction(userAudit);
//
//    List<DtoMessage> audits = userMessageService.getUserAudits(userId);
//    assertFalse(audits.isEmpty());
//    assertEquals(userId, audits.get(0).getUserId());
//  }
//
//  @Test
//  void testGetUserAuditsNegative() {
//    UUID nonExistentUserId = UUID.randomUUID();
//    List<DtoMessage> audits = userMessageService.getUserAudits(nonExistentUserId);
//    assertTrue(audits.isEmpty());
//  }
//}