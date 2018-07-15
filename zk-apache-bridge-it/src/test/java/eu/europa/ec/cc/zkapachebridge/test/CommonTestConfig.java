package eu.europa.ec.cc.zkapachebridge.test;

import eu.europa.ec.cc.zkapachebridge.apache.rewritemap.RewriteMapWriter;
import eu.europa.ec.cc.zkapachebridge.test.helpers.TestableRewriteMapWriter;
import org.apache.curator.test.TestingServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.zookeeper.ZookeeperProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.SocketUtils;

import java.util.Properties;

@TestConfiguration
public class CommonTestConfig {

    @Autowired
    TestableRewriteMapWriter testableRewriteMapWriter;
    @Bean
    RewriteMapWriter rewriteMapWriter() {
        return testableRewriteMapWriter;
    }

    @Bean(destroyMethod = "close") TestingServer testingServer(ConfigurableEnvironment env) throws Exception {

        int availableTcpPort = SocketUtils.findAvailableTcpPort();
        Properties props = new Properties();
        String key = "spring.cloud.zookeeper.connect-string";
        props.put(key, "localhost:" + availableTcpPort);
        env.getPropertySources().addFirst(new PropertiesPropertySource("testprops", props));
        TestingServer testingServer = new TestingServer(availableTcpPort);

        return testingServer;
    }

    @Bean ZookeeperProperties zookeeperProperties(TestingServer testingServer) throws Exception {

        ZookeeperProperties zookeeperProperties = new ZookeeperProperties();
        zookeeperProperties.setConnectString("localhost:" + testingServer.getPort());
        return zookeeperProperties;
    }
}
