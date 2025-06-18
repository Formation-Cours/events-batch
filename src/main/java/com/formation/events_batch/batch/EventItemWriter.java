package com.formation.events_batch.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.formation.events_batch.entities.EventEntity;
import com.formation.events_batch.repositories.EventRepository;

import jakarta.transaction.Transactional;

@Component
public class EventItemWriter implements ItemWriter<EventEntity> {

  private static final Logger logger = LoggerFactory.getLogger(EventItemWriter.class);

  private final EventRepository eventRepository;
  private long total = 0;

  public EventItemWriter(EventRepository eventRepository) {
    this.eventRepository = eventRepository;
  }

  @Override
  @Transactional
  public void write(Chunk<? extends EventEntity> chunk) throws Exception {
    if (chunk.isEmpty()) {
      return;
    }

    try {
      eventRepository.saveAll(chunk.getItems());
      total += chunk.size();

      if (total % 1000 == 0) {
        logger.info("Total: {}", total);
      }
    } catch (Exception e) {

      logger.error("Failed to save chunk of {} events: {}", chunk.size(), e.getMessage());
      for (EventEntity event : chunk.getItems()) {
        try {
          eventRepository.save(event);
        } catch (Exception ee) {
          logger.error("Probleme d'enregistrement pour cet item: {}", event);
        }
      }
    }
  }

}
