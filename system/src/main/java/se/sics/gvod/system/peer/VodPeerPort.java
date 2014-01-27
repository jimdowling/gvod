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
package se.sics.gvod.system.peer;

import se.sics.gvod.system.peer.events.Pause;
import se.sics.gvod.system.peer.events.Play;
import se.sics.kompics.PortType;
import se.sics.gvod.system.peer.events.ChangeUtility;
import se.sics.gvod.system.peer.events.JumpBackward;
import se.sics.gvod.system.peer.events.JumpForward;
import se.sics.gvod.system.peer.events.Quit;
import se.sics.gvod.system.peer.events.QuitCompleted;
import se.sics.gvod.system.peer.events.ReadingCompleted;
import se.sics.gvod.system.peer.events.ReportDownloadSpeed;
import se.sics.gvod.system.peer.events.SlowBackground;
import se.sics.gvod.system.peer.events.SpeedBackground;
import se.sics.gvod.web.port.DownloadCompletedSim;

public class VodPeerPort extends PortType {

    {
        //peer level events
//        negative(DataFileInfos.class); //handles
        
        //p2pvod relaying events
        negative(Play.class); //relays in
        negative(Pause.class); //relays in
        negative(ChangeUtility.class); // relays in
        negative(JumpBackward.class); // relays in
        negative(JumpForward.class); // relays in
        negative(Quit.class); // relays in
        positive(QuitCompleted.class); //relays out
        positive(ReadingCompleted.class); // relays out
        positive(ReportDownloadSpeed.class); // relays out
        positive(DownloadCompletedSim.class); // relays out
        positive(SlowBackground.class);negative(SlowBackground.class); // relaying bidirectional, depends on the source
        positive(SpeedBackground.class);negative(SpeedBackground.class); // relaying bidirectional, depends on the source
        
    }
}
