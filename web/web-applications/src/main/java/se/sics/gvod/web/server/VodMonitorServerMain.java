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

import java.io.IOException;
import java.net.InetAddress;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.net.NatNetworkControl;
import se.sics.gvod.net.VodNetwork;

import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.kompics.nat.utils.getip.ResolveIp;
import se.sics.kompics.nat.utils.getip.ResolveIpPort;
import se.sics.kompics.nat.utils.getip.events.GetIpRequest;
import se.sics.kompics.nat.utils.getip.events.GetIpResponse;
import se.sics.gvod.net.NettyInit;
import se.sics.gvod.net.NettyNetwork;
import se.sics.gvod.net.Transport;
import se.sics.gvod.net.VodMsgFrameDecoder;
import se.sics.gvod.net.events.PortBindRequest;
import se.sics.gvod.net.events.PortBindResponse;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.java.JavaTimer;
import se.sics.kompics.web.Web;
import se.sics.kompics.web.jetty.JettyWebServer;
import se.sics.kompics.web.jetty.JettyWebServerConfiguration;
import se.sics.kompics.web.jetty.JettyWebServerInit;

/**
 * The <code>GVodMonitorServerMain</code> class.
 *
 * @author Cosmin Arad <cosmin@sics.se>
 * @version $Id: GVodMonitorServerMain.java 1160 2009-09-02 12:47:38Z Cosmin $
 */
public class VodMonitorServerMain extends ComponentDefinition {

    Component MonitorServer;
    Component timer;
    Component network;
    Component web;
    private Component resolveIp;

    static {
        PropertyConfigurator.configureAndWatch("log4j.properties");
    }
    private static final Logger logger = LoggerFactory.getLogger(VodMonitorServerMain.class);

    public static void main(String[] args) {
        Kompics.createAndStart(VodMonitorServerMain.class, 1);
    }

    /**
     * Instantiates a new gvod monitor server main.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public VodMonitorServerMain() throws IOException {
        // creating components
        MonitorServer = create(VodMonitorServer.class);
        timer = create(JavaTimer.class);
        network = create(NettyNetwork.class);
//        network = create(MinaNetwork.class);
        web = create(JettyWebServer.class);
        resolveIp = create(ResolveIp.class);

        // connecting components
        connect(MonitorServer.getNegative(Timer.class), timer.getPositive(Timer.class));
        connect(MonitorServer.getNegative(VodNetwork.class), network.getPositive(VodNetwork.class));
        connect(MonitorServer.getPositive(Web.class), web.getNegative(Web.class));

        subscribe(handleGetIpResponse, resolveIp.getPositive(ResolveIpPort.class));
        trigger(new GetIpRequest(false), resolveIp.getPositive(ResolveIpPort.class));

    }
    public Handler<GetIpResponse> handleGetIpResponse = new Handler<GetIpResponse>() {

        @Override
        public void handle(GetIpResponse event) {
            try {
                logger.info("handle getIpResponse");
                InetAddress ip = event.getBoundIp();
                VodMonitorConfiguration gvodMonitorConfiguration =
                        VodMonitorConfiguration.load(System.getProperty("gvod.monitor.configuration"));
                JettyWebServerConfiguration webConfiguration = JettyWebServerConfiguration.load(System.getProperty("jetty.web.configuration"));

                JettyWebServerConfiguration webConf =
                        new JettyWebServerConfiguration(ip, webConfiguration.getPort(),
                        webConfiguration.getRequestTimeout(), webConfiguration.getMaxThreads(), webConfiguration.getHomePage());

                trigger(new VodMonitorServerInit(gvodMonitorConfiguration),
                        MonitorServer.getControl());
                trigger(new NettyInit(112, 
                        true, VodMsgFrameDecoder.class), network.getControl());
                
                PortBindRequest pb1 = new PortBindRequest(
                        gvodMonitorConfiguration.getMonitorServerAddress(), Transport.UDP);
                PortBindResponse pbr1 = new PortBindResponse(pb1) {};
                trigger(pb1, network.getPositive(NatNetworkControl.class));
                
//                trigger(new MinaNetworkInit(gvodMonitorConfiguration.getMonitorServerAddress(), 5), network.getControl());
                trigger(new JettyWebServerInit(webConf), web.getControl());

                logger.info("Started. Network={} Web={}", gvodMonitorConfiguration.getMonitorServerAddress(), webConf.getIp().getHostAddress()
                        + ":" + webConf.getPort());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
