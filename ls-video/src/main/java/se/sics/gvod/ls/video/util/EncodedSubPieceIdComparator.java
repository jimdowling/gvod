package se.sics.gvod.ls.video.util;

import java.util.Comparator;
import se.sics.gvod.video.msgs.EncodedSubPiece;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class EncodedSubPieceIdComparator implements Comparator<EncodedSubPiece> {

    @Override
    public int compare(EncodedSubPiece t, EncodedSubPiece t1) {
        if(t == null && t1 == null) {
            return 0;
        }
        if(t == null) {
            return 1;
        }
        if(t1 == null) {
            return -1;
        }
        if(t.getEncodedIndex() > t1.getEncodedIndex()) {
            return -1;
        }
        if(t.getEncodedIndex() < t1.getEncodedIndex()) {
            return 1;
        }
        return 0;
    }

    
}
