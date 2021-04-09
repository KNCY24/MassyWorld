package com.RSS.MRM.MassyWorld;

import com.RSS.MRM.MassyWorld.generated.PallierType;
import com.RSS.MRM.MassyWorld.generated.PalliersType;
import com.RSS.MRM.MassyWorld.generated.ProductType;
import com.RSS.MRM.MassyWorld.generated.World;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

@Path("generic")
public class Webservice {
    Services services;

    public Webservice() {
        services = new Services();
    }

    @GET
    @Path("world")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getWorld(@Context HttpServletRequest request) {
        String username = request.getHeader("X-user");
        return Response.ok(services.getWorld(username)).build();
    }

    @PUT
    @Path("product")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void setProduct(ProductType newproduct, @Context HttpServletRequest request) throws JAXBException {
        String username = request.getHeader("X-user");
        services.updateProduct(username,newproduct);
    }

    @PUT
    @Path("manager")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void setManager(PallierType newmanager, @Context HttpServletRequest request) throws JAXBException {
        String username = request.getHeader("X-user");
        services.updateManager(username,newmanager);
    }

    @PUT
    @Path("upgrade")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void setUpgrade(PallierType newuprade, @Context HttpServletRequest request) throws JAXBException {
        String username = request.getHeader("X-user");
        services.updateUpgrade(username,newuprade);
    }

    @GET
    @Path("delete")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteWorld(@Context HttpServletRequest request) throws JAXBException {
        String username = request.getHeader("X-user");
        return Response.ok(services.delete(username)).build();
    }


}
