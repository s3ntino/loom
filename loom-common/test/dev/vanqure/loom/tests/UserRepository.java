package dev.vanqure.loom.tests;

import dev.vanqure.loom.CrudLoomRepository;

public interface UserRepository extends CrudLoomRepository {

    void insertUser(User user);

    void updateUser(User user);

    void deleteUser(User user);

    User findUserById(String userId);
}
