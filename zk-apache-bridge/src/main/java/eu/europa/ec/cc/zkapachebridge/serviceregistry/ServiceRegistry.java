package eu.europa.ec.cc.zkapachebridge.serviceregistry;

import java.net.URI;
import java.util.Map;

public interface ServiceRegistry {
    void addServiceURI(String service, URI uri, boolean healthCheckEnabled, String healthCheckEndpoint);

    void removeServiceURI(String service, URI uri);

    Map<String, EndpointCollection> getServices();
}
