package com.i2i.tenant.config;

import com.i2i.tenant.TenantApplication;
import org.hibernate.cfg.Environment;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
public class HibernateConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public MultiTenantConnectionProvider multiTenantConnectionProvider() {
        return new SchemaMultiTenantConnectionProvider(dataSource);
    }

    @Bean
    public CurrentTenantIdentifierResolver tenantIdentifierResolver() {
        return new SchemaBasedTenantIdentifierResolver();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            MultiTenantConnectionProvider connectionProvider,
            CurrentTenantIdentifierResolver tenantResolver) {

        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.multiTenancy", "SCHEMA");
        props.put(Environment.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
        props.put(Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantResolver);
        props.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        props.put(Environment.SHOW_SQL, true);

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan(TenantApplication.class.getPackage().getName());
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emf.setJpaPropertyMap(props);
        return emf;
    }
}
