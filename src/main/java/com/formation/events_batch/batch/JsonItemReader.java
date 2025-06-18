package com.formation.events_batch.batch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

public class JsonItemReader implements ItemReader<OpenAgendaDTO> {

  private static final Logger logger = LoggerFactory.getLogger(JsonItemReader.class);

  private final Resource resource;
  private final ObjectMapper objectMapper;
  private Iterator<OpenAgendaDTO> eventIterator;
  private JsonParser jsonParser;

  public JsonItemReader(Resource resource) {
    this.resource = resource;
    this.objectMapper = new ObjectMapper();
    this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
        false);

  }

  @Override
  public OpenAgendaDTO read()
      throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
    if (eventIterator == null) {
      initializeReader();
    }

    if (eventIterator != null && eventIterator.hasNext()) {
      return eventIterator.next();
    }

    if (eventIterator == null) {
      return null;
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

    List<OpenAgendaDTO> events = new ArrayList<>();
    int count = 0;
    int logInterval = 10000;

    try {

      while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
        if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
          try {
            OpenAgendaDTO event = objectMapper.readValue(jsonParser, OpenAgendaDTO.class);

            // verification des fields

            events.add(event);
            count++;

            if (count % logInterval == 0) {
              logger.info("Chargement de {} events", count);
            }

          } catch (Exception e) {
            logger.error("Failed to parse event at position {}: {}", count, e.getMessage());
          }
        }
      }

    } catch (Exception e) {
      logger.error("Error Json parse: {}", e.getMessage());
    }

    logger.info("Total des events: {}", events.size());

    eventIterator = events.iterator();

  }

}
