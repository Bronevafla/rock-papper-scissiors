package com.simkin.service;

import com.simkin.datamodel.User;
import com.simkin.datamodel.UserStatus;
import com.simkin.repository.UserRepository;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @NonNull
    private final UserRepository userRepository;

    public UserServiceImpl(@NonNull UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Nullable
    @Override
    public User getUserByNickName(@NonNull String nickname) {
        return userRepository.findUserByNickname(nickname);
    }

    @NonNull
    @Override
    public User createUser(@NonNull String nickname) {
        User newUserEntity = new User();
        newUserEntity.setNickname(nickname);
        newUserEntity.setStatus(UserStatus.OFFLINE);
        return userRepository.save(newUserEntity);
    }

    @NonNull
    @Override
    public List<User> getUsersByUserStatus(@NonNull UserStatus userStatus) {
        return userRepository.findUsersByStatus(userStatus);
    }
}
