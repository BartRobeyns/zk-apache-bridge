package eu.europa.ec.cc.zkapachebridge.apache.rewritemap;

import eu.europa.ec.cc.zkapachebridge.serviceregistry.EndpointCollection;
import eu.europa.ec.cc.zkapachebridge.serviceregistry.ServiceRegistry;
import eu.europa.ec.cc.zkapachebridge.serviceregistry.ServiceRegistryUpdatedEvent;
import freemarker.template.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RewriteMapUpdater implements ApplicationListener<ServiceRegistryUpdatedEvent> {

    final ServiceRegistry serviceRegistry;

    final RewriteMapWriter rewriteMapWriter;

    final Configuration freemarkerConfiguration;

    @Value("${zkapachebridge.rewritemap.active}")
    boolean active;

    @Value("${zkapachebridge.urls.prefix:}")
    String urlPrefix;

    @Autowired
    public RewriteMapUpdater(Configuration freemarkerConfiguration, RewriteMapWriter rewriteMapWriter, ServiceRegistry serviceRegistry) {
        this.freemarkerConfiguration = freemarkerConfiguration;
        this.rewriteMapWriter = rewriteMapWriter;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void onApplicationEvent(ServiceRegistryUpdatedEvent event) {
        if ( !active ) {
            return;
        }

        String contents = buildRewriteMapContents();
        rewriteMapWriter.write(contents);
    }

    public String getConfiguration() {
        return buildRewriteMapContents();
    }

    private String buildRewriteMapContents() {
        Map<String, EndpointCollection> services = serviceRegistry.getServices();
        StringBuffer sb = new StringBuffer();
        services.forEach((servicename, endpointCollection) ->
            sb
                .append(urlPrefix)
                .append(servicename)
                .append("\t")
                .append(String.join("|", endpointCollection.getActiveURIStrings()))
                .append("\n"));
        return sb.toString();
    }

}
