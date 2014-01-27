/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.bootserver.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
@Entity
@Table(name = "overlay_details")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "OverlayDetails.findAll", query = "SELECT o FROM OverlayDetails o"),
    @NamedQuery(name = "OverlayDetails.findByOverlayId", query = "SELECT o FROM OverlayDetails o WHERE o.overlayId = :overlayId"),
    @NamedQuery(name = "OverlayDetails.findByOverlayName", query = "SELECT o FROM OverlayDetails o WHERE o.overlayName = :overlayName"),
    @NamedQuery(name = "OverlayDetails.findByOverlayDescription", query = "SELECT o FROM OverlayDetails o WHERE o.overlayDescription = :overlayDescription"),
    @NamedQuery(name = "OverlayDetails.findByDateAdded", query = "SELECT o FROM OverlayDetails o WHERE o.dateAdded = :dateAdded")})
public class OverlayDetails implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "overlay_id", nullable = false)
    private Integer overlayId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 128)
    @Column(name = "overlay_name", nullable = false, length = 128)
    private String overlayName;
    @Size(max = 512)
    @Column(name = "overlay_description", length = 512)
    private String overlayDescription;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_added", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateAdded;
    @Lob
    @Column(name = "overlay_picture")
    private byte[] overlayPicture;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "overlayDetails")
    private List<Overlays> overlaysList;

    public OverlayDetails() {
    }

    public OverlayDetails(Integer overlayId) {
        this.overlayId = overlayId;
    }

    public OverlayDetails(Integer overlayId, String overlayName, Date dateAdded) {
        this.overlayId = overlayId;
        this.overlayName = overlayName;
        this.dateAdded = dateAdded;
    }

    public Integer getOverlayId() {
        return overlayId;
    }

    public void setOverlayId(Integer overlayId) {
        this.overlayId = overlayId;
    }

    public String getOverlayName() {
        return overlayName;
    }

    public void setOverlayName(String overlayName) {
        this.overlayName = overlayName;
    }

    public String getOverlayDescription() {
        return overlayDescription;
    }

    public void setOverlayDescription(String overlayDescription) {
        this.overlayDescription = overlayDescription;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public byte[] getOverlayPicture() {
        return overlayPicture;
    }

    public void setOverlayPicture(byte[] overlayPicture) {
        this.overlayPicture = overlayPicture;
    }

    @XmlTransient
    public List<Overlays> getOverlaysList() {
        return overlaysList;
    }

    public void setOverlaysList(List<Overlays> overlaysList) {
        this.overlaysList = overlaysList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (overlayId != null ? overlayId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof OverlayDetails)) {
            return false;
        }
        OverlayDetails other = (OverlayDetails) object;
        if ((this.overlayId == null && other.overlayId != null) || (this.overlayId != null && !this.overlayId.equals(other.overlayId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "se.sics.gvod.bootserver.entity.OverlayDetails[ overlayId=" + overlayId + " ]";
    }
    
}
