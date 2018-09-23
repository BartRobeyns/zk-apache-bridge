package eu.europa.ec.cc.zkapachebridge.rest;

import eu.europa.ec.cc.zkapachebridge.ZkApacheBridgeApplication;
import eu.europa.ec.cc.zkapachebridge.test.CommonTestConfig;
import org.hamcrest.core.StringContains;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@SpringBootTest(
        classes = {CommonTestConfig.class, ZkApacheBridgeApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public class InfoControllerTest {


    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    public void getServiceInformation() {
        assertTrue(
                "service info didn't return zk-apache-bridge itself as endpoint",
                this.restTemplate.getForObject("/api/info", Map.class).containsKey("zk-apache-bridge"));
    }

    @Test
    public void getLoadBalancerConfiguration() {
        assertThat(
                this.restTemplate.getForObject("/api/info/loadbalancer", String.class),
                new StringContains("ProxyPass /zk-apache-bridge"));
    }

    @Test
    public void getRewriteMapConfiguration() {
        assertThat(
                this.restTemplate.getForObject("/api/info/rewritemap", String.class),
                new StringContains("zk-apache-bridge"));
    }
}