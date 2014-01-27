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
package se.sics.gvod.config;

import se.sics.gvod.system.vod.VodConfiguration;

/**
 * The
 * <code>Configuration</code> class.
 *
 */
public class VodCompositeConfiguration extends CompositeConfiguration {

    VodConfiguration vodConfiguration;

    public VodCompositeConfiguration() {
        vodConfiguration = VodConfiguration.build("topgear.mp4");
//                new VodConfiguration(
//                5 /*shuffleLength*/,
//                15 /*randomViewSize*/,
//                1000 /*shufflePeriod*/,
//                10000 /*shuffleTimeout*/,
//                //                new BigInteger("2").pow(13) /*identifierSpaceSize*/,
//                2000 /*connectionTimeout*/,
//                10 /*utilitySetSize*/,//TODO faire des tests
//                2000/*refTimeout*/,
//                2000 /*verifyPeriod*/,
//                3 /*offset*/,
//                500 /*dataOfferPeriod*/,
//                10000 /*dataRequestTimeout*/,
//                //            105 /*readingPeriod not forgoten*/,
//                //            182 /*700kbps*/,
//                125 /*1Mbps*/,
//                //                148 /* lindy 865Kbps*/,
//                //            250,
//                5/*pipeSize*/,
//                11 /*limReadingWindow*/,
//                seed,
//                10 /*upperSetSize*/,
//                11 /*belowSetSize*/,
//                64 /*infUtilFrec*/,
//                70 /*percentBack*/,
//                10000 /*checkPositionPeriod*/,
//                50 /*utilitySetFillingRate*/,
//                78643200, //10min 1Mbps
//                "golf.flv",
//                //                new Address(ip, 0, 0),
//                1024 * 5,
//                1000,
//                80,
//                //                monitorConfiguration.getMonitorServerAddress(),
//                8080 /*webserver port */,
//                1400 /* MTU */);
    }
    BootstrapConfiguration bootConfiguration = BootstrapConfiguration.build();
}
