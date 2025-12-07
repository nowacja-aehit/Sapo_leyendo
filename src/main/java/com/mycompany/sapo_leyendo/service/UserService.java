package com.mycompany.sapo_leyendo.service;

import com.mycompany.sapo_leyendo.dto.UserRequest;
import com.mycompany.sapo_leyendo.model.Role;
import com.mycompany.sapo_leyendo.model.User;
import com.mycompany.sapo_leyendo.repository.RoleRepository;
import com.mycompany.sapo_leyendo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public User createUser(UserRequest userRequest) {
        User user = new User();
        user.setLogin(userRequest.getLogin());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setActive(userRequest.isActive());
        
        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
        }
        
        if (userRequest.getRoleIds() != null) {
            List<Role> roles = roleRepository.findAllById(userRequest.getRoleIds());
            user.setRoles(new HashSet<>(roles));
        }
        
        return userRepository.save(user);
    }

    public Optional<User> updateUser(Integer id, UserRequest userRequest) {
        return userRepository.findById(id).map(user -> {
            user.setLogin(userRequest.getLogin());
            user.setFirstName(userRequest.getFirstName());
            user.setLastName(userRequest.getLastName());
            user.setActive(userRequest.isActive());
            
            if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
                user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
            }
            
            if (userRequest.getRoleIds() != null) {
                List<Role> roles = roleRepository.findAllById(userRequest.getRoleIds());
                user.setRoles(new HashSet<>(roles));
            }
            
            return userRepository.save(user);
        });
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }
}
