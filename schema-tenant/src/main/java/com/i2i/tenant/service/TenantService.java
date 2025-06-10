package com.i2i.tenant.service;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Service
public class TenantService {

    @Autowired
    private DataSource dataSource;

    public void createTenant(String tenantId) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // Step 1: Create schema
            statement.execute("CREATE SCHEMA IF NOT EXISTS " + tenantId);

            // Step 2: Run Flyway migrations
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(tenantId)
                    .load();
            flyway.migrate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create tenant schema: " + tenantId, e);
        }
    }
}
