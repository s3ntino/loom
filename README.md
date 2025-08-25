## loom

A lightweight Scylla DSL, crafted for expressive queries without boilerplate.

### Get started

You can build dependency and append it to your local .m2 directory, by using: ./gradlew publishToMavenLocal

### Using Loom

Loom in action:

```java
public record User(String id, String password) {}

public interface UserRepository {

    void insertUser(User user);

    void updateUser(User user);

    void deleteUser(User user);

    User findUserById(String userId);
}

final class UserRepositoryImpl extends BaseLoomRepository<User> implements UserRepository {

    private final SimpleStatement findUserByIdQuery;
    private final SimpleStatement insertUserQuery;
    private final SimpleStatement updateUserQuery;
    private final SimpleStatement deleteUserQuery;

    UserRepositoryImpl(final SessionProvider sessionProvider) {
        super(identityOf("survival-games", "users"), sessionProvider, rowTransformer());
        this.insertUserQuery = insertQuery()
                .value("id", bindMarker())
                .value("password", bindMarker())
                .build();
        this.updateUserQuery = updateQuery()
                .setColumn("password", bindMarker())
                .whereColumn("id")
                .isEqualTo(bindMarker())
                .build();
        this.deleteUserQuery =
                deleteQuery().whereColumn("id").isEqualTo(bindMarker()).build();
        this.findUserByIdQuery =
                selectQuery().all().whereColumn("id").isEqualTo(bindMarker()).build();
        executeQuery(
                createTableQuery().ifNotExists().withPartitionKey("id", TEXT).withColumn("password", TEXT));
    }

    private static RowTransformer<User> rowTransformer() {
        return row -> new User(row.get("id", String.class), row.get("password", String.class));
    }

    @Override
    public void insertUser(final User user) {
        executeWithSession(session -> {
            final var statement = session.prepare(insertUserQuery).bind(user.id(), user.password());
            final var resultSet = session.execute(statement);
            if (!resultSet.wasApplied()) {
                throw new OperationNotAppliedException(
                        "Couldn't insert user identified by id %s.".formatted(user.id()));
            }
        });
    }

    @Override
    public void updateUser(final User user) {
        executeWithSession(session -> {
            final var statement = session.prepare(updateUserQuery).bind(user.password(), user.id());
            final var resultSet = session.execute(statement);
            if (!resultSet.wasApplied()) {
                throw new OperationNotAppliedException(
                        "Couldn't update user identified by id %s.".formatted(user.id()));
            }
        });
    }

    @Override
    public void deleteUser(final User user) {
        executeWithSession(session -> {
            final var statement = session.prepare(deleteUserQuery).bind(user.id());
            final var resultSet = session.execute(statement);
            if (!resultSet.wasApplied()) {
                throw new OperationNotAppliedException(
                        "Couldn't delete user identified by id %s.".formatted(user.id()));
            }
        });
    }

    @Override
    public User findUserById(final String userId) {
        return applyWithSession(session -> {
            final var statement = session.prepare(findUserByIdQuery).bind(userId);
            final var userRow = session.execute(statement).one();
            if (userRow == null) {
                throw new UserNotFoundException("User identified by id %s couldn't be found.".formatted(userId));
            }

            return transformRow(userRow);
        });
    }
}

final class UserNotFoundException extends IllegalArgumentException {

    UserNotFoundException(final String message) {
        super(message);
    }
}
```