/*
 * @filename ViewListener.java
 * @author Chris Tremblay (cst1465)
 * @date 3/22/2021, Bavarian Crepes Day!
 *
 * This file contains an interface that defines
 * communication to the server from the client.
 */

/**
 * The interface that defines operations the client
 * can send to the server
 *
 * @author Chris Tremblay (cst1465)
 * @version 1.0
 */
public interface ViewListener {

    /**
     * The move request instruction
     */
    int MOVE_REQUEST = 'M';

    /**
     * The new game instruction
     */
    int NEW_GAME = 'N';

    /**
     * The join instruction
     */
    int JOIN = 'J';

    /**
     * The quit instruction
     */
    int QUIT = 'Q';

    /**
     * A request from the client to take a certain
     * amount of sticks from a pile
     *
     * @param pile  the pile number (zero indexed)
     * @param start the start amount to take
     * @param amount the amount to take from the start index
     */
    void moveRequest(int pile, int start, int amount);

    /**
     * Restart the game, to a fresh one
     */
    void newGame();

    /**
     * Report that a player has joined a game
     *
     * @param view the view that we are talking to
     * @param name the name of the player
     */
    void join(ModelListener view, String name);

    /**
     * Cause server to end the game
     */
    void quit();
}
