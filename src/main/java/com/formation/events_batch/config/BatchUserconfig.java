package com.formation.events_batch.config;

import java.io.IOException;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.formation.events_batch.batch.users.UserItemProcessor;
import com.formation.events_batch.batch.users.UserItemReader;
import com.formation.events_batch.batch.users.UserItemWriter;
import com.formation.events_batch.batch.users.UserItemPartitioner;
import com.formation.events_batch.dto.UserCsvDTO;
import com.formation.events_batch.entities.UserEntity;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BatchUserconfig {

  private final UserItemProcessor userItemProcessor;
  private final UserItemWriter userItemWriter;
  private final JobExecutionListenerImpl jobExecutionListenerImpl;
  private final UserItemPartitioner userItemPartition;

  @Bean
  TaskExecutor userTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("user-batch-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(60);
    executor.initialize();
    return executor;
  }

  /*
   * -------------- Lecture non Thread-Safe -----------------
   */
  @Bean
  FlatFileItemReader<UserCsvDTO> userItemReader() {
    return UserItemReader.read(new ClassPathResource("data/users.csv"));
  }

  @Bean
  Step userItemStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
    return new StepBuilder("importUserStep", jobRepository)
        .<UserCsvDTO, UserEntity>chunk(100, platformTransactionManager)
        .reader(userItemReader())
        .processor(userItemProcessor)
        .writer(userItemWriter)
        .taskExecutor(userTaskExecutor())
        .faultTolerant()
        .skip(DataIntegrityViolationException.class)
        .skipLimit(20)
        .skip(ConstraintViolationException.class)
        .skipLimit(Integer.MAX_VALUE)
        // .retry(DataIntegrityViolationException.class)
        // .retryLimit(3)
        .noSkip(IOException.class)
        .build();
  }

  @Bean
  Job importUserJob(JobRepository jobRepository, Step userItemStep) {
    return new JobBuilder("importUserJob", jobRepository)
        .start(userItemStep)
        .listener(jobExecutionListenerImpl)
        .build();

  }

  /*
   * -------------- Lecture Thread-Safe -----------------
   */

  @Bean
  ItemReader<UserCsvDTO> synchronizedUserItemReader() {
    var reader = new SynchronizedItemStreamReader<UserCsvDTO>();
    reader.setDelegate(userItemReader());
    return reader;
  }

  @Bean
  Step synchronizedUserItemStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
    return new StepBuilder("synchronizedUserItemStep", jobRepository)
        .<UserCsvDTO, UserEntity>chunk(1000, platformTransactionManager)
        .reader(synchronizedUserItemReader())
        .processor(userItemProcessor)
        .writer(userItemWriter)
        .taskExecutor(userTaskExecutor())
        .faultTolerant()
        .skip(DataIntegrityViolationException.class)
        .skipLimit(20)
        .skip(ConstraintViolationException.class)
        .skipLimit(Integer.MAX_VALUE)
        // .retry(DataIntegrityViolationException.class)
        // .retryLimit(3)
        .noSkip(IOException.class)
        .build();
  }

  @Bean
  Job synchronizedImportUserJob(JobRepository jobRepository, Step synchronizedUserItemStep) {
    return new JobBuilder("synchronizedImportUserJob", jobRepository)
        .start(synchronizedUserItemStep)
        .listener(jobExecutionListenerImpl)
        .build();
  }

  /*
   * -------------- Lecture en partition -----------------
   */

  @Bean
  Step userPartitionStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
    return new StepBuilder("userPartitionStep", jobRepository)
        .<UserCsvDTO, UserEntity>chunk(500, platformTransactionManager)
        .reader(userItemReader())
        .processor(userItemProcessor)
        .writer(userItemWriter)
        .taskExecutor(userTaskExecutor())
        .faultTolerant()
        .skip(DataIntegrityViolationException.class)
        .skipLimit(20)
        .skip(ConstraintViolationException.class)
        .skipLimit(Integer.MAX_VALUE)
        // .retry(DataIntegrityViolationException.class)
        // .retryLimit(3)
        .noSkip(IOException.class)
        .build();
  }

  @Bean
  Step partitionedUserStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
    return new StepBuilder("partitionedUserStep", jobRepository)
        .partitioner("userPartitionStep", userItemPartition)
        .step(userPartitionStep(jobRepository, platformTransactionManager))
        .taskExecutor(userTaskExecutor())
        .gridSize(10)
        .build();
  }

  @Bean
  Job partitionedImportUserJob(JobRepository jobRepository, Step partitionedUserStep) {
    return new JobBuilder("partitionedImportUserJob", jobRepository)
        .start(partitionedUserStep)
        .listener(jobExecutionListenerImpl)
        .build();
  }
}
