package eu.europa.ec.cc.zkapachebridge.serviceregistry;

import java.net.URI;
import java.util.Map;

public interface ServiceRegistry {
    void addServiceURI(String service, String instance, URI uri, boolean healthCheckEnabled, String healthCheckEndpoint);

    void removeServiceURI(String service, String instance);

    Map<String, EndpointCollection> getServices();
}
