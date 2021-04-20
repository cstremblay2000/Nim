/*
 * @filename Nim.java
 * @author Chris Tremblay
 * @date 3/25/2021, International; Waffle Day!
 *
 * The view for the Game of Nim
 */

import java.util.Scanner;

/**
 * View for Nim
 * @author Chris Tremblay (cst1465)
 * @version 1.0
 */
public class NimView implements ModelListener{

    /** Keep track of the listener */
    private ViewListener listener;

    /** The state of the game */
    private int[] piles;

    /** Getting user input */
    private final Scanner userInput;

    /** The help message */
    private static final String HELP_MSG = """
                    Command  Example/Description
                    q        quit the game
                    n        request new restarted game
                    p# i# q# remove q# pins starting at index i# from pile p#
                    Commands use 0-based indexing.""";

    /**
     * Create a view object
     * Create a view object
     */
    public NimView() {
        userInput = new Scanner(System.in);
    }

    /**
     * Set the listener for the view
     *
     * @param listener the listener
     */
    public void setListener(ViewListener listener){
        this.listener = listener;
    }

    /**
     * Report a player quit
     */
    @Override
    public void quit() {
        System.out.println("quitting");
        System.exit(0);
    }

    /**
     * Print the piles nicely
     *
     * @param p the list of piles
     */
    private void printPiles(int[] p){
        System.out.print("Piles: ");
        for(int i : p)
            System.out.printf("%d ", i);
        System.out.println();
    }

    /**
     * Report a player made a move
     *
     * @param piles the new piles formed from the move
     */
    @Override
    public void moveMade(int[] piles) {
        this.piles = piles.clone();
        printPiles(this.piles);
    }

    /**
     * Report that a player is waiting for other player
     */
    @Override
    public void waitingForOtherPlayer() {
        System.out.println("Waiting for an opponent...");
    }

    /**
     * Report that it's my turn to play
     */
    @Override
    public void myTurn() {
        int pile, start, amount;
        while(true) {
            System.out.print("Your turn > ");
            String input = userInput.nextLine();

            // check if the user wants help
            if(input.equalsIgnoreCase("h") ||
            input.equalsIgnoreCase("help")) {
                printHelp();
                continue;
            }

            // see if user wants to quit
            if(input.equalsIgnoreCase("q")){
                listener.quit();
                break;
            }

            // Check if user wants to restart game
            if(input.equals("n")){
                listener.newGame();
                break;
            }

            String[] split = input.split(" ");
            try{
                // parse moves
                pile = Integer.parseInt(split[0]);
                start = Integer.parseInt(split[1]);
                amount = Integer.parseInt(split[2]);
                listener.moveRequest(pile, start, amount);
                break;
            } catch (Exception ignored){
                // squash
            }
        }
        // make move
    }

    /**
     * Prints the help message
     */
    private void printHelp(){
        System.out.println(HELP_MSG);
    }

    /**
     * Report that it's another players turn to play
     *
     * @param player the player who's turn it is
     */
    @Override
    public void otherTurn(String player) {
        System.out.printf("%s planning move.\n", player);
    }

    /**
     * Report that this player has won
     */
    @Override
    public void youWon() {
        System.out.println("You win!");
        listener.quit();
    }

    /**
     * Report that another player has won
     *
     * @param player the winning player
     */
    @Override
    public void otherWin(String player) {
        System.out.printf("%s wins!\n", player);
    }

    /**
     * Report that a new game has been started
     *
     * @param p the list of piles and amounts
     */
    @Override
    public void newGame(int[] p) {
        this.piles = new int[p.length];
        System.arraycopy(p, 0, this.piles, 0, p.length);
        System.out.println("new game started.");
        printPiles(piles);
    }
}
