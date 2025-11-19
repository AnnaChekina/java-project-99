package hexlet.code.service;

import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsManager {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    public void createUser(UserDetails userData) {
        var user = (User) userData;

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new DataIntegrityViolationException("Email already exists: " + user.getEmail());
        }

        String rawPassword = user.getPasswordDigest();
        user.setPasswordDigest(passwordEncoder.encode(rawPassword));

        userRepository.save(user);
    }

    @Override
    public void updateUser(UserDetails user) {
        var existingUser = (User) user;

        if (!existingUser.getPasswordDigest().startsWith("$2a$")) {
            String rawPassword = existingUser.getPasswordDigest();
            existingUser.setPasswordDigest(passwordEncoder.encode(rawPassword));
        }

        userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(String username) {
        var user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        userRepository.delete(user);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        throw new UnsupportedOperationException("Password change not implemented");
    }

    @Override
    public boolean userExists(String username) {
        return userRepository.findByEmail(username).isPresent();
    }

    public User createUserWithRawPassword(User user, String rawPassword) {
        user.setPasswordDigest(rawPassword);
        createUser(user);
        return user;
    }
}
