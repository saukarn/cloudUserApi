package com.saukarn.cloudUserApi.repository;

import org.springframework.stereotype.Repository;

import com.saukarn.cloudUserApi.model.User;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;

@Repository
public class UserRepository {

    private final DynamoDbTable<User> table;

    public UserRepository(DynamoDbEnhancedClient client) {
        this.table = client.table("users", TableSchema.fromBean(User.class));
    }

    public void save(User user) {
        table.putItem(user);
    }

    public User findById(String userId) {
        return table.getItem(Key.builder().partitionValue(userId).build());
    }
    
    public boolean deleteById(String userId) {
        Key key = Key.builder()
                .partitionValue(userId)
                .build();

        // deleteItem returns the deleted item (or null if it didn't exist)
        User deleted = table.deleteItem(r -> r.key(key));
        return deleted != null;
    }
}

