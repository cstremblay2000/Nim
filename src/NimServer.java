/*
 * filename: NimServer.java
 * author: Chris Tremblay (cst1465)
 * date: 3/17/2021, St. Patrick's Day!
 * description:
 *      The server application for Game of Nim
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The server side application for Game of Nim
 *
 * @author Chris Tremblay (cst1465)
 * @version 1.0
 */
public class NimServer {

    /** The usage message for NimServer */
    private static final String USAGE = "Usage: java NimServer hostname port-number" +
            " [true] [pile1 [pile2 ...]]";

    /**
     * Start the server
     *
     * @param args the command line args as defined in usage message
     */
    public static void main(String[] args) {
        // check args are right
        if(args.length < 2 ){
            System.err.println(USAGE);
            System.exit(1);
        }

        // Get host name
        String host = args[0];

        // Get port number
        int port = -1;
        try{
            port = Integer.parseInt(args[1]);
        } catch (Exception e){
            System.err.printf("NimServer: port-number = \"%s\" illegal\n", args[1]);
            System.err.println(USAGE);
            System.exit(1);
        }

        // get verbose output argument
        boolean verbose = false;
        int start = 2;
        try{
            verbose = args[2].equals("true");
            start++;
        } catch (Exception ignored){
            // squash
        }

        // Get piles
        int size = args.length - start;
        int[] piles;
        if( size == 0 ){
            piles = new int[]{3, 4, 5};
        } else {

            piles = new int[size];
            int i = 3;
            try {
                for (; i < args.length; i++) {
                    piles[i - 3] = Integer.parseInt(args[i]);
                }
            } catch (NumberFormatException n) {
                System.err.printf("'%s' not a valid integer\n", args[i]);
                System.err.println(USAGE);
                System.exit(1);
            }
        }

        // Create server socket and start serving games
        try{
            // bind to socket
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(host, port));

            // start games
            NimModel model = null;
            while(true){
                // open a connection to a client
                Socket socket = serverSocket.accept();
                ViewProxy proxy = new ViewProxy(socket);
                if(model == null || model.isFinished() ){
                    model = new NimModel(piles, verbose);
                    proxy.setListener(model);
                } else {
                    proxy.setListener(model);
                    model = null;
                }
            }
        } catch (IOException ioe){
            ioError(ioe);
        }
    }

    /**
     * Handle IO exceptions when dealing with socket, and socket binding
     *
     * @param ioe the IOException
     */
    private static void ioError(IOException ioe){
        System.err.println("NimServer: IO error");
        ioe.printStackTrace(System.err);
        System.exit(1);
    }
}
