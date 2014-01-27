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
package se.sics.gvod.system.vod;

import se.sics.gvod.bootstrap.port.Rebootstrap;
import se.sics.gvod.bootstrap.port.RebootstrapResponse;
import se.sics.gvod.system.peer.events.Quit;
import se.sics.gvod.system.peer.events.Play;
import se.sics.gvod.system.peer.events.ChangeUtility;
import se.sics.gvod.system.peer.events.ReportDownloadSpeed;
import se.sics.gvod.system.peer.events.Pause;
import se.sics.gvod.system.peer.events.JumpForward;
import se.sics.gvod.system.peer.events.JumpBackward;
import se.sics.gvod.system.peer.events.ReadingCompleted;
import se.sics.gvod.system.peer.events.SpeedBackground;
import se.sics.gvod.system.peer.events.SlowBackground;
import se.sics.gvod.system.peer.events.QuitCompleted;
import se.sics.gvod.web.port.DownloadCompletedSim;
import se.sics.kompics.PortType;

/**
 *
 */
public class VodPort extends PortType {

    {
        
//        negative(VodJoin.class);
        negative(Play.class);
        negative(Pause.class);
        negative(ChangeUtility.class);
        negative(JumpBackward.class);
        negative(JumpForward.class);
        negative(Quit.class);
        positive(QuitCompleted.class);
        positive(ReadingCompleted.class);
        positive(ReportDownloadSpeed.class);
        positive(DownloadCompletedSim.class);
        positive(SlowBackground.class);
        negative(SlowBackground.class);
        positive(SpeedBackground.class);
        positive(Rebootstrap.class);
        negative(RebootstrapResponse.class);
        negative(SpeedBackground.class);

    }
}
