/**
 * This file is part of the Kompics P2P Framework.
 * 
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS)
 * Copyright (C) 2009 Royal Institute of Technology (KTH)
 *
 * Kompics is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.sics.gvod.web.server;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.util.Properties;

import se.sics.gvod.address.Address;
import se.sics.gvod.config.VodConfig;

/**
 * The <code>GVodMonitorConfiguration</code> class.
 * 
 * @author Cosmin Arad <cosmin@sics.se>
 * @version $Id: GVodMonitorConfiguration.java 1161 2009-09-02 12:48:33Z
 *          Cosmin $
 */
public class VodMonitorConfiguration {

    private final static int DEFAULT_VIEW_EVICT_AFTER = 30 * 1000;
    private final static int DEFAULT_CLIENT_UPDATE_PERIOD = 30 * 1000;
    private final static int DEFAULT_CLIENT_WEB_PORT = VodConfig.DEFAULT_MEDIA_PORT+1;

    private final Address monitorServerAddress;
    private final long viewEvictAfter;
    private final long clientUpdatePeriod;
    private final int clientWebPort;


        public VodMonitorConfiguration(Address monitorServerAddress) {
            this(monitorServerAddress, DEFAULT_VIEW_EVICT_AFTER,
                    DEFAULT_CLIENT_UPDATE_PERIOD, DEFAULT_CLIENT_WEB_PORT);
    }

    public VodMonitorConfiguration(Address monitorServerAddress,
            long viewEvictAfter, long clientUpdatePeriod, int clientWebPort) {
        super();
        this.monitorServerAddress = monitorServerAddress;
        this.viewEvictAfter = viewEvictAfter;
        this.clientUpdatePeriod = clientUpdatePeriod;
        this.clientWebPort = clientWebPort;
    }

    public Address getMonitorServerAddress() {
        return monitorServerAddress;
    }

    public long getViewEvictAfter() {
        return viewEvictAfter;
    }

    public long getClientUpdatePeriod() {
        return clientUpdatePeriod;
    }

    public int getClientWebPort() {
        return clientWebPort;
    }

    public void store(String file) throws IOException {
        Properties p = new Properties();
        p.setProperty("view.evict.after", "" + viewEvictAfter);
        p.setProperty("client.update.period", "" + clientUpdatePeriod);
        p.setProperty("client.web.port", "" + clientWebPort);
        p.setProperty("server.ip", ""
                + monitorServerAddress.getIp().getHostAddress());
        p.setProperty("server.port", "" + monitorServerAddress.getPort());
        p.setProperty("server.id", "" + monitorServerAddress.getId());
//        p.setProperty("transport.protocol", protocol.name());

        Writer writer = new FileWriter(file);
        p.store(writer, "se.sics.kompics.p2p.monitor.cyclon");
    }

    public static VodMonitorConfiguration load(String file)
            throws IOException {
        Properties p = new Properties();
        Reader reader = new FileReader(file);
        p.load(reader);

        InetAddress ip = InetAddress.getByName(p.getProperty("server.ip"));
        int port = Integer.parseInt(p.getProperty("server.port"));
        int id = Integer.parseInt(p.getProperty("server.id"));

        Address monitorServerAddress = new Address(ip, port, id);
        long viewEvictAfter = Long.parseLong(p.getProperty("view.evict.after"));
        long clientUpdatePeriod = Long.parseLong(p.getProperty("client.update.period"));
        int clientWebPort = Integer.parseInt(p.getProperty("client.web.port"));
//        Transport protocol = Enum.valueOf(Transport.class, p.getProperty("transport.protocol"));

        return new VodMonitorConfiguration(monitorServerAddress,
                viewEvictAfter, clientUpdatePeriod, clientWebPort);
    }


}
