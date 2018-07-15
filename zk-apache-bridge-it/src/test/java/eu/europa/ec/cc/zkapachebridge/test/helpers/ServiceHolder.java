package eu.europa.ec.cc.zkapachebridge.test.helpers;

import lombok.Data;
import org.apache.curator.x.discovery.ServiceInstance;
import org.eclipse.jetty.server.Server;

import java.net.URI;

@Data
public class ServiceHolder {
    private final String name;
    private final int port;
    private final ServiceInstance serviceInstance;
    private final URI uri;
    private Server server;

    public ServiceHolder(String name, int port, ServiceInstance serviceInstance, URI uri, boolean withJettyServer) throws Exception {

        this.name = name;
        this.port = port;
        this.serviceInstance = serviceInstance;
        this.uri = uri;
        if (withJettyServer) {
            createServer();
        }
    }

    public Server createServer() throws Exception {
        return createServer(true);
    }

    public Server createServer(boolean start) throws Exception {
        this.server = TestJettyServer.build(this.port, true);
        return this.server;
    }

    public void stopServer() throws Exception {
        if (this.server != null) {
            this.server.stop();
        }
    }
}
