<#assign foo = services/>

<#list services?keys as key>
    <Proxy "balancer://${key}">
        <#list services[key].activeURIStrings as URI>
            BalancerMember "${URI}"
        </#list>
    </Proxy>
    ProxyPass /${prefix}${key} balancer://${key}
</#list>
