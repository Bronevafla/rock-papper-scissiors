package com.simkin.session;

import com.simkin.datamodel.User;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Getter
public class Session {

    private static final Map<String, Session> sessionIdSessionMap = new ConcurrentHashMap<>();
    private static final Map<String, UserSessionState> userIdConnectionPropertiesMap = new ConcurrentHashMap<>();

    @NonNull
    private final String sessionId;

    @NonNull
    private final UserSessionState playerProperties1;

    @NonNull
    private final UserSessionState playerProperties2;

    @NonNull
    private volatile SessionStatus sessionStatus;

    private Session(@NonNull String sessionId,
                    @NonNull UserSessionState playerProps1,
                    @NonNull UserSessionState playerProps2) {
        this.sessionId = sessionId;
        this.playerProperties1 = playerProps1;
        this.playerProperties2 = playerProps2;
        this.sessionStatus = SessionStatus.WAITING_CONNECTION;
    }

    @NonNull
    public static Session initSession(@NonNull UserSessionState userSessionState,
                                      @NonNull User opponent) {
        userIdConnectionPropertiesMap.put(
                userSessionState.getPlayer().getNickname(), userSessionState);

        UserSessionState opponentSessionProperties;
        while (true) {
            if (userIdConnectionPropertiesMap.containsKey(opponent.getNickname())) {
                opponentSessionProperties = userIdConnectionPropertiesMap.get(opponent.getNickname());
                break;
            }
        }

        String uniqueSessionId = generateSessionId(
                userSessionState.getPlayer().getNickname(),
                opponentSessionProperties.getPlayer().getNickname());

        return sessionIdSessionMap.computeIfAbsent(uniqueSessionId, key -> {
            log.info("user: {} created new session with user: {}", userSessionState.getPlayer().getNickname(),
                    opponentSessionProperties.getPlayer().getNickname());
            Session session = new Session(uniqueSessionId, userSessionState, opponentSessionProperties);
            session.setSessionStatus(SessionStatus.READY_TO_GAME);
            return session;
        });
    }

    @NonNull
    public static Map<String, Session> getPreparedSession(){
        return sessionIdSessionMap.entrySet().stream()
                .filter(kV -> kV.getValue().getSessionStatus().equals(SessionStatus.READY_TO_GAME))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @NonNull
    private static String generateSessionId(@NonNull String playerNickname1, @NonNull String playerNickname2) {
        return String.valueOf(
                Stream.of(playerNickname1, playerNickname2)
                        .sorted()
                        .collect(Collectors.joining())
                        .hashCode()
        );
    }

    public void setSessionStatus(@NonNull SessionStatus status) {
        log.info("session: {} set status: {}", this.sessionId, status);
        this.sessionStatus = status;
    }
}
