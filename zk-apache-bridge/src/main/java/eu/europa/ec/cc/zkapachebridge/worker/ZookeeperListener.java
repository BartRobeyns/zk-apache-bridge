package eu.europa.ec.cc.zkapachebridge.worker;

import eu.europa.ec.cc.zkapachebridge.serviceregistry.ServiceRegistryImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.Map;

@Component
public class ZookeeperListener implements TreeCacheListener {
    private final CuratorFramework curator;

    private final ServiceDiscovery serviceDiscovery;

    private final ServiceRegistryImpl serviceRegistry;

    @Autowired
    public ZookeeperListener(CuratorFramework curator, ServiceDiscovery serviceDiscovery, ServiceRegistryImpl serviceRegistry) {
        this.curator = curator;
        this.serviceDiscovery = serviceDiscovery;
        this.serviceRegistry = serviceRegistry;
    }


    @PostConstruct
    public void listen() throws Exception {
        TreeCache treeCache = TreeCache.newBuilder(curator, "/services").setCacheData(true).setMaxDepth(4).build();
        treeCache.getListenable().addListener(this);
        treeCache.start();
    }

    @Override
    public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
        System.out.println(event.getType());
        ChildData data = event.getData();
        if (null == data) {
            return;
        }
        ServicePath servicePath = getServicePath(data.getPath());
        System.out.println(servicePath);
        if (servicePath.instance == null) {
            return;
        }
        ServiceInstance<ZookeeperInstance> serviceInstance = getServiceInstance(data);
        switch (event.getType()) {
            case NODE_ADDED:
                onNodeAdded(servicePath, serviceInstance);
                break;
            case NODE_REMOVED:
                onNodeRemoved(servicePath, serviceInstance);
                break;
            default:
        }
    }

    private ServiceInstance<ZookeeperInstance> getServiceInstance(ChildData data) throws Exception {
        JsonInstanceSerializer serializer = new JsonInstanceSerializer(ZookeeperInstance.class);
        return (ServiceInstance<ZookeeperInstance>) serializer.deserialize(data.getData());
    }

    private void onNodeRemoved(ServicePath servicePath, ServiceInstance<ZookeeperInstance> serviceInstance) {
        System.out.printf("Removed %s %s\n", servicePath.name, servicePath.instance);
        String uriSpec = serviceInstance.buildUriSpec();
        serviceRegistry.removeServiceURI(servicePath.name, URI.create(uriSpec));
    }

    private void onNodeAdded(ServicePath servicePath, ServiceInstance<ZookeeperInstance> serviceInstance) {
        System.out.printf("Added %s %s\n", servicePath.name, servicePath.instance);
        String uriSpec = serviceInstance.buildUriSpec();
        boolean healthCheckEnabled = isHealthCheckEnabled(serviceInstance);
        String healthCheckEndpoint = uriSpec + buildHealthCheckEndpoint(serviceInstance);
        serviceRegistry.addServiceURI(servicePath.name, URI.create(uriSpec), healthCheckEnabled, healthCheckEndpoint);
    }

    private ServicePath getServicePath(String path) {

        // e.g. /services/zk-client-sample/6e11e56e-9f62-450c-a1e7-afa134c4547e
        ServicePath instance = new ServicePath();

        String[] split = path.split("/"); // split[0] is always "", split[1] contains "services"
        if (split.length >= 3) {
            instance.name = split[2];
        }
        if (split.length >= 4) {
            instance.instance = split[3];
        }
        return instance;
    }

    private String buildHealthCheckEndpoint(ServiceInstance<ZookeeperInstance> serviceInstance) {
        return safeGetMetadata(serviceInstance, "healthcheck.endpoint", "/actuator/health");
    }

    private boolean isHealthCheckEnabled(ServiceInstance<ZookeeperInstance> serviceInstance) {
        return "true".equals(safeGetMetadata(serviceInstance,"healthcheck.enabled", "true"));
    }

    private String safeGetMetadata(ServiceInstance<ZookeeperInstance> serviceInstance, String endpointKey, String defaultValue) {
        if ( serviceInstance == null || serviceInstance.getPayload() == null || serviceInstance.getPayload().getMetadata() == null ) {
            return defaultValue;
        }
        String value = serviceInstance.getPayload().getMetadata().get(endpointKey);
        if ( StringUtils.isEmpty(value) ) {
            return defaultValue;
        }
        return value;
    }

}
