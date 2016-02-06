package rest;

import javax.websocket.*;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Niko on 11/25/2015.
 */
@ClientEndpoint
public class PositionEventClientEndpoint {
    CountDownLatch latch = new CountDownLatch(1);
    private Session session;

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to server");
        this.session = session;
        //latch.countDown();
    }
    @OnMessage
    public void onText(String message, Session session) {
        System.out.println("Message received from server:" + message);
    }
    @OnClose
    public void onClose(CloseReason reason, Session session) {
        System.out.println("Closing a WebSocket due to " + reason.getReasonPhrase());
    }
    public CountDownLatch getLatch() {
        return latch;
    }

    public void sendMessage(String str) {
        try {
            session.getBasicRemote().sendText(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}