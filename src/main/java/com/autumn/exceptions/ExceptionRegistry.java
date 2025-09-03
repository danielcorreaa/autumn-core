package com.autumn.exceptions;

import com.autumn.anotation.ExceptionHandler;
import org.eclipse.jetty.http.HttpStatus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ExceptionRegistry {
    private final Map<Class<? extends Throwable>, Method> handlers = new HashMap<>();
    private final Object controllerInstance;

    public ExceptionRegistry(Object controllerInstance) {
        this.controllerInstance = controllerInstance;
        scanForHandlers(controllerInstance);
    }

    private void scanForHandlers(Object controller) {
        for (Method method : controller.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(ExceptionHandler.class)) {
                ExceptionHandler ann = method.getAnnotation(ExceptionHandler.class);
                for (Class<? extends Throwable> exClass : ann.value()) {
                    handlers.put(exClass, method);
                }
            }
        }
    }

    public Object handle(Exception ex) {
        // procura um handler específico
        Method method = handlers.get(ex.getClass());

        // fallback: procura um handler genérico (Exception.class)
        if (method == null) {
            method = handlers.get(Exception.class);
        }

        if (method != null) {
            try {
                return method.invoke(controllerInstance, ex);
            } catch (IllegalAccessException | InvocationTargetException e) {
                return new MessageError("", HttpStatus.INTERNAL_SERVER_ERROR_500, e.getMessage(), "");
            }
        }
        return new MessageError("", HttpStatus.INTERNAL_SERVER_ERROR_500, ex.getMessage(), "");
    }
}

