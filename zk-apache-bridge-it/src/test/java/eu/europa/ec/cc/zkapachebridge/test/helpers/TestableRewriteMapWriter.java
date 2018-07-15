package eu.europa.ec.cc.zkapachebridge.test.helpers;

import eu.europa.ec.cc.zkapachebridge.apache.rewritemap.RewriteMapWriter;
import org.springframework.stereotype.Component;

@Component
public class TestableRewriteMapWriter implements RewriteMapWriter {
    public String contents;

    public void write(String contents) {
        this.contents = contents;
    }
}
