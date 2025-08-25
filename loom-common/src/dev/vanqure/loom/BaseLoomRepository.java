package dev.vanqure.loom;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.querybuilder.BuildableQuery;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import com.datastax.oss.driver.api.querybuilder.delete.DeleteSelection;
import com.datastax.oss.driver.api.querybuilder.insert.InsertInto;
import com.datastax.oss.driver.api.querybuilder.schema.AlterKeyspaceStart;
import com.datastax.oss.driver.api.querybuilder.schema.AlterTableStart;
import com.datastax.oss.driver.api.querybuilder.schema.CreateKeyspaceStart;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTableStart;
import com.datastax.oss.driver.api.querybuilder.schema.Drop;
import com.datastax.oss.driver.api.querybuilder.select.SelectFrom;
import com.datastax.oss.driver.api.querybuilder.truncate.Truncate;
import com.datastax.oss.driver.api.querybuilder.update.UpdateStart;

public abstract class BaseLoomRepository<V> {

    private final SessionProvider sessionProvider;
    private final RowTransformer<V> rowTransformer;
    private final CqlIdentifier keyspace;
    private final CqlIdentifier table;

    protected BaseLoomRepository(
            final RepositoryIdentity identity,
            final SessionProvider sessionProvider,
            final KeyspaceValidator keyspaceValidator,
            final RowTransformer<V> rowTransformer) {
        this.sessionProvider = sessionProvider;
        this.keyspace = CqlIdentifier.fromCql(identity.keyspace());
        this.table = CqlIdentifier.fromCql(identity.table());
        this.rowTransformer = rowTransformer;
        executeQuery(keyspaceValidator.validateKeyspace(keyspace));
    }

    protected void executeQuery(final BuildableQuery buildableQuery) {
        executeWithSession(session -> session.execute(buildableQuery.build()));
    }

    protected void executeWithSession(final SessionExecutor sessionExecutor) throws SessionOperationException {
        try (final CqlSession session = sessionProvider.provideSession()) {
            sessionExecutor.execute(session);
        } catch (final Exception exception) {
            throw new SessionOperationException("Failed to execute session operation", exception);
        }
    }

    protected <R> R applyWithSession(final SessionTransformer<R> sessionFunction) throws SessionOperationException {
        try (final CqlSession session = sessionProvider.provideSession()) {
            return sessionFunction.transformSession(session);
        } catch (final Exception exception) {
            throw new SessionOperationException("Failed to transform session operation", exception);
        }
    }

    protected V transformRow(final Row row) {
        try {
            return rowTransformer.transformRow(row);
        } catch (final Exception exception) {
            throw new RowTransformationException("Failed to transform row %s.".formatted(row), exception);
        }
    }

    protected CreateTableStart createTableQuery() {
        return SchemaBuilder.createTable(keyspace, table);
    }

    protected AlterTableStart alterTableQuery() {
        return SchemaBuilder.alterTable(keyspace, table);
    }

    protected Drop dropTableQuery() {
        return SchemaBuilder.dropTable(keyspace, table);
    }

    protected CreateKeyspaceStart createKeyspaceQuery() {
        return SchemaBuilder.createKeyspace(keyspace);
    }

    protected AlterKeyspaceStart alterKeyspaceQuery() {
        return SchemaBuilder.alterKeyspace(keyspace);
    }

    protected Drop dropKeyspaceQuery() {
        return SchemaBuilder.dropKeyspace(keyspace);
    }

    protected InsertInto insertQuery() {
        return QueryBuilder.insertInto(keyspace, table);
    }

    protected UpdateStart updateQuery() {
        return QueryBuilder.update(keyspace, table);
    }

    protected SelectFrom selectQuery() {
        return QueryBuilder.selectFrom(keyspace, table);
    }

    protected Truncate truncateQuery() {
        return QueryBuilder.truncate(keyspace, table);
    }

    protected DeleteSelection deleteQuery() {
        return QueryBuilder.deleteFrom(keyspace, table);
    }
}
