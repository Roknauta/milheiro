package com.roknauta.milheiro.support;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class BeanLocator {

    private static ApplicationContext context;

    public BeanLocator(ApplicationContext applicationContext) {
        BeanLocator.context = applicationContext;
    }

    public static <T> T getBean(Class<T> beanType) {
        if (context == null) {
            throw new IllegalStateException(
                    "ApplicationContext ainda não inicializado"
            );
        }
        return context.getBean(beanType);
    }
}
