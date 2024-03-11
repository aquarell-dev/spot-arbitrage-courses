package org.arbitrage.mexc;

import org.arbitrage.client.APIExchangeClient;
import org.arbitrage.coins.CoinsProcessor;
import org.arbitrage.exchange.Exchange;
import org.arbitrage.orderbook.Orderbook;
import org.arbitrage.semaphore.DelayedSemaphoreManager;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpResponse;

public class MexcClient extends APIExchangeClient {
    public MexcClient(CoinsProcessor processor) {
        super(Exchange.MEXC, processor, new DelayedSemaphoreManager<>(10, 1000));
    }

    @Override
    protected URI getOrderbookUriByCoin(String coin) {
        return getExchange().baseUri.resolve(String.format("depth?symbol=%s&limit=%d", coinToTicker(coin), 20));
    }

    @Override
    protected void processResponse(String coin, HttpResponse<String> response) {
        int status = response.statusCode();

        if (status != 200) {
            logger.error(String.format("%s: Response status is not 200, but %d", getExchange().exchange, status));
            return;
        }

        JSONObject body = new JSONObject(response.body());

        Orderbook orderbook = orderbooks.get(coin);

        if (orderbook == null) {
            Orderbook newOrderbook = new MexcOrderbook(processor.getCoin(coin, Exchange.MEXC));
            orderbook = newOrderbook;
            orderbooks.put(coin, newOrderbook);
        }

        if (!body.has("bids") || !body.has("asks")) return;

        orderbook.updateBids(body.getJSONArray("bids"));
        orderbook.updateAsks(body.getJSONArray("asks"));
    }

    @Override
    public String tickerToCoin(String ticker) {
        return ticker.replace("USDT", "");
    }

    @Override
    public String coinToTicker(String coin) {
        return coin + "USDT";
    }
}
