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

import se.sics.gvod.ls.system.LSConfig;

/**
 *
 * @author jim
 */
public class InterAsConfiguration 
        extends AbstractConfiguration<InterAsConfiguration>
{
    /**
     * Fields cannot be private. Package protected, ok.
     */
    long setsExchangePeriod;
    int setsExchangeRto;

    /**
     * Default constructor comes first.
     */
    public InterAsConfiguration() {
        this(BaseCommandLineConfig.getSeed(),
                LSConfig.INTER_AS_SETS_EXCHANGE_PERIOD,
                LSConfig.INTER_AS_SETS_EXCHANGE_TIMEOUT);
    }

    /**
     * Full argument constructor comes second.
     */
    public InterAsConfiguration(
            int seed,
            long setsExchangePeriod,
            int setsExchangeRto) {
        super();
        this.seed = seed;
        this.setsExchangePeriod = setsExchangePeriod;
        this.setsExchangeRto = setsExchangeRto;
    }
    
    public static InterAsConfiguration build() {
        return new InterAsConfiguration();
    }

    /**
     * @return the setsExchangeDelay
     */
    public int getSetsExchangeRto() {
        return setsExchangeRto;
    }

    /**
     * @return the setsExchangePeriod
     */
    public long getSetsExchangePeriod() {
        return setsExchangePeriod;
    }

    public InterAsConfiguration setSetsExchangePeriod(long setsExchangePeriod) {
        this.setsExchangePeriod = setsExchangePeriod;
        return this;
    }

    public InterAsConfiguration setSetsExchangeRto(int setsExchangeRto) {
        this.setsExchangeRto = setsExchangeRto;
        return this;
    }
    
    
}
