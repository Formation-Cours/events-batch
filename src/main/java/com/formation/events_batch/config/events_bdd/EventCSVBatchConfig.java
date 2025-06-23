package com.formation.events_batch.config.events_bdd;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.formation.events_batch.dto.EventBddDTO;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class EventCSVBatchConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  @Value("${app.batch.file.output}")
  private String outputByYear;

  // @Value("${app.batch.years}")
  // private Integer[] years;

  /*
   * ------------ Simple Export Event ------------
   * # Batch simple
   * - reader
   * - processor (si besoin)
   * - writer
   * - listener (si besoin)
   * - step
   * - job
   */

  @Bean
  Step simpleEventStep(
      @Qualifier("stepScopeEventBDDItemReader") ItemReader<EventBddDTO> reader,
      @Qualifier("stepScopeEventBDDItemWriter") ItemWriter<EventBddDTO> writer) {
    return new StepBuilder("simpleEventStep", jobRepository)
        .<EventBddDTO, EventBddDTO>chunk(5000, transactionManager)
        .reader(reader)
        .writer(writer)
        .build();
  }

  @Bean
  Job simpleEventJob(Step simpleEventStep) {
    return new JobBuilder("simpleEventJob", jobRepository)
        // .incrementer(new RunIdIncrementer())
        // .flow(simpleEventStep)
        // .end()
        .start(simpleEventStep)
        .build();
  }

  /*
   * ------------ Partitioner Export Event By Year ------------
   * # Batch partition
   * - reader
   * - processor (si besoin)
   * - writer
   * - listener (si besoin)
   * - step (injection -> reader, processor, writer, ...)
   * - partitioner
   * - step orchestrateur (injection -> partitioner)
   * - job
   */

  @Bean
  Partitioner yearPartitioner() {
    return gridSize -> {
      Map<String, ExecutionContext> partitions = new HashMap<>();

      int[] years = { 2021, 2022 };

      for (int i = 0; i < years.length; i++) {
        ExecutionContext context = new ExecutionContext();
        int year = years[i];

        context.putString("whereClause", "WHERE EXTRACT(YEAR FROM start_date) = " + year);
        DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
        context.putString("outputFilename",
            outputByYear + "/events_year_" + year + "_" + date.format(LocalDateTime.now()) + ".csv");
        context.putString("partitionName", "year_" + year);

        partitions.put("partition_year_" + year, context);
      }

      return partitions;
    };
  }

  @Bean("partitionerEventTaskExecutor")
  TaskExecutor partitionerEventTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("partitioner-event-");
    executor.initialize();
    return executor;
  }

  @Bean
  Step partitionEventStep(
      @Qualifier("stepScopeEventBDDItemReader") ItemReader<EventBddDTO> reader,
      @Qualifier("stepScopeEventBDDItemWriter") ItemWriter<EventBddDTO> writer) {
    return new StepBuilder("partitionEventStep", jobRepository)
        .<EventBddDTO, EventBddDTO>chunk(5000, transactionManager)
        .reader(reader)
        .writer(writer)
        .build();
  }

  @Bean
  Step partitionYearEventStep(Step partitionEventStep, Partitioner yearPartitioner,
      TaskExecutor partitionerEventTaskExecutor) {
    return new StepBuilder("partitionYearEventStep", jobRepository)
        .partitioner("partitionEventStep", yearPartitioner)
        .step(partitionEventStep)
        .gridSize(4)
        .taskExecutor(partitionerEventTaskExecutor)
        .build();
  }

  @Bean
  Job partitionEventJob(Step partitionYearEventStep) {
    return new JobBuilder("partitionEventJob", jobRepository)
        .start(partitionYearEventStep)
        .build();
  }

}
