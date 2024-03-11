package org.arbitratge;

import org.arbitrage.exchange.Exchange;
import org.arbitrage.orderbook.NotEnoughVolume;
import org.arbitrage.orderbook.Orderbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.TreeMap;


public class OrderbookTest {
    @ParameterizedTest
    @EnumSource(Exchange.class)
    void testUpdateOrderbook(Exchange exchange) {
        Orderbook orderbook = TestUtilities.getOrderBookInstance(exchange);

        orderbook.updateBids(TestUtilities.createSampleQuotes(TestUtilities.sampleBids));
        orderbook.updateAsks(TestUtilities.createSampleQuotes(TestUtilities.sampleAsks));

        Assertions.assertEquals(TestUtilities.sampleBids, orderbook.getBids());
        Assertions.assertEquals(TestUtilities.sampleAsks, orderbook.getAsks());
    }

    @ParameterizedTest
    @EnumSource(Exchange.class)
    void testGetBestAskAndBestBid(Exchange exchange) {
        Orderbook orderbook = TestUtilities.getOrderBookInstance(exchange);

        TestUtilities.updateOrderbook(orderbook);

        Assertions.assertEquals(100, orderbook.getBestAsk());
        Assertions.assertEquals(90, orderbook.getBestBid());
    }

    @ParameterizedTest
    @EnumSource(Exchange.class)
    void testGetAsksAndBids(Exchange exchange) {
        Orderbook orderbook = TestUtilities.getOrderBookInstance(exchange);

        TestUtilities.updateOrderbook(orderbook);

        TreeMap<Double, Double> expectedBids = new TreeMap<>() {{
            put(90d, 100d);
            put(80d, 100d);
        }};

        TreeMap<Double, Double> expectedAsks = new TreeMap<>() {{
            put(110d, 100d);
            put(100d, 100d);
        }};

        Assertions.assertEquals(expectedBids, orderbook.getBids(2));
        Assertions.assertEquals(expectedAsks, orderbook.getAsks(2));
    }

    @ParameterizedTest
    @EnumSource(Exchange.class)
    void testGetMeanPrice(Exchange exchange) {
        Orderbook orderbook = TestUtilities.getOrderBookInstance(exchange);

        TestUtilities.updateOrderbook(orderbook);

        Assertions.assertEquals(95, orderbook.getMeanPrice());
    }

    @ParameterizedTest
    @EnumSource(Exchange.class)
    void testCalculateAveragePrice(Exchange exchange) throws NotEnoughVolume {
        Orderbook orderbook = TestUtilities.getOrderBookInstance(exchange);

        TestUtilities.updateOrderbook(orderbook);

        Assertions.assertEquals(100, orderbook.calculateBuyAveragePrice(100));
        Assertions.assertEquals(105, orderbook.calculateBuyAveragePrice(200));
        Assertions.assertEquals(108, orderbook.calculateBuyAveragePrice(250));
        Assertions.assertEquals(104.25, orderbook.calculateBuyAveragePrice(174), 0.003);
        Assertions.assertThrows(NotEnoughVolume.class, () -> orderbook.calculateBuyAveragePrice(251));

        Assertions.assertEquals(90, orderbook.calculateSellAveragePrice(100));
        Assertions.assertEquals(85, orderbook.calculateSellAveragePrice(200));
        Assertions.assertEquals(82, orderbook.calculateSellAveragePrice(250));
        Assertions.assertEquals(86.25, orderbook.calculateSellAveragePrice(160), 0.003);
        Assertions.assertThrows(NotEnoughVolume.class, () -> orderbook.calculateSellAveragePrice(251));
    }
}
