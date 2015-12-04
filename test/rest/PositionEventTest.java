package rest;


import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import java.net.URI;

/**
 * Created by niko on 12/4/15.
 */
public class PositionEventTest {

    public static void main(String[] args) {
        String dest = "ws://localhost:8082/ws/positions";
        try {
            PositionEventClientEndpoint socket = new PositionEventClientEndpoint();
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(socket, new URI(dest));
            socket.getLatch().await();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
