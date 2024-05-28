package com.simkin;

import com.simkin.datamodel.User;
import com.simkin.exception.DataProcessingException;
import com.simkin.exception.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Slf4j
@Service
public class PlayersQueueService {

    private static final ArrayBlockingQueue<User> PLAYERS_QUEUE = new ArrayBlockingQueue<>(20, true);

    public void addUserToQueue(@NonNull User user) {
        boolean isUserWasAdded = PLAYERS_QUEUE.offer(user);
        if (isUserWasAdded) {
            log.info("user: {} was added to queue", user.getNickname());
        } else {
            throw new TimeoutException("player's queue is full. try again later");
        }
    }

    public User findOpponent(User currentPlayer) {
        User opponent;
        while (true) {
            try {
                opponent = PLAYERS_QUEUE.poll(15, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new DataProcessingException("matchmaking error");
            }
            if (opponent == null || !opponent.getNickname().equals(currentPlayer.getNickname())) {
                break;
            } else {
                PLAYERS_QUEUE.offer(opponent);
            }
        }

        log.info("{} has been removed from the queue", opponent.getNickname());
        return opponent;
    }
}
