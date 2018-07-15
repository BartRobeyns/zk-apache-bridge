package eu.europa.ec.cc.zkapachebridge.serviceregistry;

import org.springframework.context.ApplicationEvent;

public class ServiceRegistryUpdatedEvent extends ApplicationEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public ServiceRegistryUpdatedEvent(Object source) {
        super(source);
    }
}
