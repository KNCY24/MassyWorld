package com.RSS.MRM.MassyWorld;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("generic")
public class Webservice {
    Services services;

    public Webservice() {
        services = new Services();
    }

    @GET
    @Path("world")
    @Produces(MediaType.APPLICATION_XML)
    public Response getWorld() {
        return Response.ok(services.getWorld()).build();
    }
}
