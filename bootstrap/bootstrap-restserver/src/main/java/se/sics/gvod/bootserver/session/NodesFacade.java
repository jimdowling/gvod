/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.bootserver.session;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import se.sics.gvod.bootserver.entity.Nodes;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
@Stateless
public class NodesFacade extends AbstractFacade<Nodes> {
    @PersistenceContext(unitName = "se.sics.gvod_bootstrap-webserver_war_1.0-SNAPSHOTPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public NodesFacade() {
        super(Nodes.class);
    }
    
}
