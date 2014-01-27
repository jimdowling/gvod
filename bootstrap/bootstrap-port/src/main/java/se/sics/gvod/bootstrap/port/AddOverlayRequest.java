package se.sics.gvod.bootstrap.port;

import se.sics.gvod.address.Address;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.net.VodAddress;
import se.sics.kompics.Request;

public class AddOverlayRequest extends Request {

    private final VodAddress bootstrapServerAddr;
    private final int overlayId;
    private final String overlayName;
    private final String description;
    private final byte[] torrentsData;
    private final String imageUrl;

    public AddOverlayRequest(Address bootstrapServerAddr, 
            int overlayId, String overlayName, String description, 
            byte[] torrentsData, String imageUrl) {
        this.bootstrapServerAddr = ToVodAddr.systemAddr(bootstrapServerAddr);
        this.overlayId = overlayId;
        this.overlayName = overlayName;
        this.description = description;
        this.torrentsData = torrentsData;
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public byte[] getTorrentsData() {
        return torrentsData;
    }
    
    public int getOverlayId() {
        return overlayId;
    }

    public String getOverlayName() {
        return overlayName;
    }

    public VodAddress getBootstrapServerAddr() {
        return bootstrapServerAddr;
    }
    
}