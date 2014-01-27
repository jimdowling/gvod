/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.system.util;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 *
 * @author jdowling
 */
public class JwHttpServer {

    private static InetSocketAddress addr;
    private static JwHttpServer instance = null;
    private static HttpServer server = null;

    private JwHttpServer() {
    }

    public static synchronized void startOrUpdate(InetSocketAddress addr, String filename,
            HttpHandler handler)
            throws IOException {
        if (addr == null) {
            throw new IllegalArgumentException("InetSocketAddress was null for HttpServer");
        }
        if (filename == null) {
            throw new IllegalArgumentException("filename was null for HttpServer");
        }
        boolean toStart = false;
        if (instance == null) {
            instance = new JwHttpServer();
            JwHttpServer.addr = addr;

            server = HttpServer.create(addr,
                    /*system default backlog for TCP connections*/ 0);
//            server.setExecutor(Executors.newCachedThreadPool());
            toStart = true;
        }
        HttpContext context = server.createContext(filename, handler);
        context.getFilters().add(new ParameterFilter());
        if (toStart) {
            server.start();
        }
    }

    public static synchronized void addContext(String httpPath, HttpHandler handler) {
        if (server == null) {
            throw new IllegalStateException("JwHttpServer should be initialized before a context is added.");
        }
        HttpContext context = server.createContext(httpPath, handler);
        context.getFilters().add(new ParameterFilter());
    }

    public static synchronized void removeContext(String filename) {
        if (server == null) {
            throw new IllegalStateException("You must create the HttpServer before removing a context");
        }
        server.removeContext(filename);
    }
}
