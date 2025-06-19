package com.formation.events_batch.dto;

import java.time.LocalDateTime;

import lombok.Data;

// firstName,lastName,email,password,emailVerified,dateInscription,avatar,streetAddress,city,country;

@Data
public class UserCsvDTO {
  private String firstName;

  private String lastName;

  private String email;

  private String password;

  private boolean emailVerified;

  private LocalDateTime dateInscription;

  private String avatar;

  private String streetAddress;

  private String city;

  private String country;
}
