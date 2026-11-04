package com.cybermanager.infrastructure.out.adapters.persistences;

import java.net.URI;

record ConnectionTarget(String host, int port, String database) {

    static ConnectionTarget from(String jdbcUrl) {
        try {
            URI uri = URI.create(jdbcUrl.substring("jdbc:".length()));
            String database = uri.getPath().replaceFirst("^/", "");
            int port = uri.getPort() < 0 ? 5432 : uri.getPort();
            if (!"postgresql".equals(uri.getScheme()) || uri.getHost() == null || database.isBlank()) {
                throw new IllegalArgumentException("URL JDBC PostgreSQL invalide");
            }
            return new ConnectionTarget(uri.getHost(), port, database);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("URL JDBC PostgreSQL invalide pour les sauvegardes", exception);
        }
    }

}
