import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import java.util.Map;

class MyLogProcessor implements Processor {
    public void process(Exchange exchange) throws Exception {
        Message in = exchange.getIn();
        String body = in.getBody(String.class);
        Map map = in.getHeaders();
        for (Object o : map.entrySet()) {
            Map.Entry thisEntry = (Map.Entry) o;
            Object key = thisEntry.getKey();
            Object value = thisEntry.getValue();
            System.out.println("Key: " + key + " Value : " + value.toString());
        }
        Core.logger.info("From: " + in.getHeader("From") + " Body: \n" + body);
        exchange.getOut().setBody(body);
    }
}