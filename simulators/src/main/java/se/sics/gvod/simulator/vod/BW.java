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

/**
 * A model of the bandwidth of a node
 * @author gautier
 */
public class BW extends Number {
    private final Long uploadBw;
    private final Long downloadBw;

    public BW(Long downloadBw, Long uploadBw) {
        this.uploadBw = uploadBw;
        this.downloadBw = downloadBw;
    }

    public Long getDownloadBw() {
        return downloadBw;
    }

    public Long getUploadBw() {
        return uploadBw;
    }

    @Override
    public double doubleValue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float floatValue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int intValue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long longValue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}