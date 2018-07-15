package eu.europa.ec.cc.zkapachebridge.zkclientsample.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;

import static java.net.NetworkInterface.getNetworkInterfaces;

// The Java class will be hosted at the URI path "/helloworld"
@RestController
@RequestMapping("/zkclient")
public class ZkClientController {
    @RequestMapping(method = RequestMethod.GET)
    public String getIPAddress(HttpServletRequest request) throws SocketException {
        ArrayList<NetworkInterface> networkInterfaces = Collections.list(getNetworkInterfaces());
        StringBuffer sb = new StringBuffer();
        for (NetworkInterface intf: networkInterfaces) {
            ArrayList<InetAddress> inetAddresses = Collections.list(intf.getInetAddresses());
            for( InetAddress inetAddress: inetAddresses ) {
                sb.append(inetAddress).append("\n");
            }
        }

        return sb.toString();
    }
}
