package com.saukarn.cloudUserApi.controller;

import java.net.URI;
//import java.util.logging.Logger;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saukarn.cloudUserApi.model.User;
import com.saukarn.cloudUserApi.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class CloudUserController {
	private final UserService service;
	private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public CloudUserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody @Valid User user) {
        service.createUser(user);
        URI location = URI.create("/api/v1/users/" + user.getUserId());
        log.info("user_created userId={} email={}", user.getUserId(), user.getEmail());
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> get(@PathVariable String id) {
    	try {
            UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid userId format. Must be UUID.");
        }
    	log.info("Searching UserId={}",id);
    	 return ResponseEntity.ok(service.getUser(id));
        
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id){
    	boolean deleted = service.deleteUserById(id);
    	return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
    
    public class BadRequestException extends RuntimeException {
        public BadRequestException(String message) { super(message); }
    }

}
