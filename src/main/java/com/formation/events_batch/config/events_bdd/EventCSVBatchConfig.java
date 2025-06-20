package com.formation.events_batch.config.events_bdd;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.formation.events_batch.batch.events_bdd.EventBDDItemReader;
import com.formation.events_batch.batch.events_bdd.EventBDDItemWriter;
import com.formation.events_batch.dto.EventBddDTO;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class EventCSVBatchConfig {

  private final DataSource dataSource;
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

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

  // @Bean
  // ItemReader<EventBddDTO> simpleEventBDDItemReader() {
  // return new EventBDDItemReader(dataSource).eventItemReader(null, null);
  // }
  //
  // @Bean
  // ItemWriter<EventBddDTO> simpleEventBDDItemWriter() {
  // return new EventBDDItemWriter().eventItemWriter(null, null);
  // }

  @Bean
  Step simpleEventStep(@Qualifier("stepScopeEventBDDItemReader") ItemReader<EventBddDTO> reader,
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
        .incrementer(new RunIdIncrementer())
        .flow(simpleEventStep)
        .end()
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

  // @Bean
  // @StepScope
  // ItemReader<EventBddDTO> partitionEventBDDItemReader(
  // @Value("#{stepExecutionContext['whereClause']}") String whereClause,
  // @Value("#{stepExecutionContext['partitionName']}") String partitionName
  // ) {
  // return new EventBDDItemReader(dataSource).eventItemReader(whereClause,
  // partitionName);
  // }
  //
  // @Bean
  // @StepScope
  // ItemWriter<EventBddDTO> partitionEventBDDItemWriter(
  // @Value("#{stepExecutionContext['outputFilename']}") String outputFilename,
  // @Value("#{stepExecutionContext['partitionName']}") String partitionName
  // ) {
  // return new EventBDDItemWriter().eventItemWriter(outputFilename,
  // partitionName);
  // }

  @Bean
  Partitioner yearPartitioner() {
    return gridSize -> {
      Map<String, ExecutionContext> partitions = new HashMap<>();

      int[] years = { 2021, 2022, 2023, 2024, 2025 };

      for (int i = 0; i < years.length; i++) {
        ExecutionContext context = new ExecutionContext();
        int year = years[i];

        context.putString("whereClause", "WHERE EXTRACT(YEAR FROM start_date) = " + year);
        context.putString("outputFilename", "output/events_year_" + year + ".csv");
        context.putString("partitionName", "year_" + year);

        partitions.put("partition_year_" + year, context);
      }

      return partitions;
    };
  }

  @Bean("simpleEventTaskExecutor")
  TaskExecutor simpleEventTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("partitioner-event-");
    executor.initialize();
    return executor;
  }

  @Bean
  Step partitionEventStep(@Qualifier("stepScopeEventBDDItemReader") ItemReader<EventBddDTO> reader,
      @Qualifier("stepScopeEventBDDItemWriter") ItemWriter<EventBddDTO> writer) {
    return new StepBuilder("partitionEventStep", jobRepository)
        .<EventBddDTO, EventBddDTO>chunk(5000, transactionManager)
        .reader(reader)
        .writer(writer)
        .build();
  }

  @Bean
  Step partitionYearEventStep(Step partitionEventStep) {
    return new StepBuilder("partitionYearEventStep", jobRepository)
        .partitioner("partitionEventStep", yearPartitioner())
        .step(partitionEventStep)
        .gridSize(4)
        .taskExecutor(simpleEventTaskExecutor())
        .build();
  }

  @Bean
  Job partitionEventJob(Step partitionYearEventStep) {
    return new JobBuilder("partitionEventJob", jobRepository)
        .start(partitionYearEventStep)
        .build();
  }

}
