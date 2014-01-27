/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.bootserver.session;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import se.sics.gvod.bootserver.entity.OverlayDetails;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
@Stateless
public class OverlayDetailsFacade extends AbstractFacade<OverlayDetails> {
    @PersistenceContext(unitName = "se.sics.gvod_bootstrap-webserver_war_1.0-SNAPSHOTPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public OverlayDetailsFacade() {
        super(OverlayDetails.class);
    }
    
}
