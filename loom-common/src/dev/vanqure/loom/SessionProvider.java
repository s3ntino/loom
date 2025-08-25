package dev.vanqure.loom;

import com.datastax.oss.driver.api.core.CqlSession;

@FunctionalInterface
public interface SessionProvider {

    CqlSession provideSession();
}
