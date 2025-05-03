package com.example.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Table("user_audit")
public class UserAudit {

  @PrimaryKey
  private UUID id;
  private Instant eventTime;
  private String action;
  private String eventDetails;
}