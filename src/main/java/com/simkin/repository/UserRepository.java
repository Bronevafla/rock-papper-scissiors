package com.simkin.repository;

import com.simkin.datamodel.User;
import com.simkin.datamodel.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    User findUserByNickname(@NonNull String nickname);

    List<User> findUsersByStatus(@NonNull UserStatus status);
}
