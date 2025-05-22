package com.example.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.example.entity.DtoMessage;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserMessageService {

  private final CqlSession session;

  @Value("${cassandra.keyspace}")
  private String keyspace;

  @Value("${cassandra.table}")
  private String table;

  private PreparedStatement insertPreparedStatement;
  private PreparedStatement selectPreparedStatement;

  public UserMessageService(CqlSession session) {
    this.session = session;
  }

  @PostConstruct
  public void init() {
    this.insertPreparedStatement = session.prepare(
        String.format("INSERT INTO %s.%s (user_id, event_time, event_type, event_details) VALUES (?, ?, ?, ?)", keyspace, table)
    );

    this.selectPreparedStatement = session.prepare(
        String.format("SELECT * FROM %s.%s WHERE user_id = ? ORDER BY event_time DESC", keyspace, table)
    );
  }

  public void insertUserAction(DtoMessage dtoMessage) {
    BoundStatement boundStatement = insertPreparedStatement.bind(
        dtoMessage.getUserId(),
        dtoMessage.getEventTime(),
        dtoMessage.getEventType(),
        dtoMessage.getEventDetails()
    );
    session.execute(boundStatement);
  }

  public List<DtoMessage> getUserAudits(Long userId) {
    BoundStatement boundStatement = selectPreparedStatement.bind(userId);
    ResultSet resultSet = session.execute(boundStatement);
    return resultSet.all().stream()
        .map(row -> DtoMessage.builder()
            .userId(userId)
            .eventTime(row.getInstant("event_time"))
            .eventType(row.getString("event_type"))
            .eventDetails(row.getString("event_details"))
            .build())
        .toList();
  }
}
