/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.bootserver.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
@Embeddable
public class OverlaysPK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "id", nullable = false)
    private int id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "overlay_id", nullable = false)
    private int overlayId;

    public OverlaysPK() {
    }

    public OverlaysPK(int id, int overlayId) {
        this.id = id;
        this.overlayId = overlayId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOverlayId() {
        return overlayId;
    }

    public void setOverlayId(int overlayId) {
        this.overlayId = overlayId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) id;
        hash += (int) overlayId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof OverlaysPK)) {
            return false;
        }
        OverlaysPK other = (OverlaysPK) object;
        if (this.id != other.id) {
            return false;
        }
        if (this.overlayId != other.overlayId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "se.sics.gvod.bootserver.entity.OverlaysPK[ id=" + id + ", overlayId=" + overlayId + " ]";
    }
    
}
