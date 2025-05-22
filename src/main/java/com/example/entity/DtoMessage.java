package com.example.entity;

import com.fasterxml.jackson.databind.annotation.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonDeserialize(builder = DtoMessage.UserAuditBuilder.class)
public class DtoMessage {
  private Long userId;
  private Instant eventTime;
  private String eventType;
  private String eventDetails;

  @JsonPOJOBuilder(withPrefix = "")
  public static class UserAuditBuilder {}
}