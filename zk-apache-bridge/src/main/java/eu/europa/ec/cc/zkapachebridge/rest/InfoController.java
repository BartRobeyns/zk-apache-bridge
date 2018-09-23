package eu.europa.ec.cc.zkapachebridge.rest;

import eu.europa.ec.cc.zkapachebridge.apache.loadbalancer.LoadBalancerUpdater;
import eu.europa.ec.cc.zkapachebridge.apache.rewritemap.RewriteMapUpdater;
import eu.europa.ec.cc.zkapachebridge.serviceregistry.EndpointCollection;
import eu.europa.ec.cc.zkapachebridge.serviceregistry.ServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/info")
public class InfoController {

    @Autowired
    ServiceRegistry serviceRegistry;

    @Autowired
    LoadBalancerUpdater loadBalancerUpdater;

    @Autowired
    RewriteMapUpdater rewriteMapUpdater;

    @RequestMapping(method = RequestMethod.GET)
    public Map<String, EndpointCollection> getServiceInformation() {
        return serviceRegistry.getServices();
    }

    @RequestMapping(path="loadbalancer", method=RequestMethod.GET)
    public String getLoadBalancerConfiguration() throws Exception {
        return loadBalancerUpdater.getConfiguration();
    }

    @RequestMapping(path="rewritemap", method=RequestMethod.GET)
    public String getRewriteMapConfiguration() {
        return rewriteMapUpdater.getConfiguration();
    }
}
