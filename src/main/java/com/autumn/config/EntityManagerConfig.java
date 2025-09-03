package com.autumn.config;

import com.autumn.helper.ApplicationPropertiesLoader;
import com.autumn.helper.Context;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceUnitInfo;
import org.hibernate.jpa.HibernatePersistenceProvider;


import java.util.Map;

public class EntityManagerConfig {

    public static EntityManagerFactory createEntityManagerFactory() {
        Map<String, String> props = ApplicationPropertiesLoader.loadJpaProperties();      ;
        var clazzNames = Context.entities.stream().map(Class::getName).toList();
        PersistenceUnitInfo info = new SimplePersistenceUnitInfo("autumn-unit",
                clazzNames, props);
        return new HibernatePersistenceProvider().createContainerEntityManagerFactory(info, props);
    }

    public EntityManager entityManager(){
        return createEntityManagerFactory().createEntityManager();
    }
}
