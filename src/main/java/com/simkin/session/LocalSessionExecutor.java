package com.simkin.session;

import com.simkin.GameState;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LocalSessionExecutor implements SessionExecutor {

    private final ExecutorService executorService = Executors.newFixedThreadPool(20);
    private final Map<String, Session> readyToGameSession = new ConcurrentHashMap<>();

    @Override
    @Scheduled(fixedDelay = 500, initialDelay = 1000)
    public void run() {
        List<Callable<Boolean>> toInvokeTasks = getTasks();
        if (!toInvokeTasks.isEmpty()){
            try {
                executorService.invokeAll(toInvokeTasks, 30, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @NonNull
    private List<Callable<Boolean>> getTasks() {
        return readyToGameSession.values().stream()
                .map(session -> (Callable<Boolean>) () -> {
                    try {
                        if (session.getSessionStatus().equals(SessionStatus.READY_TO_GAME)){
                            log.info("start game session: {} on thread: {}", session.getSessionId(),
                                    Thread.currentThread().getId());
                            session.setSessionStatus(SessionStatus.IN_GAME);
                            readyToGameSession.remove(session.getSessionId());
                            GameState gameState = new GameState();
                            gameState.runGame(session);
                            session.setSessionStatus(SessionStatus.CLOSED);
                        }
                        return true;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        session.getPlayerProperties1().getWriter().println("server error. session closed");
                        session.getPlayerProperties2().getWriter().println("server error. session closed");
                        throw new RuntimeException("server error. session closed");
                    } finally {
                        session.getPlayerProperties1().closeReadWriteSession();
                        session.getPlayerProperties2().closeReadWriteSession();
                        session.setSessionStatus(SessionStatus.CLOSED);
                    }
                }).toList();
    }

    @Scheduled(fixedDelay = 100, initialDelay = 1000)
    private void updateReadySession() {
        readyToGameSession.putAll(Session.getPreparedSession());
    }
}
