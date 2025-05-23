package com.example.service;

import com.example.entity.DtoMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

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
