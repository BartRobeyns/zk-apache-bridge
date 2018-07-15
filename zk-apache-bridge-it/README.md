# Why are these tests in a separate module?

zk-apache-bridge downgrades zookeeper to 3.4, to be compatible with the version used by Kafka.

However, integration tests with embedded Zookeeper must run with Zookeeper 3.5, because curator-test (who offers the embedded 'TestingServer') runs into incompatibility issues with Zookeeper 3.4:

```
java.lang.NoSuchFieldError: configFileStr
    at org.apache.curator.test.QuorumConfigBuilder$1.<init>(QuorumConfigBuilder.java:135)
    at org.apache.curator.test.QuorumConfigBuilder.buildConfig(QuorumConfigBuilder.java:130)
    at org.apache.curator.test.TestingZooKeeperServer$1.run(TestingZooKeeperServer.java:149)
```
curator-test-zk34 provides compatibility with 3.4, but doesn't provide the TestingServer.

Providing two different versions of zookeeper with different scopes (test,compile) doesn't work anymore since Maven 3