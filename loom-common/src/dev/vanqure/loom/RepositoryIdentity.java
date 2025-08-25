package dev.vanqure.loom;

public record RepositoryIdentity(String keyspace, String table) {

    public static RepositoryIdentity identityOf(final String keyspace, final String table) {
        return new RepositoryIdentity(keyspace, table);
    }
}
