/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.system.util;

import com.sun.net.httpserver.HttpHandler;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mp4.util.Mp4Split;

/**
 *
 * @author jdowling
 */
public abstract class BaseHandler extends Mp4Split implements HttpHandler
{

    protected DataOutputStream df;
    protected FileOutputStream f;
    
    public void writeBody() {
        try {
            if (df != null) {
                writeMdatBody(df);
                f.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(BaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
