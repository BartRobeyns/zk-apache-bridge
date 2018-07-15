package eu.europa.ec.cc.zkapachebridge.serviceregistry;

import com.google.common.collect.ImmutableList;
import lombok.ToString;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@ToString
public class EndpointCollection {

    private List<Endpoint> endpoints = new ArrayList<>();

    public void addURI(URI uri) {
        endpoints.add(new Endpoint(uri, false));
    }

    public List<Endpoint> getEndpoints() {
        return ImmutableList.copyOf(endpoints);
    }

    public boolean containsURI(URI uri) {
        Endpoint endpoint = findByURI(uri);
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

    public void removeURI(URI uri) {
        Endpoint endpoint = findByURI(uri);
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
