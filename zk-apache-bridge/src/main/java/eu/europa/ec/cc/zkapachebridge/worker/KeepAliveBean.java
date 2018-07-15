package eu.europa.ec.cc.zkapachebridge.worker;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class KeepAliveBean {

    @RequestMapping("/")
    public void noop() {
    }
}
