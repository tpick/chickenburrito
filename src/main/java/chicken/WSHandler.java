package chicken;


import chicken.strategies.ProbabilityFlavour;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;

@WebSocket(maxTextMessageSize = 64 * 1024)
public class WSHandler implements Constants {

    private Session session;
    private String name;
    private volatile boolean closed = false;
    private int turns;
    private int shots;
    private int misses;
    public WSHandler(String name) {
        this.name = name;
    }

    private Flavour s;
    private Field f;

    public synchronized boolean waitForClose() {
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return closed;
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
        closed = true;
        synchronized (this) {
            this.notifyAll();
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;

        s = new ProbabilityFlavour();
        f = new Field(this);
        s.configure(this, f);
        turns = 0;
        System.out.println("Connected. Starting game...");
        send("play");


    }

    public void send(String s) {
        try {
            session.getRemote().sendString(s);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @OnWebSocketMessage
    public synchronized void onMessage(String msg) {
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
                send("rename "+name);
                break;
            case PLACE_SHIPS:
                s.placeShips();
                break;
            case ENEMY_SHIP_HIT:
                f.lastShotHit(true, msg);

                s.play();
                shots++;
                break;
            case ENEMY_SHIP_MISSED:
                f.lastShotHit(false, msg);
                misses++;
                break;
            case ENEMY_SHIP_SUNK:
                f.lastShotSunkShip(msg);
                s.play();
                shots++;
                break;
            case YOUR_TURN:
                s.play();
                shots++;
                turns++;
                break;
            case DRONE:
                f.observed(msg);
                break;
            case YOU_WIN:
                System.out.printf("WIN in %d turns with %d shots and %d misses.%n", turns, shots, misses);
                session.close();
                break;
            case YOU_LOSE:
                System.out.println("FAIL");
                session.close();
                break;
            case GAME_OVER:
                session.close();
                break;


        }
    }
}
