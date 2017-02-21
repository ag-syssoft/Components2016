package com.RestletAPI_Test;

import org.apache.camel.Exchange;
import org.restlet.data.MediaType;
import org.apache.camel.Message;
import org.apache.camel.Processor;



public class HeaderProcessor implements Processor
{

	@Override
	public void process(Exchange exchange) throws Exception
	{
		Message toProcess = exchange.getIn().copy();
		
		toProcess.setHeader(Exchange.CONTENT_TYPE, MediaType.APPLICATION_JSON);
		
		exchange.setOut(toProcess);
	}

}
