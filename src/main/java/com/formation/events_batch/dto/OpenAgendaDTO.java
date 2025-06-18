package com.formation.events_batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAgendaDTO {

  @JsonProperty("title_fr")
  private String title;

  @JsonProperty("longdescription_fr")
  private String description;

  @JsonProperty("firstdate_begin")
  private String firstDateBegin;

  @JsonProperty("firstdate_end")
  private String firstDateEnd;

  @JsonProperty("location_address")
  private String locationAddress;

}
