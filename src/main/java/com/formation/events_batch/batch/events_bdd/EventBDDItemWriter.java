package com.formation.events_batch.batch.events_bdd;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.core.io.FileSystemResource;

import com.formation.events_batch.dto.EventBddDTO;

public class EventBDDItemWriter {

  private static final Logger logger = LoggerFactory.getLogger(EventBDDItemWriter.class);

  public FlatFileItemWriter<EventBddDTO> eventItemWriter() {
    File outputDir = new File("output");
    if (!outputDir.exists()) {
      boolean created = outputDir.mkdirs();
      logger.info("OuputDir a t-il ete cree: {}", created);
    }

    DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    String fileName = "output/events_" + date.format(LocalDateTime.now()) + ".csv";

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

    // String[] fieldNames = new Test<>().getFields(EventBddDTO.class);
    // fieldExtractor.setNames(fieldNames);

    agg.setFieldExtractor(fieldExtractor);

    return agg;
  }

}
