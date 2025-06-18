package com.formation.events_batch.batch;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.formation.events_batch.dto.OpenAgendaDTO;
import com.formation.events_batch.entities.EventEntity;
import com.formation.events_batch.service.UserService;

@Component
public class OpenAgendaEventProcessor implements ItemProcessor<OpenAgendaDTO, EventEntity> {

  private static final Logger logger = LoggerFactory.getLogger(OpenAgendaEventProcessor.class.toString());
  private static final String SYSTEM_USER_EMAIL = "system@event.com";

  private final UserService userService;

  public OpenAgendaEventProcessor(UserService userService) {
    this.userService = userService;
  }

  @Override
  public EventEntity process(OpenAgendaDTO item) throws Exception {

    if (item == null || item.getTitle() == null || item.getTitle().isBlank()) {
      // logger.warn("Pas de donnees dans {}", item);
      return null;
    }

    EventEntity event = new EventEntity();

    event.setTitle(cleanAndValidateString(item.getTitle()));

    if (item.getDescription() != null && item.getDescription().length() > 1000) {
      event.setDescription(item.getDescription().substring(0, 997) + "...");
    } else {
      event.setDescription(item.getDescription());
    }

    event.setDescription(cleanAndValidateString(event.getDescription()));

    event.setStartDate(parseDateTime(item.getFirstDateBegin()));
    if (event.getStartDate() == null) {
      // logger.warn("Il n'existe pas de date pour {}", item.getTitle());
      return null;
    }

    event.setEndDate(parseDateTime(item.getFirstDateEnd()));

    if (item.getLocationAddress() == null || item.getLocationAddress().isBlank()) {
      // logger.warn("Il n'existe pas d'adresse pour {}", item.getTitle());
      return null;
    }

    event.setLocation(item.getLocationAddress());
    event.setOrganizer(this.userService.getOrCreateUser(SYSTEM_USER_EMAIL));

    return event;
  }

  private LocalDateTime parseDateTime(String dateTimeStr) {
    if (dateTimeStr == null || dateTimeStr.isBlank()) {
      return null;
    }

    return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

  }

  private String cleanAndValidateString(String input) {
    if (input == null)
      return null;

    // Supprime les caractères null et autres caractères de contrôle problématiques
    String cleaned = input.replaceAll("[\u0000-\u001F\u007F-\u009F]", "");

    // Log si on a trouvé des caractères suspects
    // if (!cleaned.equals(input)) {
    // logger.warn("Caractères suspects supprimés de: {}", input);
    // }

    return cleaned;
  }
}
