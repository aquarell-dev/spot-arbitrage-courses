package org.arbitrage.manager;

import org.arbitrage.arbitrage.Arbitrage;
import org.arbitrage.bitget.BitgetClient;
import org.arbitrage.client.Client;
import org.arbitrage.coins.CoinsProcessor;
import org.arbitrage.common.Pair;
import org.arbitrage.exchange.Exchange;
import org.arbitrage.gate.GateClient;
import org.arbitrage.htx.HTXClient;
import org.arbitrage.mexc.MexcClient;
import org.arbitrage.mexc.MexcWSClient;
import org.arbitrage.orderbook.Orderbook;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ExchangeManager implements Runnable {
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private final Set<Client> clients;
    private final Map<String, Set<Arbitrage>> spreads = new HashMap<>();
    private final Set<String> coins = new HashSet<>();

    public ExchangeManager(CoinsProcessor processor, Exchange... exchanges) {
        this.clients = Arrays.stream(exchanges)
                .map(exchange -> getClient(processor, exchange))
                .collect(Collectors.toSet());
        initialize();
        executorService.scheduleAtFixedRate(this::update, 5, 3, TimeUnit.SECONDS);
    }

    public Map<String, Set<Arbitrage>> getSpreads() {
        return spreads;
    }

    @Override
    public void run() {
        clients.stream().map(Thread::new).forEach(Thread::start);
    }

    private synchronized void update() {
        for (String coin : coins) {
            List<Orderbook> orderbooks = clients.stream()
                    .map(client -> client.getOrderbooks().get(coin))
                    .toList();

            List<List<Orderbook>> pairs = Pair.generatePairs(orderbooks);

            Set<Arbitrage> newArbitrages = pairs.stream()
                    .map(pair -> new Arbitrage(coin, pair))
                    .collect(Collectors.toSet());

            spreads.put(coin, newArbitrages);
        }
    }

    private synchronized void initialize() {
        clients.forEach(client -> coins.addAll(client.getCoins()));
    }

    private Client getClient(CoinsProcessor processor, Exchange exchange) {
        return switch (exchange) {
            case GATE -> new GateClient(processor);
            case BITGET -> new BitgetClient(processor);
            case HTX -> new HTXClient(processor);
            case MEXC -> new MexcClient(processor);
            default -> throw new IllegalArgumentException();
        };
    }
}
