package com.autumn.helper;

import com.autumn.anotation.Controller;
import com.autumn.anotation.Inject;
import com.autumn.anotation.ResquestPath;
import com.autumn.config.EntityManagerConfig;
import com.autumn.exceptions.ErrorException;
import com.autumn.exceptions.ExceptionRegistry;
import com.autumn.jpa.impl.CrudRepositoryImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;

public class ReflectionHelper {
    public final static String packageController = "com.autumn.controller";
    private static final Logger logger = LogManager.getLogger(ReflectionHelper.class);

    public static Object invokeMethod(Method method, Object object){
       return invokeMethod(method,object,null);
    }

    public static final Map<Class<?>, Object> instances = new HashMap<>();


    public static Object invokeMethod(Method method, Object object, Object... args){
        try {
            if(null == args){
                return method.invoke(object);
            }
            return method.invoke(object, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Throwable causa = e.getCause();
            ExceptionRegistry registry = new ExceptionRegistry(Context.advice);
            return registry.handle((Exception) causa);
        }
    }

    public static Object getInstance(Class<?> clazz){
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new ErrorException("Falha ao instaciar objeto: " + clazz.getName(),e);
        }
    }
    public static Object getInstanceDataSource(Class<?> clazz, Map<Class<?>, Object> mapinstances){
        try {
            if(clazz.isInterface()){
                Type[] genericInterfaces = clazz.getGenericInterfaces();
                ParameterizedType parameterized = (ParameterizedType) genericInterfaces[0];
                Class<?> entityClazz =  (Class<?>) parameterized.getActualTypeArguments()[0];

                CrudRepositoryImpl<?,?> target = new CrudRepositoryImpl<>((EntityManagerConfig) mapinstances.get(EntityManagerConfig.class), entityClazz);

                Object instance = Proxy.newProxyInstance(
                        clazz.getClassLoader(),
                        new Class[]{clazz},
                        (proxy, method, args) -> {
                            Method implMethod = CrudRepositoryImpl.class.getMethod(method.getName(), method.getParameterTypes());
                            return implMethod.invoke(target, args);
                        });

                instances.put(clazz, instance);

                return instance;
            }

            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new ErrorException("Falha ao instaciar objeto: " + clazz.getName(),e);
        }
    }


    public static Object invoke(Class<?> clazz, List<Class<?>> clazzToInstance) throws ErrorException{
        try {

            if(instances.containsKey(clazz)){
                return clazz.cast(instances.get(clazz));
            }
            Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();

            Optional<Constructor<?>> maybeConstructor = Arrays.stream(declaredConstructors)
                    .filter( constructor -> constructor.isAnnotationPresent(Inject.class)).findFirst();
            Constructor<?> constructor = null;
            if(maybeConstructor.isEmpty()){
                if(clazz.isInterface()){
                    Optional<Class<?>> clazzImpl = clazzToInstance.stream().filter(clazz::isAssignableFrom).findFirst();
                    if(clazzImpl.isPresent()){
                        constructor = clazzImpl.get().getDeclaredConstructor();
                    }
                } else {
                    constructor = clazz.getDeclaredConstructor();
                }
            } else {
                constructor = maybeConstructor.get();
            }
            assert constructor != null;
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            List<Object> objects = new ArrayList<>();
            Arrays.stream(parameterTypes).forEach( type -> objects.add(invoke(type, clazzToInstance)));

            Object instance = clazz.cast(constructor.newInstance(objects.toArray()));
            instances.put(clazz, instance);
            return instance;
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new ErrorException("Falha ao instaciar objeto: " + clazz.getName(),e);
        }
    }

    public static Optional<Class<?>> getController(String beginPath,  Map<Class<?>, Object> instances) {
       List<Class<?>> classes =  instances.keySet().stream().toList();
       return classes.stream().filter(clazz -> clazz.isAnnotationPresent(Controller.class))
                .filter(controller -> controller.getAnnotation(ResquestPath.class)
                        .value().equalsIgnoreCase(beginPath))
                .findFirst();
    }

    public static List<Class<?>> getClasses(){
        return getClasses(packageController);
    }

    public static List<Class<?>> getClasses(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);

        if (resource == null) {
            logger.error("Pacote não encontrado: {}", path);
            return classes;
        }

        File directory = new File(resource.getFile());

        if (!directory.exists()) {
            return classes;
        }

        for (String file : Objects.requireNonNull(directory.list())) {
            if (file.endsWith(".class")) {
                try {
                    String className = packageName + "." + file.substring(0, file.length() - 6);
                    Class<?> clazz = Class.forName(className);
                    classes.add(clazz);
                } catch (ClassNotFoundException e) {
                    logger.error("Erro ao carregar classe: {}" , e.getMessage());
                }
            }
        }
        return classes;
    }
}
