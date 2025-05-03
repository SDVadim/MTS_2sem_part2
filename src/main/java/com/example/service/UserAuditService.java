package com.example.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.example.entity.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.security.*;
import org.springframework.stereotype.*;

import java.util.*;

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
        userAudit.getId(),
        userAudit.getEventTime(),
        userAudit.getAction(),
        userAudit.getEventDetails()
    );

    session.execute(boundStatement);
  }


  public List<UserAudit> getUserAudits(UUID id) {
    PreparedStatement preparedStatement = session.prepare(
        """
            SELECT * FROM my_keyspace.user_audit WHERE user_id = ?
            """
    );
    BoundStatement boundStatement = preparedStatement.bind(id);

    ResultSet resultSet = session.execute(boundStatement);
    return resultSet.all().stream()
        .map(row -> UserAudit.builder()
            .id(row.getUuid("user_id"))
            .eventTime(row.getInstant("event_time"))
            .action(Action.valueOf(row.getString("event_type")).name())
            .eventDetails(row.getString("event_details"))
            .build())
        .toList();
  }
}