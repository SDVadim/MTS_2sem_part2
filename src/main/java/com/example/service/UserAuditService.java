package com.example.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.example.entity.UserAudit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserAuditService {

  @Autowired
  private CqlSession session;

  public void insertUserAction(UserAudit userAudit) {
    PreparedStatement preparedStatement = session.prepare(
        "INSERT INTO my_keyspace.user_audit (user_id, event_time, event_type, event_details) " +
            "VALUES (?, ?, ?, ?)"
    );

    BoundStatement boundStatement = preparedStatement.bind(
        userAudit.getUserId(),
        userAudit.getEventTime(),
        userAudit.getEventType(),
        userAudit.getEventDetails()
    );

    session.execute(boundStatement);
  }

  public List<UserAudit> getUserAudits(UUID userId) {
    PreparedStatement preparedStatement = session.prepare(
        "SELECT * FROM my_keyspace.user_audit WHERE user_id = ?"
    );
    BoundStatement boundStatement = preparedStatement.bind(userId);

    ResultSet resultSet = session.execute(boundStatement);
    return resultSet.all().stream()
        .map(row -> UserAudit.builder()
            .userId(row.getUuid("user_id"))
            .eventTime(row.getInstant("event_time"))
            .eventType(row.getString("event_type"))
            .eventDetails(row.getString("event_details"))
            .build())
        .toList();
  }
}