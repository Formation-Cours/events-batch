package com.formation.events_batch.batch.events_bdd;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.formation.events_batch.dto.EventBddDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventBDDItemReader {

  private final DataSource dataSource;
  private static final Logger logger = LoggerFactory.getLogger(EventBDDItemReader.class);

  @Bean
  JdbcCursorItemReader<EventBddDTO> eventItemReader() {
    String sql = "SELECT id, title, description, start_date, end_date, location, max_participants, organizer_id, created_date, modified_date FROM events";

    return new JdbcCursorItemReaderBuilder<EventBddDTO>()
        .name("eventBddReader")
        .dataSource(dataSource)
        .sql(sql)
        .rowMapper(rowMapper())
        .fetchSize(10_000)
        .build();
  }

  private RowMapper<EventBddDTO> rowMapper() {
    return (rs, rowNum) -> {

      try {
        EventBddDTO dto = new EventBddDTO();
        dto.setId(rs.getLong("id"));
        dto.setTitle(rs.getString("title"));
        dto.setDescription(cleanAndTruncateText(rs.getString("description"), 1000));

        if (rs.getTimestamp("start_date") != null) {
          dto.setStartDate(rs.getTimestamp("start_date").toLocalDateTime());
        }

        if (rs.getTimestamp("end_date") != null) {
          dto.setEndDate(rs.getTimestamp("end_date").toLocalDateTime());
        }

        dto.setLocation(rs.getString("location"));

        dto.setMaxParticipants(rs.getInt("max_participants"));

        dto.setOrganizerID(rs.getLong("organizer_id"));

        if (rs.getTimestamp("created_date") != null) {
          dto.setCreatedDate(rs.getTimestamp("created_date").toLocalDateTime());
        }

        if (rs.getTimestamp("modified_date") != null) {
          dto.setModifiedDate(rs.getTimestamp("modified_date").toLocalDateTime());
        }

        logger.info("Recuperation de la dto: {}", dto.getId());
        return dto;

      } catch (Exception e) {
        logger.error("Erreur de mapping dans EventBddDTO: {}", e.getMessage());
        throw new RuntimeException("Erreur de mapping dans EventBddDTO: " + e.getMessage(), e);
      }
    };

  }

  private String cleanAndTruncateText(String text, int maxLength) {
    if (!StringUtils.hasText(text)) {
      return "";
    }

    String original = text;
    String cleaned = text.trim()
        .replaceAll("\\r\\n|\\r|\\n", " ")
        .replaceAll("\\s+", " ")
        .replaceAll("[\"\\\\]", "");

    boolean wasTruncated = false;
    if (cleaned.length() > maxLength) {
      cleaned = cleaned.substring(0, maxLength - 3) + "...";
      wasTruncated = true;
    }

    if (wasTruncated || !original.equals(cleaned)) {
      logger.debug("🧹 Texte nettoyé - Longueur: {} -> {}, Tronqué: {}",
          original.length(), cleaned.length(), wasTruncated);
    }

    return cleaned;
  }

}
