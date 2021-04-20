/*
 * @filename NimModel.java
 * @author Chris Tremblay (cst1465)
 * @date 3/22/2021, Bavarian Crepes Day!
 *
 * This file contains a Java implementation to The Game of Nim
 */

/**
 * This class contains the rules, and state for the Game Of Nim.
 * A reference to an instance to this class will be kept on the server
 * and manipulated by the players
 *
 * @author Chris Tremblay (cst1465)
 * @version 1.0
 */
public class NimModel implements ViewListener {

    /** the original list of piles of sticks */
    private final int[] originalPiles;

    /** The list that will be used in game */
    private int[] currentPiles;

    /** player1's name */
    private String player1;

    /** player2's name */
    private String player2;

    /** The ModelListener for player1 */
    private ModelListener player1View;

    /** The ModelListener for player2 */
    private ModelListener player2View;

    /** The current players turn */
    private ModelListener currentPlayer;

    /** Check if game is finished */
    private boolean finished;

    /** If there should be verbose output */
    boolean verbose;

    /** The message that prints whose turn it is */
    private static final String VERBOSE_TURN = "%s vs. %s whose turn: %s\n";

    /** Message for starting a game */
    private static final String VERBOSE_START = "%s vs. %s  start game\n";

    /** Message for a new start */
    private static final String VERBOSE_STATE = "%s vs. %s new state: %s\n";

    /** Message for ending a game */
    private static final String VERBOSE_END = "%s vs. %s  ending game\n";

    /** Message for restarting game */
    private static final String VERBOSE_RES = "%s vs. %s  restarting game\n";

    /**
     * Create a new Nim Model
     *
     * @param piles the list of piles
     * @param verbose print messages if specified
     */
    public NimModel(int[] piles, boolean verbose){
        this.originalPiles = piles;
        this.finished = false;
        this.verbose = verbose;
    }

    /**
     * Initiate a new game
     */
    private void makeNewGame(){
        currentPiles = originalPiles.clone();
        player1View.newGame(currentPiles);
        player2View.newGame(currentPiles);
        currentPlayer = player1View;
        player1View.myTurn();
        player2View.otherTurn(player1);

        // verbose logging
        if(verbose) {
            printPiles(currentPiles);
            System.out.printf(VERBOSE_START, player1, player2);
        }
    }

    /**
     * Print the piles nicely
     *
     * @param p the list of piles
     */
    private void printPiles(int[] p){
        System.out.print("piles: ");
        for(int i : p)
            System.out.printf("%d ", i);
        System.out.println();
    }

    /**
     * Report that a player has joined a game
     *
     * @param view the view that we are talking to
     * @param name the name of the player
     */
    @Override
    public synchronized void join(ModelListener view, String name) {
        if(player1 == null){
            player1 = name;
            player1View = view;
            view.waitingForOtherPlayer();
        } else {
            player2 = name;
            player2View = view;
            makeNewGame();
        }
    }

    /**
     * Check if the game is finished or not
     *
     * @return true if game is complete, false if not
     */
    public synchronized boolean isFinished(){
        return finished;
    }

    /**
     * A request from the client to take a certain
     * amount of sticks from a pile
     *
     * @param pile  the pile number (zero indexed)
     * @param start the start amount to take
     * @param amount the amount to take from the start index
     */
    @Override
    public synchronized void moveRequest(int pile, int start, int amount) {
        // check that start and end boundaries were handled properly
        if(pile < 0 || currentPiles.length <= pile ) {
            redoMove();
        }

        // Check that bounds are correct and at least one
        // pin is being taken
        else if( currentPiles[pile] < start + amount  ){
            redoMove();
        }

        // check and see if whole piles is taken
        else if((amount-start) == currentPiles[pile]){
            removePile(pile);
            if( checkWin() )
                return;
            alertPlayers();
            switchTurns();
        }

        // check to see if it is not a split move
        else if(start == 0  || start + amount == currentPiles[pile]){
            currentPiles[pile] -= (amount);
            alertPlayers();
            switchTurns();
        }

        // must be a split move
        else {
            int[] newPiles = new int[currentPiles.length + 1];
            int secondPile = currentPiles[pile] - start - amount;
            for (int i = 0, j = 0; i < currentPiles.length; i++) {
                if (i == pile) {
                    newPiles[j] = start;
                    j++;
                    newPiles[j] = secondPile;
                } else {
                    newPiles[j] = currentPiles[i];
                }
                j++;
            }
            currentPiles = newPiles;
            alertPlayers();
            switchTurns();
        }
    }

    /**
     * Check if all the piles have been taken
     *
     * @return true if all piles are gone, false if not
     */
    private boolean checkWin(){
        if(currentPiles.length == 0){
            if(currentPlayer.equals(player1View)){
                player1View.otherWin(player2);
                player2View.youWon();
            } else {
                player1View.youWon();
                player2View.otherWin(player1);
            }
            return true;
        }
        return false;
    }

    /**
     * Notify players that board was updated and switch turns
     * around
     */
    private void alertPlayers(){
        // notify a move was made
        player1View.moveMade(currentPiles);
        player2View.moveMade(currentPiles);

        // verbose logging
        if(verbose){
            StringBuilder strPiles = new StringBuilder();
            for(int i : currentPiles)
                strPiles.append(i).append(" ");
            System.out.printf(VERBOSE_STATE, player1, player2, strPiles.toString());
        }
    }

    /**
     * Switch which players turn it was
     */
    private void switchTurns(){
        // update turn accordingly
        String p;
        if(currentPlayer.equals(player1View)) {
            p = player1;
            currentPlayer = player2View;
            player1View.otherTurn(player2);
            player2View.myTurn();
        } else {
            p = player2;
            currentPlayer = player1View;
            player1View.myTurn();
            player2View.otherTurn(player1);
        }

        if(verbose)
            System.out.printf(VERBOSE_TURN, player1, player2, p);
    }

    /**
     * Removes a pile from the current piles
     *
     * @param pile the index of the pile to remove
     */
    private void removePile(int pile){
        int[] newPiles = new int[currentPiles.length-1];
        for(int i = 0, j = 0; i < currentPiles.length; i++){
            if(i==pile)
                continue;
            newPiles[j] = currentPiles[i];
            j++;
        }
        currentPiles = newPiles;
    }

    /**
     * Prompts the player to redo their move if it
     * was invalid
     */
    private void redoMove(){
        if (currentPlayer.equals(player1View))
            player1View.myTurn();
        else
            player2View.myTurn();
    }

    /**
     * Restart the game, to the original one
     */
    @Override
    public synchronized void newGame() {
        // Remake board
        if (originalPiles.length - 1 >= 0)
            System.arraycopy(originalPiles, 1, currentPiles, 1, originalPiles.length - 1);

        // make new game
        makeNewGame();

        if(verbose)
            System.out.printf(VERBOSE_RES, player1, player2);
    }

    /**
     * Cause server to end the game
     */
    @Override
    public synchronized void quit() {
        if(player1View != null)
            player1View.quit();
        if(player2View != null)
            player2View.quit();
        currentPlayer = null;
        finished = true;

        if(verbose)
            System.out.printf(VERBOSE_END, player1, player2);
    }
}
