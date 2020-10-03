/*
 * Board.java
 * Author: Woosung Song
 *
 * A simple game board information storage class.
 * Much simpler than server's one. This one doesn't contains any semantic
 * calculation. It is used just for displaying client UI.
 */
package client;

public class Board {
  // basic dimension of the board
  public int rows;
  public int columns;
  // 2d array of board; element is 0 if empty, 1 if black, 2 if white
  public int board[][];

  // 1st player's ID and 2nd player's ID
  public String turnIDs[];
  // Whose turn right now
  public int turn;
  // -1: draw, 0: not determined, 1: black, 2: white
  public int winner;
  public boolean terminated;
  public String putStoneErrorMsg;

  // Constructor
  public Board(int rows, int columns) {
    this.rows = rows;
    this.columns = columns;
    initialize();
  }

  // Initialize gll game information
  public void initialize() {
    turn = 1;
    board = new int[rows][columns];
    winner = 0;
    terminated = false;
    turnIDs = new String[2];
  }

  // Set players IDs and initiate a game
  public void begin(String id1, String id2) {
    initialize();
    turnIDs = new String[2];
    turnIDs[0] = id1;
    turnIDs[1] = id2;
  }

  // ID value of the given turn, mainly used in CUI client
  private String idOfTurn(int turn) {
    switch (turn) {
      case -1:
        return "draw";
      case 0:
        return "not determined";
      case 1:
      case 2:
        return turnIDs[turn - 1]; 
    }
    return "";
  }

  // For CUI client (debug)
  @Override
  public String toString() {
    String boardString = "Turn: " + idOfTurn(turn) + ", Winner: " + idOfTurn(winner)
      + "\n======================================\n  ";

    for (int j = 0; j < columns; j++) {
      boardString += (char) ('0' + j % 10);
    }
    boardString += "\n";

    for (int i = 0; i < rows; i++) {
      boardString += (char) ('0' + i % 10) + " ";
      for (int j = 0; j < columns; j++) {
        switch (board[i][j]) {
          case 1:
            boardString += "O";
            break;
          case 2:
            boardString += "X";
            break;
          default:
            boardString += "-";
            break;
        }
      }
      boardString += "\n";
    }

    return boardString;
  }
}
