package com.formation.events_batch.config.events_bdd;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

  @Bean
  ItemReader<EventBddDTO> eventBDDItemReader() {
    return new EventBDDItemReader(dataSource).eventItemReader();
  }

  @Bean
  ItemWriter<EventBddDTO> eventBDDItemWriter() {
    return new EventBDDItemWriter().eventItemWriter();
  }

  @Bean
  Step simpleEventStep() {
    return new StepBuilder("simpleEventStep", jobRepository)
        .<EventBddDTO, EventBddDTO>chunk(5000, transactionManager)
        .reader(eventBDDItemReader())
        .writer(eventBDDItemWriter())
        .build();
  }

  @Bean
  Job simpleEventJob() {
    return new JobBuilder("simpleEventJob", jobRepository)
        .incrementer(new RunIdIncrementer())
        .flow(simpleEventStep())
        .end()
        .build();
  }

}
