/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.system.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.sics.gvod.system.main.SwingMain;

/**
 *
 * @author jdowling
 */
public class ControlServerThread extends Thread {
    private Socket socket = null;
    private final SwingMain swingMain;

    public ControlServerThread(SwingMain swingMain, Socket socket) {
	super("ControlServerThread");
        this.swingMain = swingMain;
	this.socket = socket;
    }

    @Override
    public void run() {

        PrintWriter out = null;
        BufferedReader in = null;
	try {
	    out = new PrintWriter(socket.getOutputStream(), true);
	    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    String inputLine, outputLine;
	    ControlProtocol cp = new ControlProtocol(swingMain);

	    while ((inputLine = in.readLine()) != null) {
		outputLine = cp.processInput(inputLine);
                if (outputLine != null) {
                    out.print(outputLine + "\r\n");
                    out.flush();
                }
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    Logger.getLogger(ControlServerThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    Logger.getLogger(ControlServerThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}