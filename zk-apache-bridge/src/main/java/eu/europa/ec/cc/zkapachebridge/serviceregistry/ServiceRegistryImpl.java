package eu.europa.ec.cc.zkapachebridge.serviceregistry;

import com.google.common.collect.ImmutableMap;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Hashtable;
import java.util.Map;

@Component
@ToString(exclude = {"eventPublisher"})
public class ServiceRegistryImpl implements ServiceRegistry {

    final private ApplicationEventPublisher eventPublisher;

    final private Hashtable<String, EndpointCollection> services = new Hashtable<>();

    @Autowired
    public ServiceRegistryImpl(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void addServiceURI(String service, String instance, URI uri, boolean healthCheckEnabled, String healthCheckEndpoint) {
        EndpointCollection endpoints = ensureEndpointListExists(service);
        if (!endpoints.containsInstance(instance)) {
            endpoints.addURI(uri, instance, healthCheckEnabled, healthCheckEndpoint);
            eventPublisher.publishEvent(new ServiceRegistryUpdatedEvent(this));
        }
    }


    @Override
    public void removeServiceURI(String service, String instance) {
        EndpointCollection endpoints = ensureEndpointListExists(service);
        endpoints.removeInstance(instance);
        eventPublisher.publishEvent(new ServiceRegistryUpdatedEvent(this));
    }

    @Override
    public Map<String, EndpointCollection> getServices() {
        return ImmutableMap.copyOf(services);
    }


    private EndpointCollection ensureEndpointListExists(String service) {
        EndpointCollection endpoints = getEndpointCollection(service);
        if (endpoints == null) {
            endpoints = new EndpointCollection();
            services.put(service, endpoints);
        }
        return endpoints;
    }

    public EndpointCollection getEndpointCollection(String service) {
        return services.get(service);
    }

}
