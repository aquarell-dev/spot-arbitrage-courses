package org.arbitrage.client;

import org.arbitrage.bitget.BitgetOrderbook;
import org.arbitrage.coins.CoinsProcessor;
import org.arbitrage.common.Batch;
import org.arbitrage.exchange.Exchange;
import org.arbitrage.gate.GateOrderbook;
import org.arbitrage.htx.HTXOrderbook;
import org.arbitrage.orderbook.Orderbook;
import org.arbitrage.sockets.CustomSocketClient;
import org.java_websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class SocketExchangeClient implements Client {
    protected final WebSocketClient client;
    protected final Logger logger = LoggerFactory.getLogger(SocketExchangeClient.class);
    private final CoinsProcessor processor;
    private final List<String> coins;
    private final Exchange exchange;
    private final int batchSize;
    private final Map<String, Orderbook> orderbooks = new HashMap<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final int[] milestones;
    private long subscriptionCount = 0;

    public SocketExchangeClient(Exchange exchange, CoinsProcessor processor, int batchSize) {
        this.processor = processor;
        this.coins = processor.getCoins(exchange);
        this.exchange = exchange;
        this.client = createClient();
        this.milestones = createSubscriptionMilestones(coins.size());
        this.batchSize = batchSize;
    }

    public SocketExchangeClient(Exchange exchange, CoinsProcessor processor, List<String> coins, int batchSize) {
        this.processor = processor;
        this.coins = coins;
        this.exchange = exchange;
        this.client = createClient();
        this.milestones = createSubscriptionMilestones(coins.size());
        this.batchSize = batchSize;
    }

    @Override
    public void run() {
        connect();
    }

    @Override
    public synchronized List<String> getCoins() {
        return coins;
    }

    @Override
    public synchronized Exchange getExchange() {
        return exchange;
    }

    @Override
    public synchronized Map<String, Orderbook> getOrderbooks() {
        return orderbooks;
    }

    protected abstract void onMessage(String message);

    protected abstract String getPingMessage();

    protected abstract String createSubscriptionMessage(List<String> coins);

    private void connect() {
        try {
            this.client.connectBlocking();
            afterConnection(client);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void reconnect() {
        executor.schedule(() -> {
            logger.info(String.format("%s: Attempting reconnection...", exchange.exchange));
            try {
                this.client.reconnectBlocking();
                logger.info(String.format("%s: Reconnection successful", exchange.exchange));
                afterConnection(client);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, 2, TimeUnit.MINUTES);
    }

    private void subscribe(WebSocketClient client, List<String> coins) {
        List<List<String>> batches = Batch.splitIntoBatches(coins, batchSize);

        try {
            batches.forEach(batch -> client.send(createSubscriptionMessage(batch)));
        } catch (org.java_websocket.exceptions.WebsocketNotConnectedException e) {
            logger.error(String.format("%s: Aborting subscribing due to connection error.", exchange.exchange));
        }
    }

    private void afterConnection(WebSocketClient client) {
        subscriptionCount = 0;
        orderbooks.clear();
        subscribe(client, coins);
    }

    private WebSocketClient createClient() {
        return new CustomSocketClient(exchange, this::onMessage, this::getPingMessage, this::reconnect);
    }

    protected synchronized void addCoinToOrderbook(String coin) {
        increaseSubscriptionCount();
        orderbooks.put(coin, createOrderbook(coin, exchange));
    }

    protected synchronized void updateOrderbooks() {

    }

    private Orderbook createOrderbook(String coin, Exchange exchange) {
        return switch (exchange) {
            case HTX -> new HTXOrderbook(processor.getCoin(coin, exchange));
            case GATE -> new GateOrderbook(processor.getCoin(coin, exchange));
            case BITGET -> new BitgetOrderbook(processor.getCoin(coin, exchange));
            case null, default -> throw new RuntimeException("This exchange is not supported");
        };
    }

    private void increaseSubscriptionCount() {
        subscriptionCount++;

        Integer milestone = getMilestone(subscriptionCount, milestones);

        if (milestone == null) return;

        logger.info(String.format("%s: Subscribed to %d of %d", exchange.exchange, milestone, this.coins.size()));
    }
}
