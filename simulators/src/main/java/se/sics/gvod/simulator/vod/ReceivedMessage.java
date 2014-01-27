/**
 * This file is part of the ID2210 course assignments kit.
 * 
 * Copyright (C) 2009 Royal Institute of Technology (KTH)
 *
 * This program is free software; you can redistribute it and/or
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

import se.sics.gvod.net.msgs.DirectMsg;



/**
 * The <code>ReceivedMessage</code> class.
 * 
 * @author Cosmin Arad <cosmin@sics.se>
 */
public class ReceivedMessage implements Comparable<ReceivedMessage> {

	private final Class<? extends DirectMsg> messageType;

	private int totalCount;

	private long totalSize;

	public ReceivedMessage(Class<? extends DirectMsg> messageType) {
		this.messageType = messageType;
		this.totalCount = 0;
		this.totalSize = 0;
	}

	public ReceivedMessage(Class<? extends DirectMsg> messageType,
			int totalCount, long totalSize) {
		super();
		this.messageType = messageType;
		this.totalCount = totalCount;
		this.totalSize = totalSize;
	}

	public Class<? extends DirectMsg> getMessageType() {
		return messageType;
	}

	public void incrementCount() {
		totalCount++;
	}

	public void incrementSize(long size) {
		totalSize += size;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public long getTotalSize() {
		return totalSize;
	}

	@Override
	public int compareTo(ReceivedMessage that) {
		if (this.totalCount < that.totalCount)
			return 1;
		if (this.totalCount > that.totalCount)
			return -1;
		return 0;
	}

	@Override
	public String toString() {
		return totalCount + " " + messageType.getSimpleName();
	}
}
