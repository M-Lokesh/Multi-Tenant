package com.i2i.tenant.service;

import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Service
public class TenantSchemaService {

    private final DataSource dataSource;
    private final Flyway flyway;

    public TenantSchemaService(DataSource dataSource) {
        this.dataSource = dataSource;
        // Default Flyway config (we will clone and use it later)
        this.flyway = Flyway.configure().dataSource(dataSource).load();
    }

    public void ensureSchemaExists(String tenantId) {
        try (Connection connection = dataSource.getConnection();
             ResultSet rs = connection.getMetaData().getSchemas()) {

            boolean exists = false;
            while (rs.next()) {
                String schema = rs.getString("TABLE_SCHEM");
                if (tenantId.equalsIgnoreCase(schema)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("CREATE SCHEMA IF NOT EXISTS " + tenantId);
                }

                // Apply Flyway migrations to the new schema
                Flyway.configure()
                        .dataSource(dataSource)
                        .schemas(tenantId)
                        .load()
                        .migrate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to validate or create schema for tenant: " + tenantId, e);
        }
    }

    public boolean schemaExists(String schemaName) {
        try (Connection connection = dataSource.getConnection();
             var rs = connection.getMetaData().getSchemas()) {

            while (rs.next()) {
                String existingSchema = rs.getString("TABLE_SCHEM");
                if (schemaName.equalsIgnoreCase(existingSchema)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check schema existence", e);
        }

        return false;
    }
}
