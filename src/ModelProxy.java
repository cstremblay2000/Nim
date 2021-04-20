/*
 * @filename ModelProxy.java
 * @author Chris Tremblay (cst1465)
 * @date 3/22/2021, Bavarian Crepes Day!
 *
 * This file contains the actual implementation of the clients
 * requests to the server
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

/**
 * The Model proxy, implementation of the ViewListener that defines the
 * communication from the client to the server
 *
 * @author Chris Tremlay (cst1465)
 * @version 1.0
 */
public class ModelProxy implements ViewListener {

    /** The socket we are communication with */
    private Socket socket;

    /** The input stream from the client */
    private DataInputStream input;

    /** The output stream to the client */
    private DataOutputStream output;

    /** The actual listener, only started when a connection is needed */
    private ModelListener listener;

    /**
     * Create a new ModelProxy, get streams to
     *
     * @param socket the server socket
     */
    public ModelProxy(Socket socket){
        try{
            this.socket = socket;
            this.socket.setTcpNoDelay(true);
            output = new DataOutputStream(socket.getOutputStream());
            input = new DataInputStream(socket.getInputStream());
        } catch (IOException ioe){
            errorMessage(ioe);
        }
    }

    /**
     * Set the model listener for this proxy
     *
     * @param listener the listener
     */
    public void setListener(ModelListener listener){
        this.listener = listener;
        new InputThread().start();
    }

    /**
     * A request from the client to take a certain
     * amount of sticks from a pile
     *
     * @param pile the pile number (zero indexed)
     * @param start the start amount to take
     * @param amount the ending amount to take
     */
    @Override
    public void moveRequest(int pile, int start, int amount) {
        try{
            output.writeByte(ViewListener.MOVE_REQUEST);
            output.writeByte(pile);
            output.writeByte(start);
            output.write(amount);
            output.flush();
        } catch (IOException ioException) {
            errorMessage(ioException);
        }
    }

    /**
     * Restart the game, to a fresh one
     */
    @Override
    public void newGame() {
        try{
            output.writeByte(ViewListener.NEW_GAME);
            output.flush();
        } catch (IOException ioException) {
            errorMessage(ioException);
        }
    }

    /**
     * Report that a player has joined a game
     *
     * @param view the view that we are talking to
     * @param name the name of the player
     */
    @Override
    public void join(ModelListener view, String name) {
        try{
            output.write(ViewListener.JOIN);
            output.writeUTF(name);
            output.flush();
        } catch (IOException ioe){
            errorMessage(ioe);
        }
    }

    /**
     * Cause server to end the game
     */
    @Override
    public void quit() {
        try{
            output.write(ViewListener.QUIT);
            output.flush();
        } catch (IOException ioe){
            errorMessage(ioe);
        }
    }

    /**
     * General error handler for user defined exceptions
     *
     * @param msg the Exception
     */
    private static void errorMessage(String msg){
        System.err.printf("ModelProxy: %s\n", msg);
        System.exit(1);
    }

    /**
     * Error handler for IO exceptions when initializing streams
     * in constructor
     *
     * @param exc the IOException
     */
    private static void errorMessage(IOException exc){
        System.err.println("ModelProxy: IO error");
        exc.printStackTrace(System.err);
        System.exit(1);
    }

    /**
     * The thread that handles input from network and decodes them
     *
     * @author Chris Tremblay (cst1465)
     * @version 1.0
     */
    private class InputThread extends Thread{
        /**
         * Run the input handling thread
         */
        public void run(){
            int instruction, pile, amount, size;
            int[] piles;
            String name;

            try {
                while (true) {
                    instruction = input.readByte();
                    switch (instruction) {
                        case ModelListener.QUIT:
                            listener.quit();
                            break;
                        case ModelListener.MOVE_MADE:
                            size = input.readByte();
                            piles = new int[size];
                            pile = 0;
                            while(pile != size){
                                amount = input.readByte();
                                piles[pile] = amount;
                                pile++;
                            }
                            listener.moveMade(piles);
                            break;
                        case ModelListener.WAITING_OTHER_PLAYER:
                            listener.waitingForOtherPlayer();
                            break;
                        case ModelListener.MY_TURN:
                            listener.myTurn();
                            break;
                        case ModelListener.OTHER_TURN:
                            name = input.readUTF();
                            listener.otherTurn(name);
                            break;
                        case ModelListener.YOU_WON:
                            listener.youWon();
                            break;
                        case ModelListener.OTHER_WIN:
                            name = input.readUTF();
                            listener.otherWin(name);
                            break;
                        case ModelListener.NEW_GAME:
                            size = input.readByte();
                            piles = new int[size];
                            pile = 0;
                            while(pile != size){
                                amount = input.readByte();
                                piles[pile] = amount;
                                pile++;
                            }
                            listener.newGame(piles);
                            break;
                        default:
                            errorMessage(String.format(
                                    "Bad Instruction '%s'\n", instruction));
                    }
                }
            } catch (EOFException ignored) {
                // squash
            } catch (IOException ioe){
                errorMessage(ioe);
            } finally {
                try{
                    listener.quit();
                    System.err.println("Quit");
                    socket.close();
                } catch (Exception ignored){}
            }
        }
    }
}
