package hexlet.code.service;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<UserDTO> getAll() {
        var users = userRepository.findAll();
        return users.stream()
                .map(userMapper::map)
                .toList();
    }

    public UserDTO findById(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found: " + id));
        return userMapper.map(user);
    }

    public UserDTO create(UserCreateDTO userData) {
        if (userData.getPassword() == null || userData.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        var user = userMapper.map(userData);
        user.setPasswordDigest(passwordEncoder.encode(userData.getPassword()));

        try {
            userRepository.save(user);
            return userMapper.map(user);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Email already exists: " + userData.getEmail());
        }
    }

    public UserDTO update(UserUpdateDTO userData, Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found: " + id));

        userMapper.update(userData, user);

        if (userData.getPassword() != null && userData.getPassword().isPresent()) {
            String newPassword = userData.getPassword().get();
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                user.setPasswordDigest(passwordEncoder.encode(newPassword));
            }
        }

        try {
            userRepository.save(user);
            return userMapper.map(user);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Email already exists");
        }
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
