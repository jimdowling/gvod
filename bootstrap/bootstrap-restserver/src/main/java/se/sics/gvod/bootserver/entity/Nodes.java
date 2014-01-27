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
@Table(name = "nodes")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Nodes.findAll", query = "SELECT n FROM Nodes n"),
    @NamedQuery(name = "Nodes.findById", query = "SELECT n FROM Nodes n WHERE n.id = :id"),
    @NamedQuery(name = "Nodes.findByIp", query = "SELECT n FROM Nodes n WHERE n.ip = :ip"),
    @NamedQuery(name = "Nodes.findByPort", query = "SELECT n FROM Nodes n WHERE n.port = :port"),
    @NamedQuery(name = "Nodes.findByAsn", query = "SELECT n FROM Nodes n WHERE n.asn = :asn"),
    @NamedQuery(name = "Nodes.findByCountry", query = "SELECT n FROM Nodes n WHERE n.country = :country"),
    @NamedQuery(name = "Nodes.findByNatType", query = "SELECT n FROM Nodes n WHERE n.natType = :natType"),
    @NamedQuery(name = "Nodes.findByOpen", query = "SELECT n FROM Nodes n WHERE n.open = :open"),
    @NamedQuery(name = "Nodes.findByMtu", query = "SELECT n FROM Nodes n WHERE n.mtu = :mtu"),
    @NamedQuery(name = "Nodes.findByLastPing", query = "SELECT n FROM Nodes n WHERE n.lastPing = :lastPing"),
    @NamedQuery(name = "Nodes.findByJoined", query = "SELECT n FROM Nodes n WHERE n.joined = :joined")})
public class Nodes implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "ip", nullable = false)
    private int ip;
    @Basic(optional = false)
    @NotNull
    @Column(name = "port", nullable = false)
    private short port;
    @Basic(optional = false)
    @NotNull
    @Column(name = "asn", nullable = false)
    private short asn;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 2)
    @Column(name = "country", nullable = false, length = 2)
    private String country;
    @Basic(optional = false)
    @NotNull
    @Column(name = "nat_type", nullable = false)
    private short natType;
    @Basic(optional = false)
    @NotNull
    @Column(name = "open", nullable = false)
    private boolean open;
    @Basic(optional = false)
    @NotNull
    @Column(name = "mtu", nullable = false)
    private short mtu;
    @Basic(optional = false)
    @NotNull
    @Column(name = "last_ping", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastPing;
    @Basic(optional = false)
    @NotNull
    @Column(name = "joined", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date joined;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "nodes")
    private List<Overlays> overlaysList;

    public Nodes() {
    }

    public Nodes(Integer id) {
        this.id = id;
    }

    public Nodes(Integer id, int ip, short port, short asn, String country, short natType, boolean open, short mtu, Date lastPing, Date joined) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.asn = asn;
        this.country = country;
        this.natType = natType;
        this.open = open;
        this.mtu = mtu;
        this.lastPing = lastPing;
        this.joined = joined;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getIp() {
        return ip;
    }

    public void setIp(int ip) {
        this.ip = ip;
    }

    public short getPort() {
        return port;
    }

    public void setPort(short port) {
        this.port = port;
    }

    public short getAsn() {
        return asn;
    }

    public void setAsn(short asn) {
        this.asn = asn;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public short getNatType() {
        return natType;
    }

    public void setNatType(short natType) {
        this.natType = natType;
    }

    public boolean getOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public short getMtu() {
        return mtu;
    }

    public void setMtu(short mtu) {
        this.mtu = mtu;
    }

    public Date getLastPing() {
        return lastPing;
    }

    public void setLastPing(Date lastPing) {
        this.lastPing = lastPing;
    }

    public Date getJoined() {
        return joined;
    }

    public void setJoined(Date joined) {
        this.joined = joined;
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
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Nodes)) {
            return false;
        }
        Nodes other = (Nodes) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "se.sics.gvod.bootserver.entity.Nodes[ id=" + id + " ]";
    }    
}
