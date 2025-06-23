package com.formation.events_batch.controllers;

import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
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

  private final JobLauncher jobLauncher;

  @Scheduled(cron = "* */2 * * * *")
  public void schedule() throws Exception {
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("time", System.currentTimeMillis())
        .toJobParameters();

    jobLauncher.run(partitionEventJob, jobParameters);
  }
}
