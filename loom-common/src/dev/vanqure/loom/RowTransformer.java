package dev.vanqure.loom;

import com.datastax.oss.driver.api.core.cql.Row;

@FunctionalInterface
public interface RowTransformer<R> {

    R transformRow(Row row) throws Exception;
}
