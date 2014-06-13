package chicken;

import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;

public class Burrito {

    public static void main(String[] args) throws Exception {
        String destUri = "ws://localhost:40000/battle";
        if (args.length > 0) {
            destUri = args[0];
        }
        System.out.printf("Battleship Server Endpoint: %s%n", destUri);
        WSHandler s1 = createClient(destUri, "foo");
        //WSHandler s2 = createClient(destUri, "bar");

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
