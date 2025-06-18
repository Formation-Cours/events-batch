package com.formation.events_batch.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private String firstName;

  @Column(nullable = false)
  private String lastName;

  @OneToMany(mappedBy = "organizer")
  private List<EventEntity> organizedEvents = new ArrayList<>();

  @ManyToMany(mappedBy = "participants")
  private List<EventEntity> participatingEvents = new ArrayList<>();

  // mail
  @Column(length = 100)
  private String emailToken;

  @Column(nullable = false)
  private boolean emailVerified = false;

  private LocalDateTime verificationTokenExpiredAt;

}
