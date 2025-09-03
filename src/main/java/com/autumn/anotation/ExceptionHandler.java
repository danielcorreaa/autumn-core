package com.autumn.anotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionHandler {
    Class<? extends Throwable>[] value(); // quais exceções esse método trata
}

