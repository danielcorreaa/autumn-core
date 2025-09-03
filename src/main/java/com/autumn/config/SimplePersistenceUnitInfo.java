package com.autumn.config;

import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.spi.PersistenceUnitTransactionType;
import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;

import javax.sql.DataSource;
import java.net.URL;
import java.util.*;

public class SimplePersistenceUnitInfo implements PersistenceUnitInfo {
    private final String persistenceUnitName;
    private final List<String> managedClassNames;
    private final Properties properties;

    public SimplePersistenceUnitInfo(String persistenceUnitName, List<String> managedClassNames, Map<String, ?> props) {
        this.persistenceUnitName = persistenceUnitName;
        this.managedClassNames = managedClassNames;
        this.properties = new Properties();
        this.properties.putAll(props);
    }

    @Override public String getPersistenceUnitName() { return persistenceUnitName; }
    @Override public String getPersistenceProviderClassName() { return null; }
    @Override public PersistenceUnitTransactionType getTransactionType() { return PersistenceUnitTransactionType.RESOURCE_LOCAL; }
    @Override public DataSource getJtaDataSource() { return null; }
    @Override public DataSource getNonJtaDataSource() { return null; }
    @Override public List<String> getMappingFileNames() { return Collections.emptyList(); }
    @Override public List<URL> getJarFileUrls() { return Collections.emptyList(); }
    @Override public URL getPersistenceUnitRootUrl() { return null; }
    @Override public List<String> getManagedClassNames() { return managedClassNames; }
    @Override public boolean excludeUnlistedClasses() { return false; }
    @Override public SharedCacheMode getSharedCacheMode() { return SharedCacheMode.ENABLE_SELECTIVE; }
    @Override public ValidationMode getValidationMode() { return ValidationMode.AUTO; }
    @Override public Properties getProperties() { return properties; }
    @Override public String getPersistenceXMLSchemaVersion() { return "2.2"; }
    @Override public ClassLoader getClassLoader() { return Thread.currentThread().getContextClassLoader(); }
    @Override public void addTransformer(ClassTransformer transformer) {}
    @Override public ClassLoader getNewTempClassLoader() { return Thread.currentThread().getContextClassLoader(); }
}
