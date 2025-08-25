package dev.vanqure.loom;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.querybuilder.BuildableQuery;

@FunctionalInterface
public interface KeyspaceValidator {

    BuildableQuery validateKeyspace(CqlIdentifier keyspace);
}
