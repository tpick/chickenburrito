package chicken;


import chicken.strategies.DumbFlavour;
import chicken.strategies.SmartFlavour;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@WebSocket(maxTextMessageSize = 64 * 1024)
public class WSHandler implements Constants {


    private final CountDownLatch closeLatch;

    private Session session;
    private String name;

    public WSHandler(String name) {
        this.closeLatch = new CountDownLatch(1);
        this.name = name;
    }

    private Flavour s;
    private Field f;

    public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
        return this.closeLatch.await(duration, unit);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
        this.session = null;
        this.closeLatch.countDown();
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.printf("Got connect: %s%n", session);
        this.session = session;

        s = new SmartFlavour();
        f = new Field(this);
        s.configure(this, f);

        send("play");


    }

    public void send(String s) {
        System.out.printf("from %s: %s%n", name, s);
        try {
            session.getRemote().sendString(s);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @OnWebSocketMessage
    public synchronized void onMessage(String msg) {
        System.out.printf("to %s: %s%n", name, msg);
        int code;
        try {
            code = Integer.parseInt(msg.substring(0, 2), 10);
        } catch (NumberFormatException nfe) {
            // message can be ignored
            return;
        }
        msg = msg.substring(4);

        switch (code) {
            case BUSY:
                session.close();
                break;
            case HELLO:
                //send("rename "+name);
                break;
            case PLACE_SHIPS:
                s.placeShips();
                break;
            case ENEMY_SHIP_HIT:
                f.lastShotHit(true, msg);
                s.play();
                break;
            case ENEMY_SHIP_MISSED:
                f.lastShotHit(false, msg);
                break;
            case ENEMY_SHIP_SUNK:
                f.lastShotSunkShip(msg);
                s.play();
                break;
            case YOUR_TURN:
                s.play();
                break;
            case DRONE:
                f.observed(msg);
                break;


        }
    }
}
