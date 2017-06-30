package app.auth;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * Created by mtoepperwien on 19.06.17.
 */
public class AuthServer {
    private static HttpServer server;
    private String responseQuery;
    public void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8888), 0);
        server.createContext("/callback", new MyHandler());
        server.start();
    }

    public static void stopServer() {
        server.stop(0);
    }

    public String getCode() {
        if (responseQuery != null) {
            String[] response = responseQuery.split("=");
            return response[1];
        } else {
            return null;
        }
    }

    class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            responseQuery = t.getRequestURI().getQuery();
            String response = "Finished\n" + responseQuery;
            t.sendResponseHeaders(200, response.length());
            OutputStream outstream = t.getResponseBody();
            outstream.write(response.getBytes());
            outstream.close();
        }
    }
}

