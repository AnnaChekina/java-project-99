package hexlet.code.service.impl;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.CustomUserDetailsService;
import hexlet.code.service.TaskService;
import hexlet.code.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CustomUserDetailsService userDetailsService;
    private final TaskService taskService;

    @Override
    public List<UserDTO> getAll() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::map)
                .toList();
    }

    @Override
    public UserDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found: " + id));
        return userMapper.map(user);
    }

    @Override
    public UserDTO create(UserCreateDTO userData) {
        User user = userMapper.map(userData);
        user.setPasswordDigest(userData.getPassword());

        try {
            userDetailsService.createUser(user);
            return userMapper.map(user);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Email already exists");
        }
    }

    @Override
    public UserDTO update(UserUpdateDTO userData, Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found: " + id));

        userMapper.update(userData, user);

        if (userData.getPassword() != null && userData.getPassword().isPresent()) {
            String rawPassword = userData.getPassword().get();
            user.setPasswordDigest(rawPassword); // Устанавливаем raw password
        }

        userDetailsService.updateUser(user);
        return userMapper.map(user);
    }

    @Override
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found: " + id));

        if (taskService.existsByAssigneeId(id)) {
            throw new DataIntegrityViolationException("Cannot delete user: user has assigned tasks");
        }

        userDetailsService.deleteUser(user.getEmail());
    }
}
