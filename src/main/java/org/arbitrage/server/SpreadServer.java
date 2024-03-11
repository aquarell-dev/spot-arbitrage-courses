package org.arbitrage.server;

import org.arbitrage.arbitrage.Arbitrage;
import org.arbitrage.coins.JSONCoinsProcessor;
import org.arbitrage.exchange.Exchange;
import org.arbitrage.manager.ExchangeManager;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SpreadServer extends WebSocketServer {
    private final Logger logger = LoggerFactory.getLogger(SpreadServer.class);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private WebSocket subscriber;
    private ExchangeManager manager;

    public SpreadServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        logger.info("New connection established: " + webSocket.getRemoteSocketAddress());
    }

    @Override
    public void onStart() {
        logger.info("Server successfully started");
        scheduler.scheduleAtFixedRate(this::sendUpdates, 3, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        logger.info(String.format("Connection closed: %s(%s, %s)", webSocket.getRemoteSocketAddress(), i, s));
        unsubscribe();
    }

    @Override
    public void onMessage(WebSocket webSocket, String message) {
        if (message.equals("ping")) {
            webSocket.send("pong");
            return;
        }

        JSONObject jsonMessage = new JSONObject(message);

        if (!jsonMessage.has("event")) {
            webSocket.send("Error: No 'event' is provided!");
            return;
        }

        String event = jsonMessage.getString("event");

        if (event.equals("unsubscribe")) {
            if (subscriber.equals(webSocket)) {
                unsubscribe();
            } else {
                webSocket.send("Error: you can't unsubscribe since you were not the one to subscribe in the first place");
            }
            return;
        }

        if (event.equals("subscribe")) {
            if (getSubscriber() == null) {
                if (!jsonMessage.has("payload")) {
                    webSocket.send("Error: there must be payload provided");
                    return;
                }

                subscribe(webSocket, jsonMessage.getJSONObject("payload"));
            } else {
                webSocket.send("Error: There's already an active subscription");
            }

            return;
        }

        webSocket.send("Error: Event should be either 'subscribe' or 'unsubscribe'");
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        logger.error(e.getMessage());
    }

    public WebSocket getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(WebSocket subscriber) {
        this.subscriber = subscriber;
    }

    private void sendUpdates() {
        if (getSubscriber() == null) return;

        List<Arbitrage> arbitrages = manager.getSpreads()
            .values()
            .stream()
            .flatMap(Set::stream)
            .toList();

        List<JSONObject> jsonSpreads = arbitrages.stream()
            .map(Arbitrage::toJSON)
            .filter(Objects::nonNull)
            .toList();

        JSONObject message = new JSONObject();

        message.put("channel", Channel.ARBITRAGE.channel);
        message.put("data", new JSONArray(jsonSpreads));

        getSubscriber().send(message.toString());
    }

    private void subscribe(WebSocket subscriber, JSONObject payload) {
        setSubscriber(subscriber);
        JSONCoinsProcessor processor = new JSONCoinsProcessor(payload);
        this.manager = new ExchangeManager(processor, Exchange.BITGET, Exchange.GATE, Exchange.HTX, Exchange.MEXC);
        this.manager.run();
    }

    private void unsubscribe() {
        this.manager = null;
        setSubscriber(null);
    }
}
