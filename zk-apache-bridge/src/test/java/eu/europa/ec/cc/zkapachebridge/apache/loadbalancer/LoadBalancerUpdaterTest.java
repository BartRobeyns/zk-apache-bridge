package eu.europa.ec.cc.zkapachebridge.apache.loadbalancer;

import eu.europa.ec.cc.zkapachebridge.serviceregistry.Endpoint;
import eu.europa.ec.cc.zkapachebridge.serviceregistry.ServiceRegistry;
import eu.europa.ec.cc.zkapachebridge.serviceregistry.ServiceRegistryImpl;
import freemarker.template.Configuration;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LoadBalancerUpdaterTest {
    ApplicationEventPublisher publisher = new ApplicationEventPublisher() {
        public void publishEvent(Object event) {}
    };

    @Test
    public void buildLoadBalancerContentsTest() throws Exception {

        LoadBalancerUpdater loadBalancerUpdater = buildLoadBalancerUpdater();
        String content = loadBalancerUpdater.buildLoadBalancerContents();

        final Pattern balancerOne = Pattern.compile("\\Q<Proxy \"balancer://one\">\\E(.*?)</Proxy>", Pattern.DOTALL);
        final Pattern balancerTwo = Pattern.compile("\\Q<Proxy \"balancer://two\">\\E(.*?)</Proxy>", Pattern.DOTALL);
        final Pattern balancerOneMemberOne = Pattern.compile("BalancerMember\\s+\"http://one:1");
        final Pattern balancerOneMemberTwo = Pattern.compile("BalancerMember\\s+\"http://one:2");
        final Pattern balancerTwoMemberOne = Pattern.compile("BalancerMember\\s+\"http://two:1");
        final Pattern balancerTwoMemberTwo = Pattern.compile("BalancerMember\\s+\"http://two:2");

        Matcher matcher = balancerOne.matcher(content);
        Assert.isTrue(matcher.find(),"Balancer One not found");
        String members = matcher.group(1);
        Assert.isTrue(balancerOneMemberOne.matcher(members).find(), "Member one:1 not found");
        Assert.isTrue(balancerOneMemberTwo.matcher(members).find(), "Member one:2 not found");

        matcher = balancerTwo.matcher(content);
        Assert.isTrue(matcher.find(),"Balancer Two not found");
        members = matcher.group(1);
        Assert.isTrue(balancerTwoMemberOne.matcher(members).find(), "Member two:1 not found");
        Assert.isTrue(balancerTwoMemberTwo.matcher(members).find(), "Member two:2 not found");

    }

    private LoadBalancerUpdater buildLoadBalancerUpdater() {
        ServiceRegistry serviceRegistry = new ServiceRegistryImpl(publisher);
        LoadBalancerWriter rewriteMapWriter = null;
        Configuration freemarkerConfiguration = new Configuration( Configuration.VERSION_2_3_28 );
        freemarkerConfiguration.setClassLoaderForTemplateLoading(LoadBalancerUpdater.class.getClassLoader(), "/");
        freemarkerConfiguration.setWhitespaceStripping(true);

        setupServices(serviceRegistry);

        LoadBalancerUpdater loadBalancerUpdater = new LoadBalancerUpdater( freemarkerConfiguration,
                rewriteMapWriter, serviceRegistry );
        loadBalancerUpdater.loadBalancerConfigTemplate = "loadbalancer-config.ftl";
        loadBalancerUpdater.urlPrefix="services/";
        return loadBalancerUpdater;
    }


    @Test
    public void testServiceNameSanitize() {

        LoadBalancerUpdater loadBalancerUpdater = buildLoadBalancerUpdater();
        String r = loadBalancerUpdater.sanitizeServiceName("abc");
        Assert.isTrue("abc".equals(r));

        r = loadBalancerUpdater.sanitizeServiceName("ab c");
        System.out.println(r);
        Assert.isTrue("ab-c".equals(r));

        r = loadBalancerUpdater.sanitizeServiceName("ab  c");
        System.out.println(r);
        Assert.isTrue("ab--c".equals(r));

        r = loadBalancerUpdater.sanitizeServiceName("ab  c$%#");
        System.out.println(r);
        Assert.isTrue("ab--c---".equals(r));
    }

    private void setupServices(ServiceRegistry serviceRegistry) {
        String serviceOne = "one";
        String serviceTwo = "two";
        String serviceOneURLOne = "http://one:1";
        String serviceOneURLTwo = "http://one:2";
        String serviceTwoURLOne = "http://two:1";
        String serviceTwoURLTwo = "http://two:2";
        serviceRegistry.addServiceURI(serviceOne, URI.create(serviceOneURLOne));
        serviceRegistry.addServiceURI(serviceOne, URI.create(serviceOneURLTwo));
        serviceRegistry.addServiceURI(serviceTwo, URI.create(serviceTwoURLOne));
        serviceRegistry.addServiceURI(serviceTwo, URI.create(serviceTwoURLTwo));
        serviceRegistry.getServices().forEach((service,endpoints) -> {
            for (Endpoint endpoint : endpoints.getEndpoints()) {
                endpoint.setActive(true);
            }
        });
    }
}