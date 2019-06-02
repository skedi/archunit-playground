package de.eesit.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import de.eesit.pojo.TestPojo;

@Path("test")
public interface TestService {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void createOrUpdate(@PathParam("name") TestPojo name);
	
}
