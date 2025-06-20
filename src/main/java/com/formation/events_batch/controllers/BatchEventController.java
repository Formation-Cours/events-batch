package com.formation.events_batch.controllers;

import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BatchEventController {

  @Qualifier("simpleEventJob")
  private final Job simpleEventJob;

  @Qualifier("partitionEventJob")
  private final Job partitionEventJob;

  private final ResponseController responseController;

  @GetMapping("/export-events/simple")
  public ResponseEntity<Map<String, Object>> exportSimpleCSV() {
    return responseController.getResponse(simpleEventJob);
  }

  @GetMapping("/export-events/partitioned")
  public ResponseEntity<Map<String, Object>> exportPartitionerCSV() {
    return responseController.getResponse(partitionEventJob);
  }

}
