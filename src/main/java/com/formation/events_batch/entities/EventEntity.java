package com.formation.events_batch.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

// lombok
@Data
// jpa
@Entity
@Table(name = "events")
@EntityListeners(AuditingEntityListener.class)
public class EventEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(length = 1000)
  private String description;

  @Column(nullable = false)
  private LocalDateTime startDate;

  private LocalDateTime endDate;

  @Column(nullable = false)
  private String location;

  private Integer maxParticipants;

  @ManyToOne
  @JoinColumn(name = "organizer_id", nullable = false)
  @ToString.Exclude
  private UserEntity organizer;

  @ManyToMany
  @JoinTable(name = "event_participants", joinColumns = @JoinColumn(name = "event_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
  @ToString.Exclude
  private List<UserEntity> participants = new ArrayList<>();

  @CreatedDate
  private LocalDateTime createdDate;

  @LastModifiedDate
  private LocalDateTime modifiedDate;

  // @CreatedBy
  // private UserEntity createdBy;

  // @LastModifiedBy
  // private UserEntity modifiedBy;
}
