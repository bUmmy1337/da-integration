package net.bummy1337.dontaionalerts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.socket.client.IO;
import io.socket.client.Socket;
import net.bummy1337.daintegrate.listeners.IListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CopyOnWriteArrayList;

public class DonationAlertsClient {
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    private final URI url;
    private final IListener<DonationAlertsEvent> eventListener;
    private final CopyOnWriteArrayList<IListener<ConnectionState>> stateListeners = new CopyOnWriteArrayList<>();

    private Socket socket;
    private String currentToken;
    private volatile ConnectionState state = ConnectionState.Disconnected;
    private volatile String lastError;
    private volatile long connectedSince;
    private volatile long lastEventAt;

    public DonationAlertsClient(String server, IListener<DonationAlertsEvent> eventListener) throws URISyntaxException {
        this.url = new URI(server);
        this.eventListener = eventListener;
    }

    /**
     * Starts connecting in the background. Progress is reported through {@link #getState()}
     * and state listeners; the token is re-sent on every (re)connect.
     */
    public synchronized void connect(String token) {
        closeSocket();
        currentToken = token;
        lastError = null;
        setState(ConnectionState.Connecting);

        var options = new IO.Options();
        options.forceNew = true;
        options.reconnection = true;
        options.reconnectionDelay = 2000;
        options.reconnectionDelayMax = 15000;
        options.timeout = 10000;
        var created = IO.socket(url, options);
        socket = created;

        created.on(Socket.EVENT_CONNECT, arg -> {
            if (socket != created)
                return;
            try {
                created.emit("add-user", new JSONObject()
                        .put("token", currentToken)
                        .put("type", "minor"));
            } catch (JSONException e) {
                fail("Failed to send the token: " + e.getMessage());
                return;
            }
            connectedSince = System.currentTimeMillis();
            setState(ConnectionState.Connected);
        });
        created.on(Socket.EVENT_RECONNECTING, arg -> {
            if (socket == created)
                setState(ConnectionState.Reconnecting);
        });
        created.on(Socket.EVENT_CONNECT_ERROR, arg -> {
            if (socket == created)
                fail(describe(arg));
        });
        created.on(Socket.EVENT_ERROR, arg -> {
            if (socket == created)
                fail(describe(arg));
        });
        created.on(Socket.EVENT_DISCONNECT, arg -> {
            if (socket == created && state == ConnectionState.Connected)
                setState(ConnectionState.Reconnecting);
        });
        created.on("donation", arg -> {
            if (arg.length < 1 || !(arg[0] instanceof String json))
                return;
            lastEventAt = System.currentTimeMillis();
            eventListener.onValue(gson.fromJson(json, DonationAlertsEvent.class));
        });
        created.connect();
    }

    public synchronized void disconnect() {
        closeSocket();
        currentToken = null;
        lastError = null;
        setState(ConnectionState.Disconnected);
    }

    public boolean getConnected() {
        return state == ConnectionState.Connected;
    }

    public ConnectionState getState() {
        return state;
    }

    public String getLastError() {
        return lastError;
    }

    public long getConnectedSince() {
        return connectedSince;
    }

    public long getLastEventAt() {
        return lastEventAt;
    }

    public void addStateListener(IListener<ConnectionState> listener) {
        stateListeners.add(listener);
    }

    private void closeSocket() {
        if (socket == null)
            return;
        var old = socket;
        socket = null;
        old.off();
        old.close();
    }

    private void fail(String message) {
        lastError = message;
        setState(ConnectionState.Error);
    }

    private void setState(ConnectionState newState) {
        if (state == newState)
            return;
        state = newState;
        for (var listener : stateListeners)
            listener.onValue(newState);
    }

    private static String describe(Object[] args) {
        if (args.length == 0 || args[0] == null)
            return "Unknown error";
        if (args[0] instanceof Throwable t)
            return t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
        return String.valueOf(args[0]);
    }
}
