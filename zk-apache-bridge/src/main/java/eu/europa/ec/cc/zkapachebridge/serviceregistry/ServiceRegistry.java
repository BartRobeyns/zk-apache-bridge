package eu.europa.ec.cc.zkapachebridge.serviceregistry;

import java.net.URI;
import java.util.Map;

public interface ServiceRegistry {
    void addServiceURI(String service, URI uri);

    void removeServiceURI(String service, URI uri);

    Map<String, EndpointCollection> getServices();
}
