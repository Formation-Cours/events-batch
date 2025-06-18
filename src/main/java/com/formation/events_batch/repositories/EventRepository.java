package com.formation.events_batch.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.formation.events_batch.entities.EventEntity;

public interface EventRepository extends JpaRepository<EventEntity, Long> {
}
