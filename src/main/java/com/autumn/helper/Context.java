package com.autumn.helper;

import com.autumn.anotation.*;
import com.autumn.config.EntityManagerConfig;
import com.autumn.exceptions.ErrorException;
import jakarta.persistence.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import java.lang.reflect.Method;
import java.util.*;

public class Context {

    private static final Logger logger = LogManager.getLogger(Context.class);
    public static final Map<Class<?>, Object> mapinstances = new HashMap<>();
    public static final List<Class<?>> entities = new ArrayList<>();
    public static Object advice;
    public static void build(String basePackage){
        try {
            List<Class<?>> allClass = ScanPackageImpl.findAllClass(basePackage);

            Optional<Class<?>> maybeAdvice =
                    allClass.stream().filter(c -> c.isAnnotationPresent(ControllerAdvice.class)).findFirst();

            maybeAdvice.ifPresent(aClass -> advice = ReflectionHelper.invoke(aClass, List.of()));

            List<Class<?>> allClazzEntities = allClass.stream()
                    .filter(clazz -> clazz.isAnnotationPresent(Entity.class)).toList();
            entities.addAll(allClazzEntities);

            List<Class<?>> allClazzConfiguration = allClass.stream().filter(clazz -> clazz.isAnnotationPresent(Configuration.class)).toList();
            allClazzConfiguration.forEach( clazz -> {
                Object instanceConfiguration = ReflectionHelper.invoke(clazz, allClazzConfiguration);
                Method[] declaredMethods = clazz.getDeclaredMethods();
                List<Method> methodsBeans = Arrays.stream(declaredMethods)
                        .filter(method -> method.isAnnotationPresent(Bean.class)).toList();
                methodsBeans.forEach(method -> mapinstances
                        .put(EntityManagerConfig.class, ReflectionHelper.invokeMethod(method,instanceConfiguration)));
            });

            List<Class<?>> clazzRepositoryToInstance = allClass.stream()
                    .filter(clazz -> clazz.isAnnotationPresent(Repository.class)).toList();
            clazzRepositoryToInstance.forEach(clazz ->
                    mapinstances.put(clazz, ReflectionHelper.getInstanceDataSource(clazz, mapinstances)));

            List<Class<?>> componentA = allClass.stream().filter(clazz -> clazz.isAnnotationPresent(Component.class))
                    .toList();
            componentA.forEach( clazz -> mapinstances.put(clazz,ReflectionHelper
                    .invoke(clazz, componentA)));

            List<Class<?>> controllerA = allClass.stream().filter(clazz -> clazz.isAnnotationPresent(Controller.class))
                    .toList();

            controllerA.forEach(clazz -> mapinstances.put(clazz, ReflectionHelper
                        .invoke(clazz, componentA)));

        } catch (IOException e) {
            String errorMsg = "Falha ao obter todas as classes do projeto";
            logger.error(errorMsg, e);
            throw new ErrorException(errorMsg,e);
        }
    }


}
