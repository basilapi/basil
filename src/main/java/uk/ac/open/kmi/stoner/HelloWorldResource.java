package uk.ac.open.kmi.stoner;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Created by Luca Panziera on 09/01/15.
 */
@Path("helloworld")
@Produces("text/plain")
public class HelloWorldResource {
    @GET
    @Produces("text/plain")
    public String getHello() {
        return "Hello World!";
    }
}
