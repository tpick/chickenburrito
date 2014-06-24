package chicken;

import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;

public class Burrito {

    public static void main(String[] args) throws Exception {
        String destUri = "ws://localhost:40000/battle";
        String name = "ChickenBurrito";

        if (args.length > 0) {
            if(args[0].startsWith("ws://")) {
                destUri = args[0];
            }
            name = args[0];
        }
        if (args.length > 1) {
            destUri = args[1];
        }
        System.out.printf("Battleship Server Endpoint: %s%n", destUri);
        System.out.printf("Player name: %s%n", name);

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
        System.out.printf("Connecting to : %s%n", echoUri);


        return socket;
    }
}
