package eu.europa.ec.cc.zkapachebridge.serviceregistry;

import lombok.Data;

import java.net.URI;

@Data
public class Endpoint {
    private final URI uri;
    private boolean active;

    public Endpoint(URI uri, boolean active) {
        this.uri = uri;
        this.active = active;
    }

}
