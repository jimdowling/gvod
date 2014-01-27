package se.sics.asdistances;

import java.io.Serializable;

/**
 * Internal representation of IP prefixes.
 * 
 * @author Niklas Wahlén <nwahlen@kth.se>
 */
public class InternalPrefix implements Serializable {
    private static final long serialVersionUID = 2;
    private int prefix;
    private byte prefixLength;

    public InternalPrefix() {
    }

    public InternalPrefix(int prefix, byte prefixLength) {
        if (prefixLength < 0 || prefixLength > 32) {
            throw new IllegalArgumentException("Invalid prefix length");
        }
        this.prefix = ((prefix >>> (32-prefixLength)) << (32-prefixLength));
        this.prefixLength = prefixLength;
    }

    public int getPrefix() {
        return prefix;
    }

    public void setPrefix(int ip) {
        this.prefix = ip;
    }

    public byte getPrefixLength() {
        return prefixLength;
    }

    public void setPrefixLength(byte prefixLength) {
        if (prefixLength < 0 || prefixLength > 32) {
            throw new IllegalArgumentException("Invalid prefix length");
        }
        this.prefixLength = prefixLength;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final InternalPrefix other = (InternalPrefix) obj;
        if (this.prefix != other.prefix) {
            return false;
        }
        if (this.prefixLength != other.prefixLength) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.prefix;
        hash = 67 * hash + this.prefixLength;
        return hash;
    }
    
    
}
