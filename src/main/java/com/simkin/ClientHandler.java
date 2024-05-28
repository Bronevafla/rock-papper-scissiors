package com.simkin;

import com.simkin.datamodel.User;
import com.simkin.service.UserService;
import com.simkin.session.Session;
import com.simkin.session.SessionStatus;
import com.simkin.session.UserSessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Slf4j
public class ClientHandler implements Runnable {

    @NonNull
    private final Socket clientSocket;

    @NonNull
    private final UserService userService;

    @NonNull
    private final PlayersQueueService queueService;


    public ClientHandler(@NonNull Socket socket,
                         @NonNull UserService userService,
                         @NonNull PlayersQueueService queueService) {
        this.clientSocket = socket;
        this.userService = userService;
        this.queueService = queueService;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            writer.println("Welcome to the game! Enter your nickname... Or enter 'exit' to close");

            String inputLine = null;
            User currentUser = null;
            User opponentUser = null;
            Session session = null;
            UserSessionState userSessionState = null;

            while (true) {
                inputLine = reader.readLine();
                if (checkOnExit(inputLine)) break;

                try {
                    currentUser = userService.getUserByNickName(inputLine);
                    if (currentUser == null) {
                        currentUser = userService.createUser(inputLine);
                        log.info("user with nickname: {} not found. created new user", inputLine);
                    }

                    queueService.addUserToQueue(currentUser);
                    writer.println("Hello, " + currentUser.getNickname() + " please wait your opponent...");
                    opponentUser = queueService.findOpponent(currentUser);
                    writer.println("The opponent " + opponentUser.getNickname() + " has been found");
                    log.info("user: {} found match-pair with user: {}", currentUser.getNickname(), opponentUser.getNickname());

                    userSessionState = UserSessionState.builder()
                            .player(currentUser)
                            .reader(reader)
                            .writer(writer)
                            .build();

                    session = Session.initSession(userSessionState, opponentUser);

                    while (!session.getSessionStatus().equals(SessionStatus.CLOSED)) {
                        Thread.sleep(100);
                    }
                    break;
                } catch (Exception ex) {
                    writer.println("error: " + ex.getMessage());
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean checkOnExit(@NonNull String inputLine) throws IOException {
        return "exit".equalsIgnoreCase(inputLine.trim());
    }
}
