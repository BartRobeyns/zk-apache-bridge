package eu.europa.ec.cc.zkapachebridge.serviceregistry;

import lombok.Data;

import java.net.URI;

@Data
public class Endpoint {
    private final URI uri;
    private boolean active;
    private final boolean healthCheckEnabled;
    private final String healthCheckEndpoint;

    public Endpoint(URI uri, boolean active, boolean healthCheckEnabled, String healthCheckEndpoint) {
        this.uri = uri;
        this.active = active;
        this.healthCheckEnabled = healthCheckEnabled;
        this.healthCheckEndpoint = healthCheckEndpoint;
    }

}
