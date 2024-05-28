package com.simkin;

import com.simkin.session.Session;
import com.simkin.session.UserSessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

@Slf4j
public class GameState {

    private static final Random RANDOM = new Random();

    private final char EMPTY = ' ';
    private final char X = 'X';
    private final char O = 'O';
    private char[][] board = {
            {EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY}
    };

    public boolean runGame(@NonNull Session session) throws IOException {
        boolean isGameEnded = false;

        setRandomPlayersSide(session.getPlayerProperties1(), session.getPlayerProperties2());
        UserSessionState currentPlayerProps = session.getPlayerProperties1();
        UserSessionState waitingPlayerProps = session.getPlayerProperties2();

        currentPlayerProps.getWriter().println("Your game side: " + currentPlayerProps.getPlayerSide() + "\n");
        waitingPlayerProps.getWriter().println("Your game side: " + waitingPlayerProps.getPlayerSide() + "\n");

        boolean isOnError = false;
        String input;

        while (!isGameEnded) {
            if (!isOnError) {
                printBoard(currentPlayerProps.getWriter());
                printBoard(waitingPlayerProps.getWriter());
                currentPlayerProps.getWriter().println("Your turn! Enter your move (row and column like 1 2):");
                waitingPlayerProps.getWriter().println("Opponent's turn. Please wait\n");
            }

            Integer row = null;
            Integer col = null;

            try {
                input = currentPlayerProps.getReader().readLine();
                ClientHandler.checkOnExit(input);

                String[] parts = input.split(" ");
                if (parts.length == 2) {
                    row = Integer.parseInt(parts[0]) - 1;
                    col = Integer.parseInt(parts[1]) - 1;
                }

            } catch (NumberFormatException ex) {
                currentPlayerProps.getWriter().println("This move is not valid. Try again");
                log.error("Invalid input: ", ex);
                isOnError = true;
                continue;
            }

            if (row == null || col == null || row < 0 || col < 0 || row >= 3 || col >= 3 || board[row][col] != EMPTY) {
                currentPlayerProps.getWriter().println("Invalid move. The cell is either occupied or out of bounds." +
                        " Please enter your move in format like 1 2");
                isOnError = true;
                continue;
            } else {
                isOnError = false;
                board[row][col] = currentPlayerProps.getPlayerSide().charAt(0);
                if (checkWin(currentPlayerProps.getPlayerSide().charAt(0))) {
                    isGameEnded = true;
                    printBoard(currentPlayerProps.getWriter());
                    printBoard(waitingPlayerProps.getWriter());
                    currentPlayerProps.getWriter().println("Player " + currentPlayerProps.getPlayer().getNickname() + " wins!");
                    waitingPlayerProps.getWriter().println("Player " + currentPlayerProps.getPlayer().getNickname() + " wins!");
                } else if (isBoardFull()) {
                    isGameEnded = true;
                    printBoard(currentPlayerProps.getWriter());
                    printBoard(waitingPlayerProps.getWriter());
                    currentPlayerProps.getWriter().println("The game is a tie!");
                    waitingPlayerProps.getWriter().println("The game is a tie!");

                } else {
                    UserSessionState temp = currentPlayerProps;
                    currentPlayerProps = waitingPlayerProps;
                    waitingPlayerProps = temp;
                }
            }
        }

        return isGameEnded;
    }

    private void printBoard(@NonNull PrintWriter writer) {
        writer.println("  1   2   3 ");
        for (int i = 0; i < 3; i++) {
            writer.print((i + 1) + " ");
            for (int j = 0; j < 3; j++) {
                writer.print(" " + board[i][j] + " ");
                if (j < 2) writer.print("|");
            }
            writer.println();
            if (i < 2) writer.println("  ---+---+---");
        }
        writer.println("\n");
    }

    private boolean checkWin(char playerSide) {
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == playerSide && board[i][1] == playerSide && board[i][2] == playerSide)
                return true;
            if (board[0][i] == playerSide && board[1][i] == playerSide && board[2][i] == playerSide)
                return true;
        }
        if (board[0][0] == playerSide && board[1][1] == playerSide && board[2][2] == playerSide)
            return true;
        if (board[0][2] == playerSide && board[1][1] == playerSide && board[2][0] == playerSide)
            return true;
        return false;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    private void setRandomPlayersSide(@NonNull UserSessionState playerProps1,
                                      @NonNull UserSessionState playerProps2) {
        int randomSide = RANDOM.nextInt(2);
        char[] sides = new char[]{X, O};

        if (randomSide == 0) {
            playerProps1.setPlayerSide(String.valueOf(sides[0]));
            playerProps2.setPlayerSide(String.valueOf(sides[1]));
        } else {
            playerProps1.setPlayerSide(String.valueOf(sides[1]));
            playerProps2.setPlayerSide(String.valueOf(sides[0]));
        }
    }
}
