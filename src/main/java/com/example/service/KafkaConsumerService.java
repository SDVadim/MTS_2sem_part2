package com.example.service;

import com.example.entity.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import lombok.*;
import org.slf4j.*;
import org.springframework.kafka.annotation.*;
import org.springframework.stereotype.*;

@Service
@AllArgsConstructor
public class KafkaConsumerService {
  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaListener.class);

  private ObjectMapper objectMapper;
  UserMessageService userMessageService;

  @KafkaListener(topics = {"${topic-to-consume-message}"})
  public void consumeMessage(String message) throws JsonProcessingException {
    DtoMessage parsedMessage = objectMapper.readValue(message, DtoMessage.class);
    LOGGER.info("Retrieved message {}", message);

    userMessageService.insertUserAction(parsedMessage);
  }
}
