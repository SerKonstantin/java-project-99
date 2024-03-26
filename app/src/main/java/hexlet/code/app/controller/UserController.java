package hexlet.code.app.controller;

import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping(path = "")
    public List<User> index() {
        createDummyUser();          // DELETE
        return userRepository.findAll();
    }

    @GetMapping(path = "/{id}")
    public User show(@PathVariable long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        return user;
    }

    // Tmp dummy
    private User createDummyUser() {
        var user = new User();
        user.setFirstName("Arya");
        user.setLastName("Stark");
        user.setEmail("arya@gmail.com");
        user.setPassword("some_password");
        userRepository.save(user);
        return user;
    }

}
