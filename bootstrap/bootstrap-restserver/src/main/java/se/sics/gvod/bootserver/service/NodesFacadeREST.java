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
import se.sics.gvod.bootserver.entity.Nodes;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
@Stateless
@Path("nodes")
public class NodesFacadeREST extends AbstractFacade<Nodes> {
    @PersistenceContext(unitName = "se.sics.gvod_bootstrap-webserver_war_1.0-SNAPSHOTPU")
    private EntityManager em;

    public NodesFacadeREST() {
        super(Nodes.class);
    }

    @POST
    @Override
    @Consumes({"application/xml", "application/json"})
    public void create(Nodes entity) {
        super.create(entity);
    }

    @PUT
    @Override
    @Consumes({"application/xml", "application/json"})
    public void edit(Nodes entity) {
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
    public Nodes find(@PathParam("id") Integer id) {
        return super.find(id);
    }

    @GET
    @Override
    @Produces({"application/xml", "application/json"})
    public List<Nodes> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({"application/xml", "application/json"})
    public List<Nodes> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
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
