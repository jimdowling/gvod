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

import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.DirectMsg;
import se.sics.gvod.net.msgs.RewriteableMsg;

/**
 * The <code>ChangeUpdatePeriod</code> class.
 * 
 * @author Cosmin Arad <cosmin@sics.se>
 * @version $Id: ChangeUpdatePeriod.java 1123 2009-09-01 09:26:35Z Cosmin $
 */
public final class ChangeUpdatePeriod extends DirectMsg {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8589306178034376017L;
	
	private final long newUpdatePeriod;

	public ChangeUpdatePeriod(long newUpdatePeriod, VodAddress source,
			VodAddress destination) {
		super(source, destination);
		this.newUpdatePeriod = newUpdatePeriod;
	}

	public long getNewUpdatePeriod() {
		return newUpdatePeriod;
	}

    @Override
    public RewriteableMsg copy() {
        ChangeUpdatePeriod copy = new ChangeUpdatePeriod(newUpdatePeriod, vodSrc, vodDest);
        copy.setTimeoutId(timeoutId);
        return copy;
    }
}
