/*
 * @filename Nim.java
 * @author Chris Tremblay
 * @date 3/17/2021, St. Patrick's Day!
 *
 * The client application for Game of Nim
 */

import java.io.IOException;
import java.net.*;

/**
 * The client side application of Game of Nim
 * @author Chris Tremblay (cst1465)
 * @version 1.0
 */
public class Nim {

    /** The usage message of the client */
    private final static String USAGE= "Usage: java Nim hostname" +
            " port-number player-name";

    /**
     * Checks command line arguments and connects to socket
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        // Check that command line args are right
        if(args.length != 3){
            System.err.println(USAGE);
            System.exit(1);
        }

        // get host name
        String host = args[0];

        // get port number
        int port = 0;
        try{
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe){
            System.err.printf("'%s' is not a valid integer\n", args[1]);
            System.err.println(USAGE);
            System.exit(1);
        }

        // get player name
        String name = args[2];

        // Try creating socket
        try{
            // get connection to server
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port));

            // Set up ModelProxy and listener
            ModelProxy proxy = new ModelProxy(socket);
            NimView view = new NimView();
            proxy.setListener(view);
            view.setListener(proxy);

            proxy.join(view, name);
        } catch (IOException ioe){
            error(ioe);
            System.exit(1);
        }
    }

    /**
     * Print an error message to the screen and exit
     * @param ioe the IOException thrown
     */
    private static void error(IOException ioe){
        System.err.println("Nim: I/O error");
        ioe.printStackTrace(System.err);
        System.exit(1);
    }
}
