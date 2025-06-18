package com.formation.events_batch.batch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formation.events_batch.dto.OpenAgendaDTO;

public class ThreadSafeJsonItemReader implements ItemReader<OpenAgendaDTO> {

  private static final Logger logger = LoggerFactory.getLogger(ThreadSafeJsonItemReader.class);

  private final Resource resource;
  private final ObjectMapper objectMapper;

  private List<OpenAgendaDTO> events;

  private final ReentrantLock initializeLock = new ReentrantLock();
  private volatile boolean initialized = false;
  private final AtomicInteger currentIndex = new AtomicInteger(0);

  private JsonParser jsonParser;

  public ThreadSafeJsonItemReader(Resource resource) {
    this.resource = resource;
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public OpenAgendaDTO read()
      throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
    if (!initialized) {
      initializeLock.lock();
      try {

        if (!initialized) {
          initializeReader();
          initialized = true;
        }

      } finally {
        initializeLock.unlock();
      }
    }

    if (events != null) {
      int index = currentIndex.getAndIncrement();
      if (index < events.size()) {
        return events.get(index); // events[i]
      }
    }

    if (jsonParser != null) {
      jsonParser.close();
    }
    return null;
  }

  private void initializeReader() throws IOException {
    InputStream inputStream = resource.getInputStream();
    JsonFactory jsonFactory = new JsonFactory();
    jsonParser = jsonFactory.createParser(inputStream);

    if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
      throw new IOException(
          "Le fichier n'est pas formate correctement. Le token actuel est " + jsonParser.getCurrentToken());
    }

    List<OpenAgendaDTO> tempEvents = new ArrayList<>();

    int count = 0;
    int logInterval = 1000;

    try {

      while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
        if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
          count = extractData(tempEvents, count, logInterval);
        }
      }

    } catch (Exception e) {
      logger.error("Error Json parse: {}", e.getMessage());
    }

    // Rendre la liste thread-safe après chargement
    events = Collections.synchronizedList(tempEvents);
    logger.info("Total des events: {}", events.size());
  }

  private int extractData(List<OpenAgendaDTO> tempEvents, int count, int logInterval) {
    try {
      OpenAgendaDTO event = objectMapper.readValue(jsonParser, OpenAgendaDTO.class);

      // verification des fields

      tempEvents.add(event);
      count++;

      if (count % logInterval == 0) {
        logger.info("Chargement de {} events", count);
      }

    } catch (Exception e) {
      logger.error("Failed to parse event at position {}: {}", count, e.getMessage());
    }
    return count;
  }

}
