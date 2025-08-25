package dev.vanqure.loom;

import com.datastax.oss.driver.api.core.CqlSession;

@FunctionalInterface
public interface SessionTransformer<R> {

    R transformSession(CqlSession cqlSession) throws Exception;
}
