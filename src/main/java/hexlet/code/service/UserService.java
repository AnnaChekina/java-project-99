package hexlet.code.service;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private TaskService taskService;

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
        var user = userMapper.map(userData);

        user.setPasswordDigest(userData.getPassword());

        try {
            userDetailsService.createUser(user);
            return userMapper.map(user);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Email already exists");
        }
    }

    public UserDTO update(UserUpdateDTO userData, Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found: " + id));

        userMapper.update(userData, user);

        if (userData.getPassword() != null && userData.getPassword().isPresent()) {
            String rawPassword = userData.getPassword().get();
            user.setPasswordDigest(rawPassword); // Устанавливаем raw password
        }

        userDetailsService.updateUser(user);
        return userMapper.map(user);
    }

    public void delete(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found: " + id));

        if (taskService.existsByAssigneeId(id)) {
            throw new DataIntegrityViolationException("Cannot delete user: user has assigned tasks");
        }

        userDetailsService.deleteUser(user.getEmail());
    }
}
