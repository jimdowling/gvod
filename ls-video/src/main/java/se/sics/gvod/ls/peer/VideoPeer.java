/**
 * This file is part of the Kompics P2P Framework.
 *
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS) Copyright (C)
 * 2009 Royal Institute of Technology (KTH)
 *
 * Kompics is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package se.sics.gvod.ls.peer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Negative;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.bootstrap.port.BootstrapPort;
import se.sics.gvod.bootstrap.port.BootstrapRequest;
import se.sics.gvod.bootstrap.port.BootstrapResponse;
import se.sics.gvod.bootstrap.port.Rebootstrap;
import se.sics.gvod.bootstrap.port.RebootstrapResponse;
import se.sics.kompics.Component;
import se.sics.gvod.bootstrap.client.BootstrapClient;
import se.sics.gvod.bootstrap.client.BootstrapClientInit;
import se.sics.gvod.bootstrap.port.*;
import se.sics.gvod.common.Self;
import se.sics.gvod.croupier.Croupier;
import se.sics.gvod.croupier.CroupierPort;
import se.sics.gvod.croupier.events.CroupierInit;
import se.sics.gvod.common.evts.JoinCompleted;
import se.sics.gvod.croupier.events.CroupierJoin;
import se.sics.gvod.ls.interas.InterAs;
import se.sics.gvod.ls.interas.InterAsInit;
import se.sics.gvod.ls.system.LSConfig;
import se.sics.gvod.ls.video.Video;
import se.sics.gvod.ls.video.VideoInit;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.kompics.*;

/**
 *
 * @author gautier
 */
public final class VideoPeer extends ComponentDefinition {

    Positive<VodNetwork> network = positive(VodNetwork.class);
    Positive<Timer> timer = positive(Timer.class);
    Negative<VideoPeerPort> peerPort = negative(VideoPeerPort.class);
    private Component croupier, video, interas, bootstrap;
    private Self self;
    private Logger logger = LoggerFactory.getLogger(VideoPeer.class);
    private boolean bootstrapped = false;

    public static final class BootServerKeepAlive extends Timeout {

        public BootServerKeepAlive(SchedulePeriodicTimeout request) {
            super(request);
        }
    }

    public VideoPeer() {
        bootstrap = create(BootstrapClient.class);
        croupier = create(Croupier.class);
        video = create(Video.class);
        interas = create(InterAs.class);

        connect(network, bootstrap.getNegative(VodNetwork.class));
        connect(timer, bootstrap.getNegative(Timer.class));
        connect(network, croupier.getNegative(VodNetwork.class));
        connect(timer, croupier.getNegative(Timer.class));
        connect(network, interas.getNegative(VodNetwork.class));
        connect(timer, interas.getNegative(Timer.class));
        connect(network, video.getNegative(VodNetwork.class));
        connect(timer, video.getNegative(Timer.class));

        subscribe(handleInit, control);
        subscribe(handleStop, control);
        subscribe(handleRebootstrap, croupier.getPositive(CroupierPort.class));
        subscribe(handleJoinCompleted, croupier.getPositive(CroupierPort.class));
        subscribe(handleJoin, peerPort);
        subscribe(handleBootstrapResponse, bootstrap.getPositive(BootstrapPort.class));
        subscribe(handleAddOverlayResponse, bootstrap.getPositive(BootstrapPort.class));
    }
    Handler<VideoPeerInit> handleInit = new Handler<VideoPeerInit>() {
        @Override
        public void handle(VideoPeerInit init) {

            logger.trace("handle gvodPeerInit");
            self = init.getSelf();

            trigger(new BootstrapClientInit(self,
                    init.getBootstrapConfiguration()), bootstrap.getControl());
            trigger(new CroupierInit(self, init.getCroupierConfiguration()),
                    croupier.getControl());
            trigger(new InterAsInit(self,
                    init.getCroupierConfiguration().getShufflePeriod(),
                    init.getCroupierConfiguration().getRto()),
                    interas.getControl());
            trigger(new VideoInit(self, init.isSource(), init.getVideoConfiguration()),
                    video.getControl());

            if (LSConfig.hasSourceUrlSet()) {
                AddOverlayRequest addRequest =
                        new AddOverlayRequest(LSConfig.getBootstrapServer(),
                        LSConfig.SYSTEM_OVERLAY_ID, "video",
                        "Live streaming", new byte[]{'a'}, "");
                trigger(addRequest, bootstrap.getPositive(BootstrapPort.class));
            }
        }
    };
    Handler<JoinPeer> handleJoin = new Handler<JoinPeer>() {
        @Override
        public void handle(JoinPeer event) {


            trigger(new BootstrapRequest(LSConfig.SYSTEM_OVERLAY_ID),
                    bootstrap.getPositive(BootstrapPort.class));
        }
    };
    Handler<Stop> handleStop = new Handler<Stop>() {
        @Override
        public void handle(Stop event) {
            // TODO - cleanup connections.
            // disconnect from Bootstrap server
            // Stop monitoring client
            // destroy bootstrap client
        }
    };
    Handler<Rebootstrap> handleRebootstrap = new Handler<Rebootstrap>() {
        @Override
        public void handle(Rebootstrap event) {
            BootstrapRequest request = new BootstrapRequest(event.getOverlay(), event.getUtility());
            trigger(request, bootstrap.getPositive(BootstrapPort.class));
        }
    };
    Handler<AddOverlayResponse> handleAddOverlayResponse = new Handler<AddOverlayResponse>() {
        @Override
        public void handle(AddOverlayResponse event) {
            logger.info(self.getAddress() + " adding overlay "
                    + event.getOverlayId() + " result: " + event.isSucceeded());
        }
    };
    Handler<BootstrapResponse> handleBootstrapResponse = new Handler<BootstrapResponse>() {
        @Override
        public void handle(BootstrapResponse event) {
            if (!bootstrapped) {
                logger.debug("Bootstrapped with {} peers.",
                        event.getPeers());
                trigger(new CroupierJoin(event.getPeers()), croupier.getPositive(CroupierPort.class));
                bootstrapped = true;
            } else {
                trigger(new RebootstrapResponse(self.getId(), event.getPeers()), croupier.getPositive(CroupierPort.class));
            }
        }
    };
    Handler<JoinCompleted> handleJoinCompleted = new Handler<JoinCompleted>() {
        @Override
        public void handle(JoinCompleted event) {
        }
    };
}
