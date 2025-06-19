package com.formation.events_batch.batch.users;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.core.io.Resource;
import org.springframework.validation.BindException;

import com.formation.events_batch.dto.UserCsvDTO;

public class UserItemReader {

  // firstName,lastName,email,password,emailVerified,dateInscription,avatar,streetAddress,city,country;

  public static FlatFileItemReader<UserCsvDTO> read(Resource resource) {
    return new FlatFileItemReaderBuilder<UserCsvDTO>()
        .name("userItemReader")
        .resource(resource)
        .encoding("UTF-8")
        .linesToSkip(1)
        .delimited()
        .delimiter(",")
        .names("firstName", "lastName", "email", "password", "emailVerified", "dateInscription", "avatar",
            "streetAddress", "city", "country")
        .fieldSetMapper(userCsvDTOMapper())
        .strict(true)
        .build();
  }

  private static FieldSetMapper<UserCsvDTO> userCsvDTOMapper() {
    return new FieldSetMapper<UserCsvDTO>() {

      @Override
      public UserCsvDTO mapFieldSet(FieldSet fieldSet) throws BindException {
        UserCsvDTO userCsvDTO = new UserCsvDTO();

        userCsvDTO.setFirstName(fieldSet.readString("firstName"));
        userCsvDTO.setLastName(fieldSet.readString("lastName"));
        userCsvDTO.setEmail(fieldSet.readString("email"));
        userCsvDTO.setPassword(fieldSet.readString("password"));

        String emailVerified = fieldSet.readString("emailVerified");
        if (!emailVerified.isBlank()) {
          userCsvDTO.setEmailVerified(emailVerified.equalsIgnoreCase("true"));
        }

        userCsvDTO.setDateInscription(parseDateTime(fieldSet));

        userCsvDTO.setAvatar(fieldSet.readString("avatar"));
        userCsvDTO.setStreetAddress(fieldSet.readString("streetAddress"));
        userCsvDTO.setCity(fieldSet.readString("city"));
        userCsvDTO.setCountry(fieldSet.readString("country"));

        return userCsvDTO;
      }

      private static LocalDateTime parseDateTime(FieldSet fieldSet) {
        // dateInscription - 2021-08-21T12:48:03.715Z
        String dateInscription = fieldSet.readString("dateInscription");
        if (!dateInscription.isBlank()) {
          DateTimeFormatter[] formatters = {
              DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX"),
              DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
              DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
          };

          for (DateTimeFormatter dateTimeFormatter : formatters) {
            try {
              return LocalDateTime.parse(dateInscription, dateTimeFormatter);
            } catch (Exception e) {
            }
          }
        }
        return LocalDateTime.now();
      }

    };
  }

}
