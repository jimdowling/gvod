package se.sics.gvod.ls.interas;

import se.sics.gvod.common.Self;
import se.sics.kompics.Init;

public class InterAsInit extends Init {

    private Self self;
    private final long setsExchangePeriod;
    private final long setsExchangeRto;

    public InterAsInit(Self self,
            long setsExchangePeriod, long setsExchangetRto) {
        this.self = self;
        this.setsExchangePeriod = setsExchangePeriod;
        this.setsExchangeRto = setsExchangetRto;
    }

    public long getSetsExchangeRto() {
        return setsExchangeRto;
    }

    /**
     * @return the setsExchangePeriod
     */
    public long getSetsExchangePeriod() {
        return setsExchangePeriod;
    }

    public Self getSelf() {
        return self;
    }
}
