package com.example.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Table("user_audit")
public class UserAudit {

  @PrimaryKeyColumn(name = "user_id", ordinal = 0, type = org.springframework.data.cassandra.core.cql.PrimaryKeyType.PARTITIONED)
  private UUID userId;

  @PrimaryKeyColumn(name = "event_time", ordinal = 1, type = org.springframework.data.cassandra.core.cql.PrimaryKeyType.CLUSTERED)
  private Instant eventTime;

  private String eventType;
  private String eventDetails;
}