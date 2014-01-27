package se.sics.gvod.ls.interas;

import se.sics.gvod.ls.interas.events.InterAsSample;
import se.sics.kompics.PortType;

public class InterAsPort extends PortType {

    {
        negative(InterAsSample.class);
        positive(InterAsSample.class);
    }
}
