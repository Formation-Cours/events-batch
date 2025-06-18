package com.formation.events_batch.service;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.formation.events_batch.entities.UserEntity;
import com.formation.events_batch.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class UserService {

  private static final Logger logger = LoggerFactory.getLogger(UserService.class);
  private static final ConcurrentHashMap<String, UserEntity> USER_CACHE = new ConcurrentHashMap<>();
  private final UserRepository userRepository;

  public UserEntity getOrCreateUser(String email) {
    return USER_CACHE.computeIfAbsent(email, this::createUserSafely);
  }

  private UserEntity createUserSafely(String email) {
    return userRepository.findByEmail(email).orElseGet(() -> {
      try {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setFirstName("System");
        user.setLastName("Event");
        user.setPassword("samsamsam");
        UserEntity savedUser = userRepository.save(user);
        logger.debug("Utilisateur créé: {}", email);
        return savedUser;
      } catch (DataIntegrityViolationException e) {
        logger.debug("Utilisateur déjà créé par un autre thread, récupération depuis la DB");
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Impossible de créer ou récupérer l'utilisateur: " + email));
      }
    });
  }
}
