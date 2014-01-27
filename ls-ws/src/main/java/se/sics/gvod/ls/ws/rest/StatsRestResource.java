package se.sics.gvod.ls.ws.rest;

import com.sun.jersey.api.client.ClientResponse;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import se.sics.gvod.ls.video.snapshot.Experiment;
import se.sics.gvod.ls.ws.persistent.ExperimentEntity;
import se.sics.gvod.ls.video.snapshot.Stats;
import se.sics.gvod.ls.ws.WS;
import se.sics.gvod.ls.ws.persistent.StatsEntity;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
@Path("video")
@Stateless
public class StatsRestResource {

    @PersistenceContext(unitName = "se.sics.gvod.ls_ls-ws_war_1.0-SNAPSHOTPU")
    private EntityManager em;

    public StatsRestResource() {
    }

    @POST
    @Path("stats")
    @Consumes({"application/json"})
    public void createStats(Stats s) {
        em.persist(WS.getStatsEntity(s));
    }

    @PUT
    @Path("stats")
    @Consumes({"application/json"})
    public void editStats(Stats s) {
        em.merge(WS.getStatsEntity(s));
    }

    @DELETE
    @Path("stats/{id}")
    public void removeStats(@PathParam("id") int id) {
        em.remove(findStats(id));
    }

    @GET
    @Path("stats/{id}")
    @Produces({"application/json"})
    public Stats findStats(@PathParam("id") int id) {
        return WS.getStats(em.find(StatsEntity.class, id));
    }

    @GET
    @Path("stats/count")
    @Produces("text/plain")
    public String countStats() {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        javax.persistence.criteria.Root<StatsEntity> rt = cq.from(StatsEntity.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        javax.persistence.Query q = em.createQuery(cq);
        return String.valueOf(((Long) q.getSingleResult()).intValue());
    }

    @GET
    @Path("experiment/{id}/{iteration}")
    @Produces("application/json")
    public Response findExperimentIteration(@PathParam("id") Integer id, @PathParam("iteration") Integer iteration) {
        Query q = em.createNamedQuery("StatsEntity.findByExperimentIteration");
        q.setParameter("id", id);
        q.setParameter("iteration", iteration);
        q.setMaxResults(1);
        List l = q.getResultList();
        if(l.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            StatsEntity se = (StatsEntity) l.get(0);
            String[] strs = new String[] {"" + se.getExperimentId(), "" + se.getExperimentIteration()};
            return Response.status(Response.Status.OK).entity(strs).build();
        }
    }

    @POST
    @Path("experiment")
    @Consumes("application/json")
    public void createExperiment(Experiment e) {
        em.persist(WS.getExperimentEntity(e));
    }

    @PUT
    @Path("experiment")
    @Consumes({"application/json"})
    public void editExperiment(Experiment e) {
        em.merge(WS.getExperimentEntity(e));
    }

    @DELETE
    @Path("experiment/{id}")
    public void removeExperiment(@PathParam("id") int id) {
        em.remove(findExperiment(id));
    }

    @GET
    @Path("experiment/{id}")
    @Produces("application/json")
    public Experiment findExperiment(@PathParam("id") Integer id) {
        return WS.getExperiment(em.find(ExperimentEntity.class, id));
    }

    @GET
    @Path("experiment/count")
    @Produces("text/plain")
    public String countExperiment() {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        javax.persistence.criteria.Root<ExperimentEntity> rt = cq.from(ExperimentEntity.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        javax.persistence.Query q = em.createQuery(cq);
        return String.valueOf(((Long) q.getSingleResult()).intValue());
    }

    @GET
    @Path("experiment/max")
    @Produces("text/plain")
    public String maxExperimentId() {
        Query maxExperimentIdQuery = em.createNamedQuery("ExperimentEntity.findMaxId");
        return String.valueOf((Integer) maxExperimentIdQuery.getSingleResult());
    }
}
