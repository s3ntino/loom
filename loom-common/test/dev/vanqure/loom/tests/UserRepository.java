package dev.vanqure.loom.tests;

public interface UserRepository {

    void insertUser(User user);

    void updateUser(User user);

    void deleteUser(User user);

    User findUserById(String userId);
}
