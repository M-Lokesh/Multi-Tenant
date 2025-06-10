//package com.i2i.tenant.config;
//
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class FilterConfig {
//
//    @Bean
//    public FilterRegistrationBean<TenantFilter> tenantFilterRegistration(TenantFilter tenantFilter) {
//        FilterRegistrationBean<TenantFilter> registrationBean = new FilterRegistrationBean<>();
//        registrationBean.setFilter(tenantFilter);
//        registrationBean.setOrder(1); // very early
//        registrationBean.addUrlPatterns("/*");
//        return registrationBean;
//    }
//}
