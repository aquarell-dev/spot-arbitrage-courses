package org.arbitrage.mexc;

import org.arbitrage.client.Client;
import org.arbitrage.coins.CoinsProcessor;
import org.arbitrage.common.Batch;
import org.arbitrage.exchange.Exchange;
import org.arbitrage.orderbook.Orderbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MexcWSClient implements Client {
    protected final Logger logger = LoggerFactory.getLogger(MexcWSClient.class);
    private final ExecutorService executorService;
    private final CoinsProcessor processor;
    private final List<String> coins;
    private final Exchange exchange;
    private final Map<String, Orderbook> orderbooks = new HashMap<>();

    public MexcWSClient(CoinsProcessor processor) {
        this.processor = processor;
        this.exchange = Exchange.MEXC;
        this.coins = processor.getCoins(Exchange.MEXC);
        this.executorService = Executors.newFixedThreadPool(coins.size());
    }

    @Override
    public void run() {
        logger.info("Mexc Global: Connection Established");
        List<List<String>> batches = Batch.splitIntoBatches(coins, 30);
        batches.forEach(batch -> executorService.execute(new MexcBatch(processor, batch)));
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

    @Override
    public String tickerToCoin(String ticker) {
        return null;
    }

    @Override
    public String coinToTicker(String coin) {
        return null;
    }
}
