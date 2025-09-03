package com.autumn.helper;

import com.autumn.exceptions.ErrorException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ScanPackageImpl {

    public static final Logger logger = LogManager.getLogger(ScanPackageImpl.class);

    public static List<Class<?>> findAllClass(String basePackage) throws IOException  {
        List<Class<?>> classes = new ArrayList<>();

        String path = basePackage.replace(".","/");
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = contextClassLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        if(resources.hasMoreElements()){
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        dirs.forEach(dir -> {
            try {
                classes.addAll(findClasses(dir, basePackage));
            } catch (ClassNotFoundException e) {
                throw new ErrorException("Falha ao obter contexto",e);
            }
        });
        return classes;

    }

    public static List<Class<?>> findClasses(File diretory, String basePackage) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if(!diretory.exists()) return classes;

        File[] files = diretory.listFiles();
        if(null == files)  return classes;

        for (File file : files){
            if(file.isDirectory()){
                classes.addAll(findClasses(file, basePackage + "."+ file.getName()));
            } else if(file.getName().endsWith(".class")){
               String className = basePackage + "." +file.getName().replace(".class", "");
               //logger.info(className);
               classes.add(Class.forName(className));
            }
        }

        return classes;
    }
}
