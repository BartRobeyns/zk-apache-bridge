package eu.europa.ec.cc.zkapachebridge.serviceregistry;

import com.google.common.collect.ImmutableList;
import lombok.ToString;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@ToString
public class EndpointCollection {

    private List<Endpoint> endpoints = new ArrayList<>();

    public void addURI(URI uri, String instance, boolean healthCheckEnabled, String healthCheckEndpoint) {
        endpoints.add(new Endpoint(uri, instance, false, healthCheckEnabled, healthCheckEndpoint));
    }

    public List<Endpoint> getEndpoints() {
        return ImmutableList.copyOf(endpoints);
    }

    public boolean containsURI(URI uri) {
        Endpoint endpoint = findByURI(uri);
        return endpoint != null;
    }

    public boolean containsInstance(String instance) {
        Endpoint endpoint = findByInstance(instance);
        return endpoint != null;
    }
    public Endpoint findByURI(URI uri) {
        for (Endpoint endpoint : endpoints) {
            if (endpoint.getUri().equals(uri)) {
                return endpoint;
            }
        }
        return null;
    }

    public Endpoint findByInstance(String instance) {
        for (Endpoint endpoint : endpoints) {
            if (endpoint.getInstance().equals(instance)) {
                return endpoint;
            }
        }
        return null;
    }

    public void removeInstance(String instance) {
        Endpoint endpoint = findByInstance(instance);
        if (endpoint != null) {
            endpoints.remove(endpoint);
        }
    }

    public List<String> getActiveURIStrings() {
        List<String> list = new ArrayList<>();
        endpoints.forEach(endpoint -> {
            if (endpoint.isActive()) {
                list.add(endpoint.getUri().toString());
            }
        });
        return list;
    }

}
