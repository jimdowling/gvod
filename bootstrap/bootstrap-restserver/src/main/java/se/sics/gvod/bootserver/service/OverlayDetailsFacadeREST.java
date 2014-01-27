/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.bootserver.service;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import se.sics.gvod.bootserver.entity.OverlayDetails;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
@Stateless
@Path("overlay")
public class OverlayDetailsFacadeREST extends AbstractFacade<OverlayDetails> {
    @PersistenceContext(unitName = "se.sics.gvod_bootstrap-webserver_war_1.0-SNAPSHOTPU")
    private EntityManager em;

    public OverlayDetailsFacadeREST() {
        super(OverlayDetails.class);
    }

    @POST
    @Override
    @Consumes({"application/xml", "application/json"})
    public void create(OverlayDetails entity) {
        super.create(entity);
    }

    @PUT
    @Override
    @Consumes({"application/xml", "application/json"})
    public void edit(OverlayDetails entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Integer id) {
        super.remove(super.find(id));
    }

    @GET
    @Path("{id}")
    @Produces({"application/xml", "application/json"})
    public OverlayDetails find(@PathParam("id") Integer id) {
        return super.find(id);
    }

    @GET
    @Override
    @Produces({"application/xml", "application/json"})
    public List<OverlayDetails> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({"application/xml", "application/json"})
    public List<OverlayDetails> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces("text/plain")
    public String countREST() {
        return String.valueOf(super.count());
    }

    @java.lang.Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
