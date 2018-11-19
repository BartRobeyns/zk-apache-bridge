# zk-apache-bridge
Apache load-balancing based on Zookeeper service-discovery 

(Caveat: works with spring-cloud defaults for service-registration and health-check)

Provides the endpoints, registered in Zookeeper, to Apache. Either as a rewritemap-file, for RewriteRule-based loadbalancing, or as a true LoadBalancer-configuration.

The discovered endpoints are health-checked based on Spring-Boot's /actuator/health endpoint

The service-names, as they are registered in Zookeeper, are mapped to the /services/<service-name> on the Apache-root.
Only characters [-_a-zA-Z0-9] are allowed in these service-names; all others are replaced with '-'.

## Build it
``` 
git clone https://github.com/BartRobeyns/zk-apache-bridge.git
cd zk-apache-bridge
mvn install
```

## Test it in Docker
```
cd zkclientsample
docker-compose up -d
# repeat the following, and see the same ip-address in the reponse
curl http://localhost/zk-sample-client/zkclient

docker-compose up -d --scale zkclientsample=3
# give it some time, then repeat the following to see different  ip-addresses in the response
curl http://localhost/zk-client-sample/zkclient

# The RewriteRule-based loadbalancing listens on port 80, to see the LoadBalancer-configuration in action use port 8080 instead:
curl http://localhost:8080/services/zk-client-sample/zkclient
```

## Run it with an existing Apache
```
java -jar zk-apache-bridge-1.0.0-SNAPSHOT-exec.jar
```
parameters:

    --spring.cloud.zookeeper.connect-string=<zookeeper-url> (defaults to localhost:2181)
    --spring.cloud.zookeeper.discovery.register=<true|false> (default: true, makes zk-apache-bridge itself discoverable)
    
    --zkapachebridge.healthcheck.interval=(default:20000, time in ms between healthchecks)
    --zkapachebridge.healthcheck.timeout.connect=(default:5000)
    --zkapachebridge.healthcheck.timeout.socket=(default:5000)
    --zkapachebridge.healthcheck.timeout.connectionRequest=(default:5000)
    --zkapachebridge.healthcheck.enabled=(default:true)
    
    --zkapachebridge.urls.prefix=<prefix for the exposed endpoints> (defaults to 'services/')
    
    --zkapachebridge.rewritemap.active=<true|false> (default: true)
    --zkapachebridge.rewritemap.path=<path where to write the rewritemap> (default: /etc/apache2/maps/apimap.map)
    
    --zkapachebridge.loadbalancer.active=<true|false> (default: false)
    --zkapachebridge.loadbalancer.path=<path where to write the LoadBalancer configuration> (default: /etc/apache2/maps/lbconfig.conf)
    --zkapachebridge.loadbalancer.apache-reload=<command to restart apache after the LoadBalancer configuration has been updated) (default: apachectl graceful)
    --zkapachebridge.loadbalancer.template=(default: /loadbalancer-config.ftl, location of the freemarker-template to construct the loadbalancer configuration)

## Monitor it
```
# default port is 8080
# list services, endpoints and active-status
GET http://<host>:<port>/api/info
# show the resultant loadbalancer configuration
GET http://<host>:<port>/api/info/loadbalancer
# show the resultant rewritemap
GET http://<host>:<port>/api/info/rewritemap
```

### RewriteRule-based loadbalancing
[See the RewriteMap Docker-image](./zk-apache-bridge-docker/zk-apache-bridge-docker-rewritemap)

Apache allows you include key-value pairs useable in RewriteRules with RewriteMap.
A value can be a list (delimited with |), from where an entry is picked at random.

E.g. with map/apimap.txt
```
service1 http://192.168.0.1:80|http://192.168.0.2:80
```
you can get either the ...0.1 or the ....0.2 address in a RewriteRule like this:
```
    RewriteMap apimap rnd:maps/apimap.txt 
    RewriteRule /service1/(.*) ${apimap:service1}/$1 [LP]
```

These RewriteMaps are reloaded dynamically, whenever they change.

This is the basis of the RewriteMap-based implementation, which is fully elaborated in zk-apache-bridge-docker/zk-apache-bridge-docker-rewritemap;
take a look at the 000-default-conf file in there.

### LoadBalancer-based loadbalancing
[See the LoadBalancer Docker-image](./zk-apache-bridge-docker/zk-apache-bridge-docker-loadbalancer)

RewriteMaps are quite limited for LoadBalancing: there's no other algorithm than the randomness of the map.
 
Apache httpd offers a better solution through the proxy_balancer module. The only downside: it's not dynamically reloadable (at least not based on dynamic service discovery). But Apache httpd itself allows you to restart it 'gracefully': this means it will load changed configuration in a new set of worker-processes, and the old worker-processes are allowed to handle their current requests and only then are killed.

When zk-apache-bridge receives a change in service-endpoints, it changes the Apache configuration, and then asks it to restart gracefully (using 'apachectl graceful').
The configuration it writes looks like this, for every service and every active endpoint:

```
<Proxy "balancer://service1">
    BalancerMember "http://192.168.0.1:80"
    BalancerMember "http://192.168.0.2:80"
</Proxy>
ProxyPass        "/service1" "balancer://service1"
```

It's written to the path defined through `--zkapachebridge.loadbalancer.path`, which should be included in the Apache configuration
with, e.g., `Include maps/lbconfig.conf`
