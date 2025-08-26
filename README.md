## loom

A lightweight Scylla DSL, crafted for expressive queries without boilerplate.

### Why Loom?

Loom is a lightweight, ultra-flexible Scylla DSL built for **modern Java developers** who hate boilerplate.

- **No Boilerplate:** Define repositories once; CRUD, queries, and table handling are automatic.
- **Full Flexibility:** Run any query, mix custom logic — Loom imposes no limitations.
- **Memory & Performance Conscious:** Queries are constants, lambdas are tiny — negligible overhead, no memory leaks.

With Loom, you can focus on **building features, not repetitive scaffolding**.

### Get started

You can build dependency and append it to your local .m2 directory, by using: `./gradlew publishToMavenLocal`

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

    UserRepositoryImpl() {
        super(identityOf("survival-games", "users"),
                () -> CqlSession.builder().build(),
                keyspace -> createKeyspace(keyspace).ifNotExists().withSimpleStrategy(2),
                row -> new User(
                        row.get("id", String.class),
                        row.get("password", String.class)));
        this.insertUserQuery = insertQuery()
                .value("id", bindMarker())
                .value("password", bindMarker()).build();
        this.updateUserQuery = updateQuery()
                .setColumn("password", bindMarker())
                .whereColumn("id")
                .isEqualTo(bindMarker()).build();
        this.deleteUserQuery =
                deleteQuery().whereColumn("id").isEqualTo(bindMarker()).build();
        this.findUserByIdQuery =
                selectQuery().all().whereColumn("id").isEqualTo(bindMarker()).build();
        executeQuery(
                createTableQuery().ifNotExists()
                        .withPartitionKey("id", TEXT)
                        .withColumn("password", TEXT));
    }

    @Override
    public void insertUser(final User user) {
        executeWithSession(session -> {
            final var statement = session.prepare(insertUserQuery)
                    .bind(user.id(), user.password());
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
            final var statement = session.prepare(updateUserQuery)
                    .bind(user.password(), user.id());
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
            final var statement = session.prepare(deleteUserQuery)
                    .bind(user.id());
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
            final var statement = session.prepare(findUserByIdQuery)
                    .bind(userId);
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


---

![Visitor Count](https://visitor-badge.laobi.icu/badge?page_id=vanqure.loom)
