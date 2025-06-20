package com.formation.events_batch.batch.events_bdd;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import com.formation.events_batch.dto.EventBddDTO;

@Configuration
public class EventBDDItemWriter {

  private static final Logger logger = LoggerFactory.getLogger(EventBDDItemWriter.class);

  @Bean("stepScopeEventBDDItemWriter")
  @StepScope
  public FlatFileItemWriter<EventBddDTO> eventItemWriter(
      @Value("#{stepExecutionContext['outputFilename']}") String outputFilename,
      @Value("#{stepExecutionContext['partitionName']}") String partitionName) {
    File outputDir = new File("output");
    if (!outputDir.exists()) {
      boolean created = outputDir.mkdirs();
      logger.info("OuputDir a t-il ete cree: {}", created);
    }

    String fileName;

    if (outputFilename != null && !outputFilename.isBlank()) {
      fileName = outputFilename;
      logger.info("Le fichier output se nomme: {} et a pour partition: {}", outputFilename, partitionName);
    } else {
      DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
      fileName = "output/events_" + date.format(LocalDateTime.now()) + ".csv";
    }

    return new FlatFileItemWriterBuilder<EventBddDTO>()
        .name("eventCsvWriter")
        .resource(new FileSystemResource(fileName))
        .shouldDeleteIfExists(true)
        .headerCallback(writer -> {
          writer.write(
              "id, title, description, start_date, end_date, location, max_participants, organizer_id, created_date, modified_date");
          logger.info("Demarrage de l'ecriture du CSV: {}", fileName);
        })
        .lineAggregator(lineAggregator())
        .footerCallback(writer -> logger.info("Csv export completed"))
        .build();
  }

  private LineAggregator<EventBddDTO> lineAggregator() {
    DelimitedLineAggregator<EventBddDTO> agg = new DelimitedLineAggregator<>();
    agg.setDelimiter(",");
    agg.setQuoteCharacter("\"");

    BeanWrapperFieldExtractor<EventBddDTO> fieldExtractor = new BeanWrapperFieldExtractor<>();
    fieldExtractor.setNames(new String[] {
        "id", "title", "description", "startDate", "endDate", "location",
        "maxParticipants", "organizerID",
        "createdDate", "modifiedDate"
    });

    agg.setFieldExtractor(fieldExtractor);

    return agg;
  }

}
