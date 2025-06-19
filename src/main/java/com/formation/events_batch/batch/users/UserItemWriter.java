package com.formation.events_batch.batch.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import com.formation.events_batch.entities.UserEntity;
import com.formation.events_batch.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserItemWriter implements ItemWriter<UserEntity> {

  private static final Logger logger = LoggerFactory.getLogger(UserItemWriter.class);
  private final UserRepository userRepository;

  @Override
  public void write(Chunk<? extends UserEntity> chunk) throws Exception {
    if (chunk.isEmpty()) {
      return;
    }

    try {
      userRepository.saveAll(chunk.getItems());
      logger.info("Envoi en BDD avec succes: {}", chunk.size());
    } catch (Exception e) {
      logger.error("Probleme d'envoi en BDD: {}", e.getMessage());
      throw e;
    }
  }

}
