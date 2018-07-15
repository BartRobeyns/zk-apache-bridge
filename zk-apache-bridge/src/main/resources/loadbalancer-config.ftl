<#assign foo = services/>

<#list services?keys as key>
    <Proxy "balancer://${key}">
        <#list services[key].endpoints as endpoint>
            BalancerMember "${endpoint.uri}"
        </#list>
    </Proxy>
    ProxyPass /${key} balancer://${key}
</#list>
