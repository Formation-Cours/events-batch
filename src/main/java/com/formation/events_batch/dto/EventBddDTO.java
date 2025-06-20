package com.formation.events_batch.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class EventBddDTO {
  private Long id;

  private String title;

  private String description;

  private LocalDateTime startDate;

  private LocalDateTime endDate;

  private String location;

  private int maxParticipants;

  private Long organizerID;

  private LocalDateTime createdDate;

  private LocalDateTime modifiedDate;
}
