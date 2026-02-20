package ru.ffanjex.weatherforecast.service;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.ffanjex.weatherforecast.dto.enums.Sex;
import ru.ffanjex.weatherforecast.model.Advice;
import ru.ffanjex.weatherforecast.model.User;
import ru.ffanjex.weatherforecast.repository.AdviceRepository;
import ru.ffanjex.weatherforecast.repository.UserRepository;

import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AdviceRepository adviceRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void registerUser(String username, String email, String password, Sex sex) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Пользователь уже зарегистрирован");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .sex(sex)
                .build();

        userRepository.save(user);
    }

    public void addAdviceToUser(Integer userId, Integer adviceId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Advice> adviceOpt = adviceRepository.findById(adviceId);

        if (userOpt.isPresent() && adviceOpt.isPresent()) {
            User user = userOpt.get();
            Advice advice = adviceOpt.get();
            user.getAdviceList().add(advice);
            userRepository.save(user);
        }
    }

    public Set<Advice> getUserAdvice(Integer userId) {
        return userRepository.findById(userId)
                .map(User::getAdviceList)
                .orElse(Set.of());
    }
}
