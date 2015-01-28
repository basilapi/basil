package uk.ac.open.kmi.stoner;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import uk.ac.open.kmi.stoner.store.Store;

@Path("{id:([^/]+)}/api")
public class ApiResource extends AbstractResource {

	@GET
	public Response get(@PathParam("id") String id) {
	
		Store store = getDataStore();
		if (!store.existsSpec(id)) {
			return Response.status(404).build();
		}

		// XXX Not Implemented
		return Response.status(501).entity("Not implemented yet\n").build();
		
	}
}
