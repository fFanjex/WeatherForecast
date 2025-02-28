package ru.ffanjex.weatherforecast.service;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.ffanjex.weatherforecast.model.City;
import ru.ffanjex.weatherforecast.model.User;
import ru.ffanjex.weatherforecast.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CityService cityService;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void registerUser(String username, String email, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Пользователь с именем " + username + " уже зарегистрирован");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Пользователь с email " + email + " уже зарегистрирован");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .build();

        userRepository.save(user);
    }


    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public City getCityByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(User::getCity)
                .orElse(null);
    }
}
