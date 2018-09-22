package eu.europa.ec.cc.zkapachebridge.worker;

import eu.europa.ec.cc.zkapachebridge.ZkApacheBridgeApplication;
import eu.europa.ec.cc.zkapachebridge.serviceregistry.Endpoint;
import eu.europa.ec.cc.zkapachebridge.serviceregistry.EndpointCollection;
import eu.europa.ec.cc.zkapachebridge.serviceregistry.ServiceRegistryImpl;
import eu.europa.ec.cc.zkapachebridge.test.CommonTestConfig;
import eu.europa.ec.cc.zkapachebridge.test.helpers.ServiceHolder;
import eu.europa.ec.cc.zkapachebridge.test.helpers.TestableRewriteMapWriter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceType;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.zookeeper.serviceregistry.ServiceInstanceRegistration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.springframework.util.SocketUtils;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest(classes = {CommonTestConfig.class, ZkApacheBridgeApplication.class})
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public class ZookeeperListenerTest {

    final long zookeeperDelayMS = 100;

    @Autowired
    CuratorFramework curator;

    @Autowired
    ServiceDiscovery serviceDiscovery;

    @Autowired
    TestableRewriteMapWriter testableRewriteMapWriter;

    @Autowired
    ServiceRegistryImpl serviceRegistry;

    @Autowired
    TestingServer testingServer;

    @Value("${zkapachebridge.healthcheck.interval}")
    private long healthcheckInterval;

    @PostConstruct
    public void init() throws Exception {
        curator.createContainers("/services/");
    }

    @Test
    public void addServiceInstance() throws Exception {
        String service = "testService";
        int port = SocketUtils.findAvailableTcpPort();
        ServiceInstanceRegistration testService = buildServiceInstance(service, port);

        serviceDiscovery.registerService(testService.getServiceInstance());
        waitForZookeeperEvents();

        EndpointCollection endpoints = serviceRegistry.getEndpointCollection(service);
        Assert.isTrue(endpoints.containsURI(buildURI(port)), "Service instance registered in Zookeeper but not known by listener");
    }

    @Test
    public void removeServiceInstance() throws Exception {

        String service = "removeService";

        int port = SocketUtils.findAvailableTcpPort();
        ServiceInstanceRegistration testService = buildServiceInstance(service, port);

        serviceDiscovery.registerService(testService.getServiceInstance());
        waitForZookeeperEvents();

        serviceDiscovery.unregisterService(testService.getServiceInstance());
        waitForZookeeperEvents();

        EndpointCollection endpoints = serviceRegistry.getEndpointCollection(service);
        Assert.notNull(endpoints, "Service is unkown to Listener");
        Assert.isTrue(!endpoints.containsURI(buildURI(port)), "Service instance removed from Zookeeper but still known by listener");
    }


    @Test
    public void healthAffectsEndpointActivity() throws Exception {

        String serviceName = "healthService";
        ServiceHolder serviceHolder1 = buildNewServiceHolder(serviceName);
        ServiceHolder serviceHolder2 = buildNewServiceHolder(serviceName);


        serviceDiscovery.registerService(serviceHolder1.getServiceInstance());
        serviceDiscovery.registerService(serviceHolder2.getServiceInstance());
        waitForZookeeperEvents();

        boolean isActive = isEndpointActive(serviceHolder1.getName(), serviceHolder1.getUri());
        Assert.isTrue(!isActive, "not yet activated endpoint is active!");

        serviceHolder1.createServer(true);
        waitForHealtcheck();

        System.out.println(serviceHolder1.getName());
        System.out.println(serviceHolder1.getUri());
        isActive = isEndpointActive(serviceHolder1.getName(), serviceHolder1.getUri());
        Assert.isTrue(isActive, "activated endpoint is not active!");
        Assert.isTrue(isEndpointInRewriteMap(serviceHolder1), "activated endpoint not in rewritemap");

        serviceHolder2.createServer(true);
        waitForHealtcheck();

        isActive = isEndpointActive(serviceHolder2.getName(), serviceHolder2.getUri());
        Assert.isTrue(isActive, "activated endpoint is not active!");
        Assert.isTrue(isEndpointInRewriteMap(serviceHolder2), "activated endpoint not in rewritemap");

        serviceHolder1.stopServer();
        waitForHealtcheck();

        isActive = isEndpointActive(serviceHolder1.getName(), serviceHolder1.getUri());
        Assert.isTrue(!isActive, "de-activated endpoint is still active!");
        Assert.isTrue(!isEndpointInRewriteMap(serviceHolder1), "de-activated endpoint still in rewritemap");

        serviceHolder2.stopServer();

    }

    private void waitForHealtcheck() throws InterruptedException {
        Thread.sleep(healthcheckInterval * 2);
    }

    private void waitForZookeeperEvents() throws InterruptedException {
        Thread.sleep(zookeeperDelayMS);
    }


    @Test
    @Ignore("This tests stops zookeeper for 100 seconds, and then verifies whether the application happily reconnects")
    public void zookeeperInterruptionDoesntCrashSystem() throws Exception {
        String serviceName = "interruptionService";
        ServiceHolder serviceHolder1 = buildNewServiceHolder(serviceName);


        serviceDiscovery.registerService(serviceHolder1.getServiceInstance());
        waitForZookeeperEvents();

        testingServer.stop();
        Thread.sleep(100000);

        testingServer.restart();
        ServiceHolder serviceHolder2 = buildNewServiceHolder(serviceName);
        serviceDiscovery.registerService(serviceHolder2.getServiceInstance());
        waitForZookeeperEvents();


        EndpointCollection endpoints = serviceRegistry.getEndpointCollection(serviceName);
        Assert.notNull(endpoints, "Service is unkown to Listener after zookeeper interruption");
        Assert.isTrue(endpoints.containsURI(serviceHolder1.getUri()), "Service instance created before interruption not known by listener after zookeeper interruption");
        Assert.isTrue(endpoints.containsURI(serviceHolder2.getUri()), "Service instance created after interruption not known by listener after zookeeper interruption");
    }

    private boolean isEndpointInRewriteMap(ServiceHolder serviceHolder) {
        Pattern pattern = Pattern.compile(serviceHolder.getName() + ".*(\\s|\\|)\\Q" + serviceHolder.getUri() + "\\E(\\||$)");
        Matcher matcher = pattern.matcher(testableRewriteMapWriter.contents);
        return matcher.find();
    }

    private ServiceHolder buildNewServiceHolder(String serviceName) throws Exception {
        int port = SocketUtils.findAvailableTcpPort();
        ServiceInstanceRegistration testService = buildServiceInstance(serviceName, port);
        System.out.println("registered new instance: " + serviceName + ":" + port + " " + testService.getServiceInstance().getId());
        URI uri = buildURI(port);
        return new ServiceHolder(serviceName, port, testService.getServiceInstance(), uri, false);
    }

    private boolean isEndpointActive(String service, URI uri) {
        EndpointCollection endpoints = serviceRegistry.getEndpointCollection(service);
        Endpoint endpoint = endpoints.findByURI(uri);
        return (endpoint != null) && endpoint.isActive();
    }


    private URI buildURI(int port) {
        return URI.create("http://localhost:" + port);
    }

    private ServiceInstanceRegistration buildServiceInstance(String service, int port) {

        return ServiceInstanceRegistration.builder()
                .name(service)
                .address("localhost")
                .port(port)
                .serviceType(ServiceType.DYNAMIC)
                .defaultUriSpec()
                .build();
    }


}