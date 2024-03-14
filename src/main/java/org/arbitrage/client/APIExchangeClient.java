package org.arbitrage.client;

import org.arbitrage.coins.CoinsProcessor;
import org.arbitrage.exchange.Exchange;
import org.arbitrage.orderbook.Orderbook;
import org.arbitrage.semaphore.SemaphoreManager;
import org.arbitrage.server.SpreadServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public abstract class APIExchangeClient implements Client {
    protected final HttpClient httpClient;
    protected final Logger logger = LoggerFactory.getLogger(SpreadServer.class);
    private final List<String> coins;
    private final Exchange exchange;
    protected final Map<String, Orderbook> orderbooks = new HashMap<>();
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    protected final CoinsProcessor processor;
    private final SemaphoreManager<Void> semaphoreManager;
    private final int[] milestones;
    private long subscriptionCount = 0;

    public APIExchangeClient(Exchange exchange, CoinsProcessor processor, SemaphoreManager<Void> semaphoreManager) {
        this.processor = processor;
        this.exchange = exchange;
        this.coins = processor.getCoins(exchange);
        this.executorService = Executors.newFixedThreadPool(coins.size());
        this.httpClient = HttpClient.newBuilder()
            .executor(executorService)
            .build();
        this.semaphoreManager = semaphoreManager;
        this.milestones = createSubscriptionMilestones(coins.size());
    }

    protected abstract URI getOrderbookUriByCoin(String coin);

    protected abstract void processResponse(String coin, HttpResponse<String> response);

    @Override
    public void run() {
        logger.info(String.format("%s: Connection established", exchange.exchange));
        while (true) fetchOrderbooks();

    }

    @Override
    public List<String> getCoins() {
        return coins;
    }

    @Override
    public Exchange getExchange() {
        return exchange;
    }

    @Override
    public Map<String, Orderbook> getOrderbooks() {
        return orderbooks;
    }

    protected Map<String, HttpRequest> getRequests() {
        return this.coins.stream()
            .collect(Collectors.toMap(coin -> coin,
                coin -> HttpRequest.newBuilder()
                    .uri(getOrderbookUriByCoin(coin))
                    .GET()
                    .header("Content-Type", "application/json")
                    .header("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:123.0) Gecko/20100101 Firefox/123.0"
                    )
                    .build()
            ));
    }

    protected void increaseSubscriptionCount() {
        subscriptionCount++;

        Integer milestone = getMilestone(subscriptionCount, milestones);

        if (milestone == null) return;

        logger.info(String.format("%s: Subscribed to %d of %d", exchange.exchange, milestone, this.coins.size()));
    }

    private CompletableFuture<Void> sendRequest(String coin, HttpRequest request, BiConsumer<String, HttpResponse<String>> processResponse) {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenAcceptAsync(response -> processResponse.accept(coin, response));
    }

    private void fetchOrderbooks() {
        Map<String, HttpRequest> requests = getRequests();

        List<CompletableFuture<Void>> futures = requests.entrySet()
            .stream()
            .map(entry -> semaphoreManager.request(() -> sendRequest(entry.getKey(),
                entry.getValue(),
                this::processResponse
            )))
            .toList();

        CompletableFuture.allOf(new CompletableFuture[0])
            .join();
    }
}
