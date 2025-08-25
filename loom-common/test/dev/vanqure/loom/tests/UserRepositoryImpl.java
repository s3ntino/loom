package dev.vanqure.loom.tests;

import static com.datastax.oss.driver.api.core.type.DataTypes.TEXT;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.bindMarker;
import static dev.vanqure.loom.RepositoryIdentity.identityOf;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import dev.vanqure.loom.BaseLoomRepository;
import dev.vanqure.loom.OperationNotAppliedException;
import dev.vanqure.loom.RowTransformer;
import dev.vanqure.loom.SessionProvider;

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
        executeQuery(createTableQuery()
                .ifNotExists()
                .withPartitionKey("id", TEXT)
                .withColumn("password", TEXT));
    }

    private static RowTransformer<User> rowTransformer() {
        return row -> new User(row.get("id", String.class), row.get("password", String.class));
    }

    @Override
    public void insertUser(final User user) {
        executeWithSession(session -> {
            final var statement = session.prepare(insertUserQuery).bind(user.id(), user.password());
            final ResultSet resultSet = session.execute(statement);
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
