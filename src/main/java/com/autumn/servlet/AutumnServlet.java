package com.autumn.servlet;


import com.autumn.exceptions.ErrorException;
import com.autumn.helper.Context;
import com.autumn.anotation.*;
import com.autumn.helper.ReflectionHelper;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@WebServlet("/*")
public class AutumnServlet extends HttpServlet {

    public static final String UTF_8 = "UTF-8";
    private static final Logger logger = LogManager.getLogger(AutumnServlet.class);
    public static final String CONTENT_TYPE = "application/json";
    public static final String BARRA = "/";

    private final Gson gson = new Gson();


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String contextPath =  request.getPathInfo();
        String beginPath = BARRA + contextPath.split("/")[1];
        String pathAfterRoot = contextPath.replaceFirst(beginPath, "");

        Optional<Class<?>> clazzToInstance = ReflectionHelper.getController(beginPath, Context.mapinstances);
        try {
            if (clazzToInstance.isPresent()) {
                var instanceObj = Context.mapinstances.get(clazzToInstance.get());
                Method[] declaredMethods = instanceObj.getClass().getDeclaredMethods();
                Object retorno = null;
                for (Method method : declaredMethods) {
                    if (method.isAnnotationPresent(GetMapping.class)) {
                        GetMapping getMapping = method.getAnnotation(GetMapping.class);
                        Parameter[] parameters = method.getParameters();
                        if (parameters.length != 0) {
                            var valueGetPath = getMapping.value();
                            String[] splitParameter = valueGetPath.split("/");
                            String[] spltPathAfterRoot = pathAfterRoot.split("/");
                            Map<String, String> mapParameter = new HashMap<>();
                            for (int i = 0; i < splitParameter.length; i++) {
                                if (splitParameter[i].startsWith("{") && splitParameter[i].endsWith("}")) {
                                    String result = splitParameter[i].replaceAll("^\\{([^}]*)\\}$", "$1");
                                    mapParameter.put(result, spltPathAfterRoot[i]);
                                }
                            }
                            var responseParam = Arrays.stream(parameters).filter(parameter -> parameter.isAnnotationPresent(PathVariable.class))
                                    .map(parameter -> {
                                        PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
                                        if (pathVariable.value() != null && !pathVariable.value().isEmpty()) {
                                            return mapParameter.get(pathVariable.value());
                                        } else {
                                            return mapParameter.get(parameter.getName());
                                        }
                                    }).toList();

                            retorno = ReflectionHelper.invokeMethod(method, instanceObj, responseParam.toArray());
                        } else {
                            if (getMapping.value().equalsIgnoreCase(pathAfterRoot)) {
                                retorno = ReflectionHelper.invokeMethod(method, instanceObj);
                                break;
                            }
                        }
                    }
                }
                response.setContentType(CONTENT_TYPE);
                response.setCharacterEncoding(UTF_8);
                String json = gson.toJson(retorno);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(json);
            } else {
                response.setContentType(CONTENT_TYPE);
                response.setCharacterEncoding(UTF_8);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (ErrorException e) {
            response.setContentType(CONTENT_TYPE);
            response.setCharacterEncoding(UTF_8);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            //throw new ErrorException(e.getMessage(), e);
            //response.getWriter().write(e.getCause().getMessage());
        }
    }



    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String contextPath =  request.getPathInfo();
        String beginPath = BARRA + contextPath.split("/")[1];
        String pathAfterRoot = contextPath.replaceFirst(beginPath, "");

        BufferedReader reader = request.getReader();
        var requestBody = reader.lines().collect(Collectors.joining(System.lineSeparator()));

        Optional<Class<?>> clazzToInstance = ReflectionHelper.getController(beginPath, Context.mapinstances);
        try {
            if (clazzToInstance.isPresent()) {
                var instanceObj = Context.mapinstances.get(clazzToInstance.get());
                Method[] declaredMethods = instanceObj.getClass().getDeclaredMethods();
                Optional<Object> retorno = Arrays.stream(declaredMethods)
                        .filter(method -> method.isAnnotationPresent(PostMapping.class) && method.getAnnotation(PostMapping.class).value().equalsIgnoreCase(pathAfterRoot))
                        .map(method -> ReflectionHelper.invokeMethod(method, instanceObj, getObjectParam(gson, requestBody,
                                getParameter(method, RequestBody.class)))).findFirst();
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.setContentType(CONTENT_TYPE);
                response.getWriter().write(gson.toJson(retorno.get()));
            }
        }catch (ErrorException e){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType(CONTENT_TYPE);
            response.getWriter().write(e.toString());
        }

    }

    private static Object getObjectParam(Gson gson, String requestBody, Parameter parameter){
        return  gson.fromJson(requestBody, parameter.getType());
    }

    private static Parameter getParameter(Method method, Class<? extends Annotation>  annotation ) {
        return Arrays.stream(method.getParameters())
                .filter(parameter -> parameter.isAnnotationPresent(annotation)).findFirst().orElse(null);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String contextPath =  request.getPathInfo();
        String beginPath = BARRA + contextPath.split("/")[1];
        String pathAfterRoot = contextPath.replaceFirst(beginPath, "");
        BufferedReader reader = request.getReader();
        var requestBody = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        Optional<Class<?>> clazzToInstance = ReflectionHelper.getController(beginPath, Context.mapinstances);
        try {
            if (clazzToInstance.isPresent()) {
                var instanceObj = Context.mapinstances.get(clazzToInstance.get());
                Method[] declaredMethods = instanceObj.getClass().getDeclaredMethods();
                Optional<Object> retorno = Arrays.stream(declaredMethods)
                        .filter(method -> method.isAnnotationPresent(PutMapping.class) &&
                                method.getAnnotation(PutMapping.class).value().equalsIgnoreCase(pathAfterRoot))
                        .map(method -> ReflectionHelper.invokeMethod(method, instanceObj, getObjectParam(gson, requestBody,
                                getParameter(method,RequestBody.class)))).findFirst();
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType(CONTENT_TYPE);
                response.getWriter().write(gson.toJson(retorno.get()));
            }
        }catch (ErrorException e){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType(CONTENT_TYPE);
            response.getWriter().write(gson.toJson(e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String contextPath =  request.getPathInfo();
        String beginPath = BARRA + contextPath.split("/")[1];
        String pathAfterRoot = contextPath.replaceFirst(beginPath, "");

        Optional<Class<?>> clazzToInstance = ReflectionHelper.getController(beginPath, Context.mapinstances);

        if(clazzToInstance.isPresent()){
            var instanceObj = Context.mapinstances.get(clazzToInstance.get());
            Method[] declaredMethods = instanceObj.getClass().getDeclaredMethods();
            List<Method> deleteMethod =  Arrays.stream(declaredMethods)
                    .filter(method -> method.isAnnotationPresent(DeleteMapping.class)).toList();

            List<Object> list = deleteMethod.stream()
                    .map(method -> ReflectionHelper.invokeMethod(method, instanceObj, getParameter(method, PathParameter.class))).toList();

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(CONTENT_TYPE);
           // response.getWriter().write(gson.toJson(retorno.get()));
        }
    }
}
