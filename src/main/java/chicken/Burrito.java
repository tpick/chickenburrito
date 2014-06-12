package chicken;

import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class Burrito {

    public static void main(String[] args) throws Exception {
        String destUri = "ws://localhost:40000/battle";
        if (args.length > 0) {
            destUri = args[0];
        }

        Plate s1 = createClient(destUri, "foo");
        //Plate s2 = createClient(destUri, "bar");

        s1.awaitClose(60, TimeUnit.SECONDS);

    }

    private static Plate createClient(String destUri, String name) throws Exception {
        WebSocketClient client = new WebSocketClient();
        Plate socket = new Plate(name);

            client.start();
            URI echoUri = new URI(destUri);
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            client.connect(socket, echoUri, request);
            System.out.printf("Connecting to : %s%n", echoUri);





        return socket;
    }
}
