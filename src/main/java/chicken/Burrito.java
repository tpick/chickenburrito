package chicken;

import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;

public class Burrito {

    private static volatile boolean debug = false;

    public static class Lolgger {

        public void printf(String s, Object ... v) {
            if(debug) {
                System.out.printf(s,v);
            }
        }

        public void println(Object s) {
            if(debug) {
                System.out.println(s);
            }
        }

        public void print(Object s) {
            if(debug) {
                System.out.print(s);
            }

        }
    }

    public static Lolgger out = new Lolgger();

    public static void main(String[] args) throws Exception {
        String destUri = "ws://localhost:40000/battle";
        String name = "ChickenBurrito";
        for(String arg : args) {
            if(arg.startsWith("ws://")) {
                destUri = arg;
            } else if("-d".equals(arg)) {
                debug = true;
            }else {
                name = arg;
            }
        }

        Burrito.out.printf("Battleship Server Endpoint: %s%n", destUri);
        Burrito.out.printf("Player name: %s%n", name);

        WSHandler s1 = createClient(destUri, name);

        while (!s1.waitForClose()) {

        }
        System.exit(0);

    }

    private static WSHandler createClient(String destUri, String name) throws Exception {
        WebSocketClient client = new WebSocketClient();
        WSHandler socket = new WSHandler(name);

        client.start();
        URI echoUri = new URI(destUri);
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        client.connect(socket, echoUri, request);
        Burrito.out.printf("Connecting to : %s%n", echoUri);


        return socket;
    }
}
