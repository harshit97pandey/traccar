package org.traccar.rest;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.traccar.Context;
import org.traccar.database.ConnectionManager;
import org.traccar.model.Device;
import org.traccar.model.Position;
import org.traccar.web.JsonConverter;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpSession;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.*;

import static org.traccar.web.BaseServlet.USER_KEY;

/**
 * Created by Niko on 12/4/2015.
 */
@WebSocket
@ServerEndpoint(value = "/ws/positions")
public class PositionEventEndpoint {
    private static final Map<Long, Map<Session, AsyncSession>> SESSIONS = new HashMap<>();

    public static void sessionRefreshUser(long userId) {
        synchronized (SESSIONS) {
            SESSIONS.remove(userId);
        }
    }

    public static void sessionRefreshDevice(long deviceId) {
        synchronized (SESSIONS) {
            Iterator<Map.Entry<Long, Map<Session, AsyncSession>>> iterator = SESSIONS.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<Long, Map<Session, AsyncSession>> session = iterator.next();
                Iterator<Map.Entry<Session, AsyncSession>> asyncSessions = session.getValue().entrySet().iterator();

                while (asyncSessions.hasNext()) {
                    Map.Entry<Session, AsyncSession> asyncSession = asyncSessions.next();
                    if (asyncSession.getValue().hasDevice(deviceId)) {
                        asyncSessions.remove();
                    }
                }
            }
        }
    }
    public static class AsyncSession {

        private final Set<Long> devices = new HashSet<>();
        private final Set<Device> deviceUpdates = new HashSet<>();
        private final Set<Position> positionUpdates = new HashSet<>();
        private Session session;

        public AsyncSession(Collection<Long> devices, Session session) {
            this.devices.addAll(devices);
            this.session = session;

            Collection<Position> initialPositions = Context.getConnectionManager().getInitialState(devices);
            for (Position position : initialPositions) {
                positionUpdates.add(position);
            }

            Context.getConnectionManager().addListener(devices, dataListener);
        }

        public boolean hasDevice(long deviceId) {
            return devices.contains(deviceId);
        }

        private final ConnectionManager.UpdateListener dataListener = new ConnectionManager.UpdateListener() {
            @Override
            public void onUpdateDevice(Device device) {
                synchronized (AsyncSession.this) {
                    deviceUpdates.add(device);
                    response();
                }
            }

            @Override
            public void onUpdatePosition(Position position) {
                synchronized (AsyncSession.this) {
                    positionUpdates.add(position);
                    response();
                }
            }
        };

        public synchronized void request() {
            if ( ! deviceUpdates.isEmpty() || ! positionUpdates.isEmpty()) {
                response();
            }
        }

        private synchronized void response() {
            JsonObjectBuilder result = Json.createObjectBuilder();
            result.add("success", true);

            if (Context.getConfig().getBoolean("web.oldAsyncFormat")) {
                result.add("data", JsonConverter.arrayToJson(positionUpdates));
            } else {
                JsonObjectBuilder data = Json.createObjectBuilder();
                data.add("devices", JsonConverter.arrayToJson(deviceUpdates));
                data.add("positions", JsonConverter.arrayToJson(positionUpdates));
                result.add("data", data.build());
            }
            deviceUpdates.clear();
            positionUpdates.clear();
            try {
                session.getRemote().sendString(result.build().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public synchronized void removeListener(){
            Context.getConnectionManager().removeListener(devices, dataListener);
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        synchronized (SESSIONS) {
            Long userId = null;
            List<HttpCookie> cookies = session.getUpgradeRequest().getCookies();
            for(HttpCookie cookie :cookies) {
                if (cookie.getName().equals("JSESSIONID")) {
                    userId = MainResource.sessions.get(cookie.getValue());
                }
            }

            Collection<Long> devices = Context.getPermissionsManager().allowedDevices(userId);
            if (!SESSIONS.containsKey(userId)) {
                Map<Session, AsyncSession> sessionMap = new HashMap<>();
                sessionMap.put(session, new AsyncSession(devices, session));
                SESSIONS.put(userId, sessionMap);
            } else {
                SESSIONS.get(userId).put(session, new AsyncSession(devices, session));
            }
            Map<Session, AsyncSession> sessionAsyncSessionMap = SESSIONS.get(userId);
            sessionAsyncSessionMap.get(session).request();
        }
    }

    @OnWebSocketMessage
    public void onWebSocketText(String message) {


    }

    @OnWebSocketClose
    public void onWebSocketClose(Session session, int status, String reason) {
        synchronized (SESSIONS) {
            HttpSession httpSession = (HttpSession) session.getUpgradeRequest().getSession();
            Long userId = (Long) httpSession.getAttribute(USER_KEY);
            Map<Session, AsyncSession> asyncSession = SESSIONS.get(userId);
            if (asyncSession.containsKey(session)) {
                asyncSession.remove(session).removeListener();
            }
            if (asyncSession.isEmpty()) {
                SESSIONS.remove(userId);
            }
        }
    }

    @OnWebSocketError
    public void onWebSocketError(Throwable cause) {
        cause.printStackTrace();
    }
}
