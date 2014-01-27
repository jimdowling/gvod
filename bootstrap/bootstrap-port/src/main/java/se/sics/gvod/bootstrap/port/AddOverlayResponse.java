
package se.sics.gvod.bootstrap.port;


import se.sics.kompics.Response;

public final class AddOverlayResponse extends Response {

	private final boolean succeeded;
	private final int overlayId;

	public AddOverlayResponse(AddOverlayRequest request, boolean succeeded,
			int overlay) {
		super(request);
		this.succeeded = succeeded;
		this.overlayId = overlay;
	}

	public int getOverlayId() {
		return overlayId;
	}

	public boolean isSucceeded() {
		return succeeded;
	}
}
