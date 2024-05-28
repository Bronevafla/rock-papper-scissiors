package com.simkin.session;

import com.simkin.datamodel.User;
import lombok.Builder;
import lombok.Data;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@Data
@Builder
public class UserSessionState {

    @NonNull
    private final User player;

    @NonNull
    private final BufferedReader reader;

    @NonNull
    private final PrintWriter writer;

    @Nullable
    private volatile String playerSide;

    public void closeReadWriteSession() throws IOException {
        reader.close();
        writer.close();
    }
}
