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
package se.sics.gvod.simulator.vod;



import se.sics.kompics.ComponentDefinition;

/**
 *
 * @author gautier
 */
public final class VodExecutionMain extends ComponentDefinition {

//    static int seed;
//
//    static {
//        PropertyConfigurator.configureAndWatch("log4j.properties");
//    }
//    private static SimulationScenario scenario = SimulationScenario.load(System.getProperty("scenario"));
//
//    public static void main(String[] args) {
//        seed = Integer.parseInt(System.getProperty("seed"));
//        Kompics.createAndStart(GVodExecutionMain.class, 8);
//    }
//
//    public GVodExecutionMain() throws InterruptedException, IOException {
//        P2pOrchestrator.setSimulationPortType(GVodExperiment.class);
//        // create
//        Component p2pOrchestrator = create(P2pOrchestrator.class);
//        Component jettyWebServer = create(JettyWebServer.class);
//        Component bootstrapServer = create(BootstrapServer.class);
//        Component monitorServer = create(GVodMonitorServer.class);
//        Component gvodSimulator = create(GVodSimulator.class);
//
//        // loading component configurations
//        final BootstrapConfiguration bootConfiguration = BootstrapConfiguration.load(System.getProperty("bootstrap.configuration"));
//        final GVodMonitorConfiguration monitorConfiguration = GVodMonitorConfiguration
//        .load(System.getProperty("gvod.monitor.configuration"));
//        final GVodConfiguration gvodConfiguration = GVodConfiguration.load(System.getProperty("gvod.configuration"));
//        final JettyWebServerConfiguration webConfiguration = JettyWebServerConfiguration.load(System.getProperty("jetty.web.configuration"));
//        final NetworkConfiguration networkConfiguration = NetworkConfiguration.load(System.getProperty("network.configuration"));
//
//        System.out.println("For web access please go to " + "http://" + webConfiguration.getIp().getHostAddress() + ":" + webConfiguration.getPort() + "/");
//        Thread.sleep(2000);
//
//        trigger(new P2pOrchestratorInit(scenario,
//                new Latency(50,20,seed)), p2pOrchestrator.getControl());
//        trigger(new JettyWebServerInit(webConfiguration), jettyWebServer.getControl());
//        trigger(new BootstrapServerInit(bootConfiguration, false), bootstrapServer.getControl());
//        trigger(new GVodMonitorServerInit(monitorConfiguration),
//        monitorServer.getControl());
//        trigger(new GVodSimulatorInit(bootConfiguration, monitorConfiguration,
//                gvodConfiguration, networkConfiguration.getAddress()), gvodSimulator.getControl());
//
//        final class MessageDestinationFilter extends ChannelFilter<RewriteableMessage, Address> {
//
//            public MessageDestinationFilter(Address address) {
//                super(RewriteableMessage.class, address, true);
//            }
//            @Override
//            public Address getValue(RewriteableMessage event) {
//                return event.getDestination();
//            }
//        }
//        final class WebRequestDestinationFilter extends ChannelFilter<WebRequest, Integer> {
//
//            public WebRequestDestinationFilter(Integer destination) {
//                super(WebRequest.class, destination, false);
//            }
//
//            @Override
//            public Integer getValue(WebRequest event) {
//                return event.getDestination();
//            }
//        }
//
//        // connect
//        connect(bootstrapServer.getNegative(GVodNetwork.class),
//                p2pOrchestrator.getPositive(GVodNetwork.class), new MessageDestinationFilter(
//                bootConfiguration.getBootstrapServerAddress()));
//        connect(bootstrapServer.getNegative(Timer.class), p2pOrchestrator.getPositive(Timer.class));
//        connect(bootstrapServer.getPositive(Web.class), jettyWebServer.getNegative(Web.class), new WebRequestDestinationFilter(
//                bootConfiguration.getBootstrapServerAddress().getId()));
//
//       connect(monitorServer.getNegative(GVodNetwork.class),
//               p2pOrchestrator.getPositive(GVodNetwork.class), new MessageDestinationFilter(
//                monitorConfiguration.getMonitorServerAddress()));
//        connect(monitorServer.getNegative(Timer.class), p2pOrchestrator.getPositive(Timer.class));
//        connect(monitorServer.getPositive(Web.class), jettyWebServer.getNegative(Web.class), new WebRequestDestinationFilter(
//                monitorConfiguration.getMonitorServerAddress().getId()));
//
//        connect(gvodSimulator.getNegative(GVodNetwork.class),
//                p2pOrchestrator.getPositive(GVodNetwork.class));
//        connect(gvodSimulator.getNegative(Timer.class), p2pOrchestrator.getPositive(Timer.class));
//        connect(gvodSimulator.getPositive(Web.class), jettyWebServer.getNegative(Web.class));
//        connect(gvodSimulator.getNegative(GVodExperiment.class),
//                p2pOrchestrator.getPositive(GVodExperiment.class));
//    }
}