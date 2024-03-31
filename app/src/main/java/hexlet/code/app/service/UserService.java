package hexlet.code.app.service;

import hexlet.code.app.dto.userDto.UserCreateDTO;
import hexlet.code.app.dto.userDto.UserDTO;
import hexlet.code.app.dto.userDto.UserUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    public List<UserDTO> getAll() {
        var users = userRepository.findAll();
        return users.stream()
                .map(userMapper::map)
                .toList();
    }

    public UserDTO getById(long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        return userMapper.map(user);
    }

    public UserDTO create(UserCreateDTO data) {
        var user = userMapper.map(data);
        var hashedPassword = passwordEncoder.encode(data.getPassword());
        user.setHashedPassword(hashedPassword);
        userRepository.save(user);
        return userMapper.map(user);
    }

    public UserDTO update(UserUpdateDTO data, long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));

        userMapper.update(data, user);

        if (data.getPassword() != null) {
            var hashedPassword = passwordEncoder.encode(user.getPassword());
            user.setHashedPassword(hashedPassword);
        }

        userRepository.save(user);
        return userMapper.map(user);
    }

    public void delete(long id) {
        userRepository.deleteById(id);
    }
}
