package com.autumn;

import com.autumn.helper.ApplicationPropertiesLoader;
import com.autumn.helper.Context;
import com.autumn.servlet.AutumnServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;


/**
 * Hello world!
 */
public class AutumnApplication {

    private static final Logger logger = LogManager.getLogger(AutumnApplication.class);

    public static void run(Class<?> clazz) throws Exception {
        Context.build(clazz.getPackageName());

        Server server = new Server(ApplicationPropertiesLoader.serverPort());
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // Registrar o servlet manualmente
        context.addServlet(AutumnServlet.class.getName(), "/*");

        server.setHandler(context);

        logger.info("Servidor iniciado em http://localhost: {}", ApplicationPropertiesLoader.serverPort());

        server.start();
        server.join();
    }

    public static void main(String[] args) throws Exception {
       // AutumnApplication.run(AutumnApplication.class);
    }
}
