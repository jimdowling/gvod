package se.sics.gvod.bootstrap.client.util;

import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.net.VodAddress;

import se.sics.gvod.address.Address;
import se.sics.gvod.config.VodConfig;

/**
 * The <code>HostsParser</code> class.
 *
 * Address format: ip:port:id,ip:port:id,ip:port:id
 * 
 * @author Jim Dowling 
 */
public final class AddressParser {

    private static final Logger logger = LoggerFactory.getLogger(AddressParser.class);

    /**
     * If a hostname from the list is not resolved, a warning is printed (no exception is thrown).
     * @param fileName
     * @return
     * @throws FileNotFoundException
     * @throws AddressParserException
     */
    public static Set<VodAddress> parseAddresses(Set<String> addresses) throws FileNotFoundException, AddressParserException {
        Set<VodAddress> addrs = new HashSet<VodAddress>();
        for (String h : addresses) {
            try {
                VodAddress addr = parseAddress(h);
                if (addr != null) {
                    addrs.add(addr);
                }
            } catch (UnknownHostException e) {
                logger.warn("Unknown host:" + e.getMessage());
            }
        }

        return addrs;
    }

    public static VodAddress parseAddress(String h) throws UnknownHostException {
        int port;
        int id;
        String[] idParts = h.split(":");

        InetAddress host = null;
        // Works for both hostnames and textual format IP addrs.
        host = InetAddress.getByName(idParts[0]);
        port = Integer.parseInt(idParts[1]);
        id = Integer.parseInt(idParts[2]);
        Address addr = new Address(host, port, id);

        return new VodAddress(addr, VodConfig.SYSTEM_OVERLAY_ID);

    }
}
