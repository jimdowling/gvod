/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.bootserver.entity;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
@Entity
@Table(name = "overlays")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Overlays.findAll", query = "SELECT o FROM Overlays o"),
    @NamedQuery(name = "Overlays.findById", query = "SELECT o FROM Overlays o WHERE o.overlaysPK.id = :id"),
    @NamedQuery(name = "Overlays.findByOverlayId", query = "SELECT o FROM Overlays o WHERE o.overlaysPK.overlayId = :overlayId"),
    @NamedQuery(name = "Overlays.findByUtility", query = "SELECT o FROM Overlays o WHERE o.utility = :utility")})
public class Overlays implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected OverlaysPK overlaysPK;
    @Basic(optional = false)
    @NotNull
    @Column(name = "utility", nullable = false)
    private int utility;
    @JoinColumn(name = "overlay_id", referencedColumnName = "overlay_id", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private OverlayDetails overlayDetails;
    @JoinColumn(name = "id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Nodes nodes;

    public Overlays() {
    }

    public Overlays(OverlaysPK overlaysPK) {
        this.overlaysPK = overlaysPK;
    }

    public Overlays(OverlaysPK overlaysPK, int utility) {
        this.overlaysPK = overlaysPK;
        this.utility = utility;
    }

    public Overlays(int id, int overlayId) {
        this.overlaysPK = new OverlaysPK(id, overlayId);
    }

    public OverlaysPK getOverlaysPK() {
        return overlaysPK;
    }

    public void setOverlaysPK(OverlaysPK overlaysPK) {
        this.overlaysPK = overlaysPK;
    }

    public int getUtility() {
        return utility;
    }

    public void setUtility(int utility) {
        this.utility = utility;
    }

    public OverlayDetails getOverlayDetails() {
        return overlayDetails;
    }

    public void setOverlayDetails(OverlayDetails overlayDetails) {
        this.overlayDetails = overlayDetails;
    }

    public Nodes getNodes() {
        return nodes;
    }

    public void setNodes(Nodes nodes) {
        this.nodes = nodes;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (overlaysPK != null ? overlaysPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Overlays)) {
            return false;
        }
        Overlays other = (Overlays) object;
        if ((this.overlaysPK == null && other.overlaysPK != null) || (this.overlaysPK != null && !this.overlaysPK.equals(other.overlaysPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "se.sics.gvod.bootserver.entity.Overlays[ overlaysPK=" + overlaysPK + " ]";
    }
    
}
