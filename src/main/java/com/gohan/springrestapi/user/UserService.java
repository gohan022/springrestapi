package com.gohan.springrestapi.user;

import com.gohan.springrestapi.entities.Role;
import com.gohan.springrestapi.entities.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public void register(User user) {
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        user.setEnabled(true);
        this.save(user);
    }

    public User save(User todo) {
        return userRepository.save(todo);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Long numberOfUsers(){
        return userRepository.count();
    }
}
