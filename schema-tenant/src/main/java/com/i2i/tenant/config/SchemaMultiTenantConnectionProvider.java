package com.i2i.tenant.config;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class SchemaMultiTenantConnectionProvider implements MultiTenantConnectionProvider {

    private final DataSource dataSource;

    public SchemaMultiTenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(Object tenantIdentifier) throws SQLException {
        final Connection connection = getAnyConnection();
        connection.setSchema((String) tenantIdentifier);
        return connection;
    }

    @Override
    public void releaseConnection(Object tenantIdentifier, Connection connection) throws SQLException {
        connection.setSchema(SchemaBasedTenantIdentifierResolver.DEFAULT_SCHEMA);
        releaseAnyConnection(connection);
    }

    @Override public boolean supportsAggressiveRelease() { return false; }
    @Override public boolean isUnwrappableAs(Class<?> unwrapType) { return false; }
    @Override public <T> T unwrap(Class<T> unwrapType) { return null; }
}
