/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.system.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.system.main.SwingMain;

/**
 *
 * @author jdowling
 */
public class ControlServer extends Thread {

    private ServerSocket serverSocket = null;
    private boolean listening = true;
    private final SwingMain swingMain;
    private ControlServerThread cs;

    public ControlServer(SwingMain swingMain) throws IOException {

        this.swingMain = swingMain;
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        SocketAddress addr = new InetSocketAddress(VodConfig.getControlPort());
        serverSocket.bind(addr);
    }

    @Override
    public void run() {

        while (listening) {
            try {
                cs = new ControlServerThread(swingMain, serverSocket.accept());
                cs.start();
            } catch (IOException ex) {
                Logger.getLogger(ControlServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ControlServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
}
