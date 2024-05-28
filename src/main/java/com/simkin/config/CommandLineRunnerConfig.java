package com.simkin.config;

import com.simkin.ClientHandler;
import com.simkin.PlayersQueueService;
import com.simkin.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
@Configuration
public class CommandLineRunnerConfig {

    @Autowired
    private final UserService userService;

    @Autowired
    private final PlayersQueueService queueService;

    public CommandLineRunnerConfig(UserService userService,
                                   PlayersQueueService queueService) {
        this.userService = userService;
        this.queueService = queueService;
    }

    @Bean
    public CommandLineRunner run() {
        return args -> {
            int port = 23;
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                log.info("Telnet Server is running on port: {}", port);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    log.info("Client connected: {}", clientSocket.getInetAddress());
                    new Thread(new ClientHandler(clientSocket, userService, queueService)).start();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };
    }
}
