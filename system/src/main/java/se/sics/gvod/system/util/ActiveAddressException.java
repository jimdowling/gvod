/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.system.util;

import java.io.IOException;

/**
 *
 * @author jdowling
 */
public class ActiveAddressException extends IOException{

    public ActiveAddressException(String msg) {
        super(msg);
    }

}
