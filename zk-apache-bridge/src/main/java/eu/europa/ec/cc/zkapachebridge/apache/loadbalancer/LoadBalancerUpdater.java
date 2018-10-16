package eu.europa.ec.cc.zkapachebridge.apache.loadbalancer;

import eu.europa.ec.cc.zkapachebridge.serviceregistry.EndpointCollection;
import eu.europa.ec.cc.zkapachebridge.serviceregistry.ServiceRegistry;
import eu.europa.ec.cc.zkapachebridge.serviceregistry.ServiceRegistryUpdatedEvent;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Component
public class LoadBalancerUpdater implements ApplicationListener<ServiceRegistryUpdatedEvent> {

    final private ServiceRegistry serviceRegistry;

    final private LoadBalancerWriter loadBalancerWriter;

    final private Configuration freemarkerConfiguration;

    @Value("${zkapachebridge.loadbalancer.active}")
    boolean active;

    @Value("${zkapachebridge.loadbalancer.template}")
    String loadBalancerConfigTemplate;

    @Value("${zkapachebridge.loadbalancer.apache-reload}")
    String apacheReload;

    @Value("${zkapachebridge.urls.prefix:}")
    String urlPrefix;

    @Autowired
    public LoadBalancerUpdater(Configuration freemarkerConfiguration, LoadBalancerWriter loadBalancerWriter, ServiceRegistry serviceRegistry) {
        this.freemarkerConfiguration = freemarkerConfiguration;
        this.freemarkerConfiguration.setClassForTemplateLoading(this.getClass(),"/");
        this.loadBalancerWriter = loadBalancerWriter;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void onApplicationEvent(ServiceRegistryUpdatedEvent event) {
        if ( !active ) {
            return;
        }

        try {
            String contents = buildLoadBalancerContents();
            loadBalancerWriter.write(contents);
            reloadApache();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getConfiguration() throws Exception {
        return buildLoadBalancerContents();
    }

    String buildLoadBalancerContents() throws Exception {
        Map<String, Object> model = buildServicesFreemarkerModel();
        Template template = freemarkerConfiguration.getTemplate(loadBalancerConfigTemplate);
        return applyTemplate(template, model);
    }

    private String applyTemplate(Template template, Map<String, Object> model) throws TemplateException, IOException {
        StringWriter sw = new StringWriter();
        template.process(model, sw);
        return sw.toString();
    }

    private Map<String, Object> buildServicesFreemarkerModel() {
        Map<String, EndpointCollection> sanitizedServices = sanitizeServiceNames(serviceRegistry.getServices());
        Map<String, Object> model = new HashMap<>();
        model.put("services", sanitizedServices);
        model.put("prefix", urlPrefix);
        return model;
    }

    private Map<String, EndpointCollection> sanitizeServiceNames(Map<String,EndpointCollection> services) {
        Map<String, EndpointCollection> result = new HashMap<>();
        for (Map.Entry<String,EndpointCollection> serviceEntry: services.entrySet() ) {
            String key = sanitizeServiceName(serviceEntry.getKey());
            result.put(key, serviceEntry.getValue());
        }
        return result;
    }

    public String sanitizeServiceName(String key) {
        return key.replaceAll("([^-_A-Za-z0-9])", "-" );
    }

    void reloadApache() throws IOException {
        ExecuteResultHandler handler = new DefaultExecuteResultHandler();
        CommandLine command = CommandLine.parse(apacheReload);
        DefaultExecutor executor = new DefaultExecutor();
        executor.execute(command, handler);
    }

}
