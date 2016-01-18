package com.rasgrass.alr.server.mp3;

import com.sun.jersey.spi.resource.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/")
@Singleton
public class StaticContentController {

	@GET
	@Produces("text/html")
	public Response serve() throws FileNotFoundException {
		URL url = this.getClass().getResource("/index.html");
		File file = new File(url.getFile());

		return Response.ok(new FileInputStream(file)).status(200).build();
	}
}
