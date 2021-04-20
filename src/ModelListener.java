/*
 * @filename ModelListener.java
 * @author Chris Tremblay (cst1465)
 * @date 3/22/2021, Bavarian Crepes Day!
 *
 * This file contains a Java implementation to The Game of Nim
 */

/**
 * The interface of communication from the server to the client
 *
 * @author Chris Tremlay (cst1465)
 * @version 1.0
 */
public interface ModelListener{

    /** Quit opcode */
    int QUIT = 'Q';

    /** Move Made instruction */
    int MOVE_MADE = 'M';

    /** Waiting For Other Player instruction */
    int WAITING_OTHER_PLAYER = 'P';

    /** My Turn instruction */
    int MY_TURN = 'T';

    /** Other Turn instruction */
    int OTHER_TURN = 'U';

    /** You Won Instruction */
    int YOU_WON = 'W';

    /** Other Player Won Instruction */
    int OTHER_WIN = 'O';

    /** New Game instruction */
    int NEW_GAME = 'N';

    /**
     * Report a player quit
     */
    void quit();

    /**
     * Report a player made a move
     *
     * @param piles the new piles formed from the move
     */
    void moveMade( int[] piles );

    /**
     * Report that a player is waiting for other player
     */
    void waitingForOtherPlayer();

    /**
     * Report that it's my turn to play
     */
    void myTurn();

    /**
     * Report that it's another players turn to play
     *
     * @param player the player who's turn it is
     */
    void otherTurn(String player);

    /**
     * Report that this player has won
     */
    void youWon();

    /**
     * Report that another player has won
     *
     * @param player the winning player
     */
    void otherWin(String player);

    /**
     * Report that a new game has been started
     *
     * @param piles the list of piles and amounts
     */
    void newGame(int[] piles);
}
