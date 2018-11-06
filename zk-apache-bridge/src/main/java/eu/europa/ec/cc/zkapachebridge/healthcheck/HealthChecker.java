package eu.europa.ec.cc.zkapachebridge.healthcheck;

import eu.europa.ec.cc.zkapachebridge.serviceregistry.Endpoint;
import eu.europa.ec.cc.zkapachebridge.serviceregistry.EndpointCollection;
import eu.europa.ec.cc.zkapachebridge.serviceregistry.ServiceRegistry;
import eu.europa.ec.cc.zkapachebridge.serviceregistry.ServiceRegistryUpdatedEvent;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

@Component
public class HealthChecker {
    private final ServiceRegistry serviceRegistry;

    private final HttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();

    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public HealthChecker(ServiceRegistry serviceRegistry, ApplicationEventPublisher eventPublisher) {
        this.serviceRegistry = serviceRegistry;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedDelayString = "${zkapachebridge.healthcheck.interval}")
    public void checkEndpointsHealth() {
        Map<String, EndpointCollection> services = serviceRegistry.getServices();
        services.forEach((servicename, endpoints) -> {
            List<Endpoint> list = endpoints.getEndpoints();
            list.forEach(endpoint -> {
                URI uri = endpoint.getUri();
                String healthURL = uri.toString() + "/actuator/health";
                RequestConfig requestConfig = RequestConfig.custom()
                        .setSocketTimeout(2000)
                        .setConnectTimeout(2000)
                        .setConnectionRequestTimeout(2000).build();
                try (CloseableHttpClient client
                        = HttpClients.custom().setConnectionManager(poolingConnManager)
                        .setConnectionManagerShared(true)
                        .setDefaultRequestConfig(requestConfig)
                        .build() ) {

                    boolean isActive = false;

                    try ( CloseableHttpResponse response = client.execute(new HttpGet(healthURL)) ) {
                        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                            String content = EntityUtils.toString(response.getEntity());
                            try {
                                Map<String, Object> responseMap = new GsonJsonParser().parseMap(content);
                                if ("UP".equals(responseMap.get("status"))) {
                                    isActive = true;
                                }
                            } catch( Exception e ) {
                                System.out.println("Failed to parse content " + content);
                            }
                        }
                    } catch (HttpHostConnectException e) {
                        // host not available, mark as inactive
                    } catch (IOException e) {
                        System.out.println("Failed to contact " + healthURL);
                        e.printStackTrace();
                    }

                    if (endpoint.isActive() != isActive) {
                        endpoint.setActive(isActive);
                        System.out.println("endpont changed: " + endpoint);
                        eventPublisher.publishEvent(new ServiceRegistryUpdatedEvent(this));
                    }
                } catch (IOException e) {
                    System.out.println("Failed to contact " + healthURL);
                    e.printStackTrace();
                }

            });

        });

    }
}
