package com.simkin.service;

import com.simkin.datamodel.User;
import com.simkin.datamodel.UserStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

public interface UserService {

    @NonNull
    User createUser(@NonNull String nickname);

    @Nullable
    User getUserByNickName(@NonNull String nickname);

    @NonNull
    List<User> getUsersByUserStatus(@NonNull UserStatus userStatus);
}
