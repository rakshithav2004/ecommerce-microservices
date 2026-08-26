package com.ecommerce.auth.repository;

import com.ecommerce.auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User user = User.builder()
                .username("rakshitha")
                .email("rakshitha@example.com")
                .password("encoded-password")
                .role("USER")
                .build();

        userRepository.save(user);
    }

    @Test
    void findByEmail_shouldReturnUser() {

        Optional<User> result =
                userRepository.findByEmail(
                        "rakshitha@example.com"
                );

        assertTrue(result.isPresent());
        User user = result.get();
        assertEquals(
                "rakshitha",
                user.getUsername()
        );

        assertEquals(
                "rakshitha@example.com",
                user.getEmail()
        );
        assertEquals(
                "USER",
                user.getRole()
        );
    }


    @Test
    void findByEmail_shouldReturnEmptyWhenUserDoesNotExist() {

        Optional<User> result =
                userRepository.findByEmail(
                        "unknown@example.com"
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void existsByEmail_shouldReturnTrueForExistingUser() {

        boolean exists =
                userRepository.existsByEmail(
                        "rakshitha@example.com"
                );

        assertTrue(exists);
    }


    @Test
    void existsByEmail_shouldReturnFalseForNonExistingUser() {

        boolean exists =
                userRepository.existsByEmail(
                        "unknown@example.com"
                );

        assertFalse(exists);
    }
}