package camsetobs.alpha;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class OBSWebSocket implements WebSocket.Listener {

    private static WebSocket webSocket;
    private static Consumer<String> onSceneChanged = null;
    private static final AtomicReference<StringBuilder> messageBuilder = new AtomicReference<>(new StringBuilder());

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
        HttpClient client = HttpClient.newHttpClient();

        client.newWebSocketBuilder()
                .buildAsync(URI.create(wsUrl), new OBSWebSocket())
                .thenAccept(ws -> {
                    webSocket = ws;
                    System.out.println("Connected to OBS WebSocket.");

                    String identifyPayload = """
                        {
                            "op": 1,
                            "d": {
                                "rpcVersion": 1
                            }
                        }
                        """;

                    webSocket.sendText(identifyPayload, true);
                    System.out.println("Sent Identify payload");
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        System.out.println("WebSocket connection opened.");
        Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        messageBuilder.get().append(data);

        if (last) {
            String fullMessage = messageBuilder.get().toString();
            messageBuilder.set(new StringBuilder());

            System.out.println("OBS Message: " + fullMessage);

            if (fullMessage.contains("\"op\":0")) {
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
                webSocket.sendText(subscribePayload, true);
                System.out.println("Subscribed to scene change events");
            }

            if (fullMessage.contains("\"eventType\":\"CurrentProgramSceneChanged\"") ||
                    fullMessage.contains("\"eventType\":\"CurrentPreviewSceneChanged\"")) {
                String sceneName = extractSceneName(fullMessage);
                System.out.println("Scene changed to: " + sceneName);

                if (onSceneChanged != null) {
                    onSceneChanged.accept(sceneName);
                }
            }
        }

        return Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        System.out.println("OBS WebSocket closed: " + reason);
        return Listener.super.onClose(webSocket, statusCode, reason);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.out.println("OBS WebSocket error: " + error.getMessage());
        error.printStackTrace();
        Listener.super.onError(webSocket, error);
    }

    public static void switchToScene(String sceneName) {
        if (webSocket != null) {
            String json = "{\"op\":6,\"d\":{\"requestType\":\"SetCurrentProgramScene\",\"requestId\":\"" + sceneName + "\",\"requestData\":{\"sceneName\":\"" + sceneName + "\"}}}";
            System.out.println("Sending JSON: " + json);
            webSocket.sendText(json, true);
        } else {
            System.out.println("WebSocket is not connected.");
        }
    }
}
