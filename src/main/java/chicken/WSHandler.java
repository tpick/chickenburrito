package chicken;


import chicken.stats.ShootStats;
import chicken.strategies.DumbFlavour;
import chicken.strategies.ProbabilityFlavour;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebSocket(maxTextMessageSize = 64 * 1024)
public class WSHandler implements Constants {

    private Session session;
    private String name;
    private volatile boolean closed = false;
    private int turns;
    private int shots;
    private int misses;
    private String opponentName;
    private int rounds;
    private int currentRound;
    private List<int[]> stats = new ArrayList<>();

    public WSHandler(String name) {
        this.name = name;
    }

    private Flavour s;
    private Field f;
    private String playerNr;
    private ShootStats shootStats;

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
        System.out.println("Connected. Starting game...");

        currentRound = 0;
        send("play");

    }

    public void send(String s) {
        try {
            session.getRemote().sendString(s);
            System.out.println(" > " + s);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    boolean waitForTurn = false;

    @OnWebSocketMessage
    public synchronized void onMessage(String msg) {
        int code;
        try {
            code = Integer.parseInt(msg.substring(0, 2), 10);
        } catch (NumberFormatException nfe) {
            // message can be ignored
            return;
        }
        System.out.println(" < " + msg);
        msg = msg.substring(4);

        //TODO: save where opponent shoots to calc statistics to optimize ship placement
        switch (code) {
            case NEW_NAME:
                break;
            case BUSY:
                session.close();
                break;
            case HELLO:
                playerNr = getPlayerNr(msg);
                send("rename " + name);
                break;
            case PLACE_SHIPS:
                nextGame();
                s.placeShips();
                break;
            case ENEMY_SHIP_HIT:
                f.lastShotHit(true, msg);
                play(code);
                shots++;
                break;
            case ENEMY_SHIP_MISSED:
                f.lastShotHit(false, msg);
                misses++;
                break;
            case ENEMY_SHIP_SUNK:
                f.lastShotSunkShip(msg);
                play(code);
                shots++;
                break;
            case YOUR_TURN:
                play(code);
                shots++;
                turns++;
                break;
            case DRONE:
            case DRONEEE:
                f.observed(msg);
                break;
            case YOU_WIN:
                s.gameOver(true);
                printStats();
                break;
            case YOU_LOSE:
                s.gameOver(false);
                printStats();
                break;
            case GAME_OVER:
                break;
            case YOUR_SHIP_MISSED:
                shootStats.miss(msg);
                break;
            case YOUR_SHIP_HIT:
                shootStats.hit(msg);
                break;
            case YOUR_SHIP_SUNK:
                shootStats.sunk(msg);
                break;
            case OUT_OF_SPECIALPOWERS:
                f.error(code);
                play(YOUR_TURN);
                break;
            case NEW_GAME:
                saveOpponentName(msg);
                break;
            case CLUSTERBOMB:
            case CLUSTERBOMBEE:
            case TORPEDO:
            case TORPEDOEE:
            case WILDFIRE:
            case WILDFIREEE:
                f.specialHit(code, msg);
                break;


        }
    }

    private void play(int code) {

        if (!waitForTurn || code == YOUR_TURN) {
            s.play();
            waitForTurn = f.getLastSpecial() != Field.Special.None;
        }
    }

    private void printStats() {
        if (currentRound >= rounds) {
            int[] stat = new int[]{turns, shots, misses};
            stats.add(stat);

            turns = 0;
            shots = 0;
            misses = 0;
            for (int[] s : stats) {
                System.out.printf("%d turns with %d shots and %d misses.%n", s[0], s[1], s[2]);
                turns += s[0];
                shots += s[1];
                misses += s[2];
            }
            System.out.printf("AVG: %d turns with %d shots and %d misses.%n", turns / rounds, shots / rounds, misses / rounds);
        }
    }

    private void nextGame() {


        if (currentRound > 0) {
            int[] stat = new int[]{turns, shots, misses};
            stats.add(stat);
        }
        currentRound++;

        if ("dumm".equals(name)) {
            s = new DumbFlavour();
        } else {
            s = new ProbabilityFlavour();
        }
        f = new Field(this, s);

        shootStats = new ShootStats();
        shootStats.setName(opponentName);
        s.configure(this, f);
        turns = 0;
        shots = 0;
        misses = 0;


    }

    private void saveOpponentName(String msg) {
        opponentName = msg.substring(msg.indexOf("vs. ") + 4, msg.length() - 12);
        rounds = Integer.parseInt(msg.substring(14, msg.indexOf("rounds vs.") - 1));


    }

    private String getPlayerNr(String msg) {
        int hashIndex = msg.indexOf('#');
        return msg.substring(hashIndex + 1, hashIndex + 2);
    }
}
