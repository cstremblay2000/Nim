/*
 * @filename Nim.java
 * @author Chris Tremblay
 * @date 3/25/2021, International; Waffle Day!
 *
 * The view for the Game of Nim
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * The model listener that facilitates communication
 * from the client to the server
 *
 * @author Chris Tremblay (cst1465)
 * @version 1.0
 */
public class ViewProxy implements ModelListener {

    /** The socket */
    private Socket socket;

    /** The InputStream to the client from the server */
    private DataInputStream input;

    /** The OutputStream the the client from the server */
    private DataOutputStream output;

    /** The view listener */
    private ViewListener listener;

    /**
     * Create a new view proxy
     *
     * @param socket socket to client
     */
    public ViewProxy(Socket socket){
        try{
            this.socket = socket;
            socket.setTcpNoDelay(true);
            output = new DataOutputStream(socket.getOutputStream());
            input = new DataInputStream(socket.getInputStream());
        } catch (IOException ioe){
            errorMessage(ioe);
        }
    }

    /**
     * Set the listener and start the communication handling thread
     *
     * @param listener the listener
     */
    public void setListener( ViewListener listener ){
        this.listener = listener;
        new InputThread().start();
    }

    /**
     * Report a player quit
     */
    @Override
    public void quit() {
        try {
            output.writeByte(ModelListener.QUIT);
            output.flush();
        } catch (IOException ioe) {
            errorMessage(ioe);
        }
    }

    /**
     * Report a player made a move
     *
     * @param piles the new piles formed from the move
     */
    @Override
    public void moveMade(int[] piles) {
        try{
            output.writeByte(ModelListener.MOVE_MADE);
            output.writeByte(piles.length);
            for (int pile : piles) output.writeByte(pile);
            output.flush();
        } catch (IOException ioException) {
            errorMessage(ioException);
        }
    }

    /**
     * Report that a player is waiting for other player
     */
    @Override
    public void waitingForOtherPlayer() {
        try {
            output.write(ModelListener.WAITING_OTHER_PLAYER);
            output.flush();
        } catch (IOException ioException) {
            errorMessage(ioException);
        }
    }

    /**
     * Report that it's my turn to play
     */
    @Override
    public void myTurn() {
        try{
            output.writeByte(ModelListener.MY_TURN);
            output.flush();
        } catch (IOException ioException) {
            errorMessage(ioException);
        }
    }

    /**
     * Report that it's another players turn to play
     *
     * @param player the player who's turn it is
     */
    @Override
    public void otherTurn(String player) {
        try{
            output.writeByte(ModelListener.OTHER_TURN);
            output.writeUTF(player);
            output.flush();
        } catch (IOException ioException) {
            errorMessage(ioException);
        }
    }

    /**
     * Report that this player has won
     */
    @Override
    public void youWon() {
        try{
            output.writeByte(ModelListener.YOU_WON);
            output.flush();
        } catch (IOException ioException) {
            errorMessage(ioException);
        }
    }

    /**
     * Report that another player has won
     *
     * @param player the winning player
     */
    @Override
    public void otherWin(String player) {
        try{
            output.writeByte(ModelListener.OTHER_WIN);
            output.writeUTF(player);
            output.flush();
        } catch (IOException ioException) {
            errorMessage(ioException);
        }
    }

    /**
     * Report that a new game has been started
     *
     * @param piles the list of piles and amounts
     */
    @Override
    public void newGame(int[] piles) {
        try {
            output.writeByte(ViewListener.NEW_GAME);
            output.writeByte(piles.length);
            for( int i : piles )
                output.writeByte(i);
            output.flush();
        } catch (IOException ioException) {
            errorMessage(ioException);
        }
    }

    /**
     * Exception handler for various other exceptions
     *
     * @param msg the message to print
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
     * Thread that handles input reading and processing
     *
     * @author Chris Tremblay (cst1465)
     * @version 1.0
     */
    private class InputThread extends Thread {
        public void run() {
            int instruction, pile, start, amount;
            String name;
            try {
                while (true) {
                    instruction = input.readByte();
                    switch (instruction) {
                        case ViewListener.JOIN -> {
                            name = input.readUTF();
                            listener.join(ViewProxy.this, name);
                        }
                        case ViewListener.MOVE_REQUEST -> {
                            pile = input.readByte();
                            start = input.readByte();
                            amount = input.readByte();
                            listener.moveRequest(pile, start, amount);
                        }
                        case ViewListener.NEW_GAME -> listener.newGame();
                        case ViewListener.QUIT -> listener.quit();
                        default -> errorMessage(String.format(
                                "'%s' Bad Message", instruction));
                    }
                }
            } catch (EOFException ignored) {
                // squash
            } catch (SocketException sc){
                listener.quit();
            }
            catch (IOException ioe){
                    errorMessage(ioe);
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored){}
            }
        }
    }
}
