package com.saukarn.cloudUserApi.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.saukarn.cloudUserApi.model.User;
import com.saukarn.cloudUserApi.repository.UserRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class UserService {

    private final UserRepository repository;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
	  private final Counter usersCreated; private final Counter usersFetched;
	  private final Counter userNotFound; private final Timer dynamoLatency;
	private Counter usersDeleted;
	 
	  //private final Counter userCounter;

    public UserService(UserRepository repository, MeterRegistry registry) {
        this.repository = repository;
        this.usersCreated = registry.counter("users_created_total");
        this.usersFetched = registry.counter("users_fetched_total");
        this.usersDeleted = registry.counter("users_deleted_total");
        this.userNotFound = registry.counter("users_not_found_total");
        this.usersDeleted = registry.counter("users_deleted_total");

        this.dynamoLatency = Timer.builder("dynamodb_latency")
                .description("Latency of DynamoDB operations")
                .publishPercentileHistogram()
                .register(registry);
    }
    

	 
    @CircuitBreaker(name = "userService", fallbackMethod = "fallback")
    @Retry(name = "userService")
    public User getUser(String userId) {
        User user = repository.findById(userId);
        if (user == null) {
        	log.info("userId not found. userId={}",userId);
        	userNotFound.increment();
        	throw new UserNotFoundException(userId);
            
        }
        usersFetched.increment();
        dynamoLatency.record(() -> repository.findById(userId)); 
        return user;
    }

    public void createUser(User user) {
    	user.setUserId(UUID.randomUUID().toString());
        repository.save(user);
        usersCreated.increment();
        dynamoLatency.record(() -> repository.save(user)); 
    }
    
    public boolean deleteUserById(String userId) {
    	usersDeleted.increment();
    	//dynamoLatency.record(() -> repository.save(user)); 
    	return repository.deleteById(userId);
    	
    }

    public User fallback(String userId, Throwable ex) {
        User fallbackUser = new User();
        fallbackUser.setUserId(userId);
        fallbackUser.setName("Unavailable");
        fallbackUser.setEmail("N/A");
        return fallbackUser;
    }
    
    public class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String userId) {
            super("User not found for userId=" + userId);
        }
    }
}

