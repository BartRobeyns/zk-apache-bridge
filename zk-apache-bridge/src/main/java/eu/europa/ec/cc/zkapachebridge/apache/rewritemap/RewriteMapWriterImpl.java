package eu.europa.ec.cc.zkapachebridge.apache.rewritemap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class RewriteMapWriterImpl implements RewriteMapWriter {

    @Value("${zkapachebridge.rewritemap.path}")
    String pathToRewriteMap;

    @Override
    public void write(String contents) {
        Path path = Paths.get(pathToRewriteMap);
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
