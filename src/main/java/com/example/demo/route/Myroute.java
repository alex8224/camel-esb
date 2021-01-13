package com.example.demo.route;

import ca.uhn.hl7v2.model.v26.message.OMP_O09;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Myroute extends RouteBuilder {

    private Logger logger = LoggerFactory.getLogger(Myroute.class);

    @Override
    public void configure() throws Exception {
//        restConfiguration().component("servlet").bindingMode(RestBindingMode.off);
        onException(Exception.class).handled(true).process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
                exception.printStackTrace();
                exchange.getMessage().setBody("parse HL7 " + exception.toString());
            }
        });

        rest().post("/hl7").to("direct:dispatch");
        from("cxf:bean:helloWs").log("request ${body} from SOAP====").to("direct:dispatch");

        from("direct:dispatch").unmarshal().hl7(false).
                log(LoggingLevel.INFO, "got other app ${headers.CamelHL7SendingApplication} from ${headers.CamelHL7ReceivingApplication}")
                .to("direct:hl7echo");

        from("direct:hl7echo").choice()
                .when(header("CamelHL7SendingApplication").isEqualTo("EMR")).to("direct:omp")
                .otherwise().to("direct:otherhl7");

        String hl7Resp = "MSH|^~\\&|OtherAPP|Tongji|EMR||202101130515||ACK^O09|aa797be5-0a5e-4808-8cc0-3a2099b6f408|P|2.6\r\nMSA|CA|aa797be5-0a5e-4808-8cc0-3a2099b6f408\r\n";

        String ompResp = "MSH|^~\\&|OmpApp|Tongji|EMR||202101130515||ACK^O09|aa797be5-0a5e-4808-8cc0-3a2099b6f408|P|2.6\r\nMSA|CA|aa797be5-0a5e-4808-8cc0-3a2099b6f408\r\n";

        from("direct:otherhl7").transform().constant(hl7Resp);

        from("direct:omp").process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                OMP_O09 ompMsg = exchange.getIn().getBody(OMP_O09.class);
                logger.info("exchange in body {}", ompMsg);
                exchange.getMessage().setBody(ompResp);
            }
        });

    }
}
