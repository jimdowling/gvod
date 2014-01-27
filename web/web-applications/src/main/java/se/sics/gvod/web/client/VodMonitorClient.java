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
package se.sics.gvod.web.client;

import se.sics.gvod.web.port.VodMonitorClientJoin;
import se.sics.gvod.web.port.DownloadCompletedSim;

import se.sics.gvod.timer.TimeoutId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.net.VodAddress;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Stop;
import se.sics.gvod.common.RandomSetNeighborsRequest;
import se.sics.gvod.common.RandomSetNeighborsResponse;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.timer.CancelPeriodicTimeout;
import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.web.port.Status;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.web.server.VodNeighborsNotification;
import se.sics.gvod.common.msgs.DownloadCompleted;

/**
 * The <code>GVodMonitorClient</code> class.
 * 
 * @author Cosmin Arad <cosmin@sics.se>
 * @version $Id: GVodMonitorClient.java 1217 2009-09-06 20:51:43Z Cosmin $
 */
public class VodMonitorClient extends ComponentDefinition {

    Positive<Status> status = positive(Status.class);
    Positive<VodNetwork> network = positive(VodNetwork.class);
    Positive<Timer> timer = positive(Timer.class);
    private Logger logger;
    private TimeoutId sendViewTimeoutId;
    private VodAddress monitorServerAddress;
    private VodAddress self;
    private long updatePeriod;
//    private Transport protocol;

    public VodMonitorClient() {
        subscribe(handleInit, control);
//        subscribe(handleStart, control);
        subscribe(handleStop, control);

        subscribe(handleChangeUpdatePeriod, network);
        subscribe(handleRandomSetNeighborsResponse, status);
        subscribe(handleJoin, status);
        subscribe(handleDownloadCompleted, status);
        subscribe(handleSendView, timer);
    }
    private Handler<VodMonitorClientInit> handleInit = new Handler<VodMonitorClientInit>() {

        public void handle(VodMonitorClientInit event) {
            self = event.getSelf();
            updatePeriod = event.getConfiguration().getClientUpdatePeriod();
            monitorServerAddress = ToVodAddr.monitor(
                    event.getConfiguration().getMonitorServerAddress());
//            protocol = event.getConfiguration().getProtocol();

            logger = LoggerFactory.getLogger(getClass().getName() + "@" + self.getId());
            logger.debug("handle monitorClientInit");
        }
    };
//    private Handler<Start> handleStart = new Handler<Start>() {
//
//        public void handle(Start event) {
//            logger.info("handle Start");
//            SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(
//                    updatePeriod, updatePeriod);
//            spt.setTimeoutEvent(new SendView(spt));
//            sendViewTimeoutId = spt.getTimeoutEvent().getTimeoutId();
//
//            trigger(spt, timer);
//        }
//    };
    private Handler<VodMonitorClientJoin> handleJoin = new Handler<VodMonitorClientJoin>() {

        public void handle(VodMonitorClientJoin event) {
            SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(
                    updatePeriod, updatePeriod);
            spt.setTimeoutEvent(new SendView(spt));
            sendViewTimeoutId = spt.getTimeoutEvent().getTimeoutId();

            trigger(spt, timer);
        }
    };

    private Handler<Stop> handleStop = new Handler<Stop>() {

        public void handle(Stop event) {
            logger.info("handle stop");
            trigger(new CancelPeriodicTimeout(sendViewTimeoutId), timer);
        }
    };
    private Handler<ChangeUpdatePeriod> handleChangeUpdatePeriod = new Handler<ChangeUpdatePeriod>() {

        public void handle(ChangeUpdatePeriod event) {
            updatePeriod = event.getNewUpdatePeriod();
            logger.info("handle changeperiod");
            trigger(new CancelPeriodicTimeout(sendViewTimeoutId), timer);
            SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(
                    updatePeriod, updatePeriod);
            spt.setTimeoutEvent(new SendView(spt));
            sendViewTimeoutId = spt.getTimeoutEvent().getTimeoutId();
            trigger(spt, timer);
        }
    };
    private Handler<SendView> handleSendView = new Handler<SendView>() {

        public void handle(SendView event) {
            logger.debug("SEND_VIEW");

            RandomSetNeighborsRequest request = new RandomSetNeighborsRequest();
            trigger(request, status);
        }
    };
    private Handler<RandomSetNeighborsResponse> handleRandomSetNeighborsResponse = new Handler<RandomSetNeighborsResponse>() {

        public void handle(RandomSetNeighborsResponse event) {
            logger.debug("CYCLON_NEIGHBORS_RESP");

            if (event.getNeighbors().getSelf() != null) {
                // only send notification to the server if the peer has joined
                VodNeighborsNotification viewNotification = new VodNeighborsNotification(
                        self, monitorServerAddress, 
//                        protocol,
                        event.getNeighbors(), event.getUtility().getValue());

                trigger(viewNotification, network);
            }
        }
    };
    private Handler<DownloadCompletedSim> handleDownloadCompleted = new Handler<DownloadCompletedSim>() {

        public void handle(DownloadCompletedSim event) {
            logger.debug("Download Completed");
            if (event.getPeer() != null) {
                // only send notification to the server if the peer has joined
                DownloadCompleted down = new DownloadCompleted(event.getPeer(),
                        monitorServerAddress,
                        event.getDownloadTime());
                trigger(down, network);
            }
        }
    };
}
