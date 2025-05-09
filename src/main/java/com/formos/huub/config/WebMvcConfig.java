package com.formos.huub.config;

import com.formos.huub.framework.interceptor.SubdomainInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ***************************************************
 * * Description :
 * * File        : WebMvcConfig
 * * Author      : Hung Tran
 * * Date        : Jun 15, 2024
 * ***************************************************
 **/

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private SubdomainInterceptor subdomainInterceptor;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api", HandlerTypePredicate.forBasePackage("com.formos.huub.web.rest.noversion"));
        configurer.addPathPrefix("/api/v1", HandlerTypePredicate.forBasePackage("com.formos.huub.web.rest.v1"));
        configurer.addPathPrefix("/api/v2", HandlerTypePredicate.forBasePackage("com.formos.huub.web.rest.v2"));
        configurer.addPathPrefix("/api/webhook", HandlerTypePredicate.forBasePackage("com.formos.huub.web.rest.webhook"));
    }

    // Register Interceptor
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(subdomainInterceptor).addPathPatterns("/**");
    }
}
