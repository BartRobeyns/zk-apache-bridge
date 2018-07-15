package eu.europa.ec.cc.zkapachebridge.test.helpers;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

public class TestJettyServer {
    public static Server build(int port, boolean start) throws Exception {
        Server server = new Server(port);
        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(TestHealthServlet.class, "/actuator/health");
        server.setHandler(handler);
        if (start) {
            server.start();
        }
        return server;
    }
}
