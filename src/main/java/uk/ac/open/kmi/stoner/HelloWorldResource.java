package uk.ac.open.kmi.stoner;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Luca Panziera on 09/01/15.
 */
@Path("helloworld")
@Produces("text/plain")
public class HelloWorldResource {
	
	private static Logger log = LoggerFactory.getLogger(HelloWorldResource.class);
	
    @GET
    @Produces("text/plain")
    public String getHello() {
    	log.info("Test Info Message");
    	log.error("Test Error Message");
        return "Hello World!";
    }
}
