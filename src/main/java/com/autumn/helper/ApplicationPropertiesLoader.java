package com.autumn.helper;
import com.autumn.anotation.Component;
import com.autumn.exceptions.ErrorException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ApplicationPropertiesLoader {

    public static final String AUTUMN_DATASOURCE = "autumn.datasource.";
    private static Properties props;

    public static Map<String, String> loadJpaProperties() {
        props = new Properties();
        try (InputStream in = ApplicationPropertiesLoader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            props.load(in);
        } catch (IOException e) {
            throw new ErrorException("Erro ao carregar application.properties", e);
        }

        String dbType = props.getProperty("autumn.datasource.type");
        if (dbType == null) throw new ErrorException("autumn.datasource.type não definido");

        Map<String, String> config = new HashMap<>();

        // JPA apenas para bancos relacionais
        if (!"mongodb".equalsIgnoreCase(dbType)) {
            config.put("jakarta.persistence.jdbc.driver", props.getProperty(AUTUMN_DATASOURCE + dbType + ".driver"));
            config.put("jakarta.persistence.jdbc.url", props.getProperty(AUTUMN_DATASOURCE + dbType + ".url"));
            config.put("jakarta.persistence.jdbc.user", props.getProperty(AUTUMN_DATASOURCE + dbType + ".user"));
            config.put("jakarta.persistence.jdbc.password", props.getProperty(AUTUMN_DATASOURCE + dbType + ".password"));

            config.put("hibernate.hbm2ddl.auto", props.getProperty("autumn.jpa.hibernate.hbm2ddl", "create"));
            config.put("hibernate.show_sql", props.getProperty("autumn.jpa.show_sql", "true"));
            config.put("hibernate.dialect", props.getProperty("autumn.jpa.hibernate.dialect", "org.hibernate.dialect.H2Dialect"));

        }

        return config;
    }

    public static boolean isMongoDb() {
        Properties props = new Properties();
        try (InputStream in = ApplicationPropertiesLoader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            props.load(in);
        } catch (IOException e) {
            throw new ErrorException("Erro ao carregar application.properties", e);
        }

        String type = props.getProperty("autumn.datasource.type");
        return "mongodb".equalsIgnoreCase(type);
    }

    public static Integer serverPort(){
        return Integer.valueOf(get("server.port"));
    }

    public static String getMongoUri() {
        return get("autumn.datasource.mongodb.uri");
    }

    public static String getMongoDatabase() {
        return get("autumn.datasource.mongodb.database");
    }

    private static String get(String chave) {
        Properties props = new Properties();
        try (InputStream in = ApplicationPropertiesLoader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            props.load(in);
            return props.getProperty(chave);
        } catch (IOException e) {
            throw new ErrorException("Erro ao carregar chave: " + chave, e);
        }
    }
}
