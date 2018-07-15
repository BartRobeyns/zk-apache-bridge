package eu.europa.ec.cc.zkapachebridge.serviceregistry;

import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.net.URI;

import static org.junit.Assert.*;

public class ServiceRegistryTest {


    @Test
    public void addServiceURI() {
        String url = "http://somewhere:8080/";
        ServiceRegistryImpl serviceRegistry = new ServiceRegistryImpl(mockPublisher);
        serviceRegistry.addServiceURI("test", URI.create(url) );
        EndpointCollection endpoints = serviceRegistry.getEndpointCollection("test");
        Endpoint endpoint = endpoints.findByURI(URI.create(url));
        assertNotNull(endpoint);
    }

    @Test
    public void removeServiceURI() {
        String url = "http://somewhere:8080/";
        ServiceRegistryImpl serviceRegistry = new ServiceRegistryImpl(mockPublisher);
        serviceRegistry.addServiceURI("test", URI.create(url) );
        EndpointCollection endpoints = serviceRegistry.getEndpointCollection("test");
        Endpoint endpoint = endpoints.findByURI(URI.create(url));
        assertNotNull(endpoint);

        serviceRegistry.removeServiceURI("test", URI.create(url));
        endpoints = serviceRegistry.getEndpointCollection("test");
        endpoint = endpoints.findByURI(URI.create(url));
        assertNull(endpoint);
    }

    private ApplicationEventPublisher mockPublisher = new ApplicationEventPublisher() {
        public void publishEvent(Object event) {}
    };

}