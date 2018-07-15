package eu.europa.ec.cc.zkapachebridge.apache.loadbalancer;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class LoadBalancerWriterImpl implements LoadBalancerWriter {

    @Value("${zkapachebridge.loadbalancer.path}")
    private String loadbalancerConfigPath;

    @Override
    public void write(String contents) {
        Path path = Paths.get(loadbalancerConfigPath);
        try {
            FileWriter fw = new FileWriter(path.toFile());
            fw.write(contents);
            fw.flush();
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
