package camsetobs.alpha;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec2f;
import java.util.function.Consumer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;



public class OBSWebSocket {
    private static WebSocketClient client;
    private static Consumer<String> onSceneChanged = null;

    private static String extractSceneName(String message) {
        try {
            int index = message.indexOf("\"sceneName\":\"");
            if (index == -1) return "Unknown";
            int start = index + "\"sceneName\":\"".length();
            int end = message.indexOf("\"", start);
            return message.substring(start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }



    public static void setOnSceneChanged(Consumer<String> callback) {
        onSceneChanged = callback;
    }

    public static void connect(String wsUrl) {
        try {
            if (client != null && client.isOpen()) {
                client.close();
            }

            client = new WebSocketClient(new URI(wsUrl)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Connected to OBS WebSocket.");

                    String identifyPayload = """
                        {
                            "op": 1,
                            "d": {
                            
                                "rpcVersion": 1
                                
                            }
                        }
                        """;

                    client.send(identifyPayload);
                    System.out.println("Sent Identify payload");
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("OBS Message: " + message);

                    if (message.contains("\"op\":0")) {
                        String subscribePayload = """
                        {
                            "op": 6,
                            "d": {
                            
                                "eventSubscriptions": 1,
                                "requestId": "subscribeSceneChange",
                                "requestType": "subscribe"
                                
                            }
                        }
                        """;
                        client.send(subscribePayload);
                        System.out.println("Subscribed to scene change events");
                    }

                    //event
                    if (message.contains("\"eventType\":\"CurrentProgramSceneChanged\"") || message.contains("\"eventType\":\"CurrentPreviewSceneChanged\"")) {
                        String sceneName = extractSceneName(message);
                        System.out.println("Scene changed to: " + sceneName);

                        if (onSceneChanged != null) {
                            onSceneChanged.accept(sceneName);
                        }
                    }
                }


                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("OBS Connection closed: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    System.out.println("OBS Error: " + ex.getMessage());
                    ex.printStackTrace();
                }

            };

            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void switchToScene(String sceneName) {
        if (client != null && client.isOpen()) {
            String json = "{\"op\":6,\"d\":{\"requestType\":\"SetCurrentProgramScene\",\"requestId\":\"" + sceneName + "\",\"requestData\":{\"sceneName\":\"" + sceneName + "\"}}}";
            System.out.println("Sending JSON: " + json);
            client.send(json);
        } else {
            System.out.println("WebSocket is not connected.");
        }
    }
}
