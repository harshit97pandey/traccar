package org.traccar.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.traccar.Context;
import org.traccar.database.ConnectionManager;
import org.traccar.geofence.Alert;
import org.traccar.geofence.Message;
import org.traccar.geofence.Notification;
import org.traccar.model.Device;
import org.traccar.model.Position;
import org.traccar.rest.utils.DateTimeFormatter;

import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.HttpCookie;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Niko on 12/4/2015.
 */
@WebSocket
@ServerEndpoint(value = "/ws/positions")
public class PositionEventEndpoint {
    private static final Map<Long, Map<Session, AsyncSession>> SESSIONS = new HashMap<>();

    private static ObjectMapper mapper = new ObjectMapper();

    {
        mapper.setConfig(mapper.getSerializationConfig().with(
                new SimpleDateFormat(DateTimeFormatter.FORMAT)));
    }

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
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("devices", deviceUpdates);
                data.put("positions", positionUpdates);

                Message message = new Message("position", data);
                String m = mapper.writeValueAsString(message);

                deviceUpdates.clear();
                positionUpdates.clear();

                session.getRemote().sendString(m);
                } catch (IOException e) {
                    e.printStackTrace();
                    session.close(1001, "Communication Error");
                }
        }

        private synchronized void response(Message message) {
            try {

                String m = mapper.writeValueAsString(message);
                session.getRemote().sendString(m);
            } catch (IOException e) {
                e.printStackTrace();
                session.close(1001, "Communication Error");
            }
        }

        public synchronized void removeListener(){
            Context.getConnectionManager().removeListener(devices, dataListener);
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        synchronized (SESSIONS) {
            Long userId = getUserId(session);

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

    private Long getUserId(Session session) {
        Long userId = null;
        List<HttpCookie> cookies = session.getUpgradeRequest().getCookies();
        for(HttpCookie cookie :cookies) {
            if (cookie.getName().equals("JSESSIONID")) {
                userId = SessionResource.sessions.get(cookie.getValue());
                if (userId != null) {
                    break;
                }
            }
        }
        return userId;
    }

    @OnWebSocketMessage
    public void onWebSocketText(String message) {


    }

    public static void showAlert(Message message){
        Alert body = (Alert)message.getBody();
        Notification notification = (Notification)body.getMessage();
        synchronized (SESSIONS) {
            Iterator<Map.Entry<Long, Map<Session, AsyncSession>>> iterator = SESSIONS.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<Long, Map<Session, AsyncSession>> session = iterator.next();
                Iterator<Map.Entry<Session, AsyncSession>> asyncSessions = session.getValue().entrySet().iterator();

                while (asyncSessions.hasNext()) {
                    Map.Entry<Session, AsyncSession> asyncSession = asyncSessions.next();
                    if (asyncSession.getValue().hasDevice(notification.getDeviceId())) {
                        asyncSession.getValue().response(message);
                    }
                }
            }
        }
    }
    @OnWebSocketClose
    public void onWebSocketClose(Session session, int status, String reason) {
        synchronized (SESSIONS) {
            Long userId = getUserId(session);
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
