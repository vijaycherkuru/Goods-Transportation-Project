package com.gdc.user_registration_and_authentication.repository;

import com.gdc.user_registration_and_authentication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);


    Optional<User> findByPhone(String phone);

    Optional<User> findByUsernameOrEmail(String username, String email);
    Optional<User> getUserById(UUID userId);

    Optional<User> findByUsernameOrEmailOrPhone(String username, String email, String phone);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}