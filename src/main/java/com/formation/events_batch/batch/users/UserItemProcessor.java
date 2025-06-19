package com.formation.events_batch.batch.users;

import org.apache.catalina.User;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.formation.events_batch.dto.UserCsvDTO;
import com.formation.events_batch.entities.UserEntity;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserItemProcessor implements ItemProcessor<UserCsvDTO, UserEntity> {
  @Override
  public UserEntity process(UserCsvDTO item) throws Exception {
    if (!isValid(item)) {
      return null;
    }

    UserEntity user = new UserEntity();
    user.setEmail(item.getEmail());
    // ne pas oublier de le chiffrer
    user.setPassword(item.getPassword());
    user.setFirstName(item.getFirstName());
    user.setLastName(item.getLastName());
    user.setEmailVerified(item.isEmailVerified());

    return user;
  }

  private boolean isValid(UserCsvDTO userCsvDTO) {
    return !userCsvDTO.getEmail().isBlank() &&
        !userCsvDTO.getPassword().isBlank() &&
        !userCsvDTO.getFirstName().isBlank() &&
        !userCsvDTO.getLastName().isBlank() &&
        userCsvDTO.getEmail().contains("@");

    // return StringUtils.hasText(userCsvDTO.getEmail()) &&
    // StringUtils.hasText(userCsvDTO.getPassword()) &&
    // StringUtils.hasText(userCsvDTO.getFirstName()) &&
    // StringUtils.hasText(userCsvDTO.getLastName()) &&
    // userCsvDTO.getEmail().contains("@");
  }

}
