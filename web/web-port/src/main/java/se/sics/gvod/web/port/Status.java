package se.sics.gvod.web.port;

import se.sics.gvod.common.RandomSetNeighborsRequest;
import se.sics.gvod.common.RandomSetNeighborsResponse;
import se.sics.kompics.PortType;

public class Status extends PortType {
    {
		positive(RandomSetNeighborsResponse.class);
		negative(RandomSetNeighborsRequest.class);
                positive(DownloadCompletedSim.class);
                positive(VodMonitorClientJoin.class);
	}
}