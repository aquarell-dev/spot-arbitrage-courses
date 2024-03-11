package org.arbitratge;

import org.arbitrage.exchange.Exchange;
import org.arbitrage.arbitrage.Arbitrage;
import org.arbitrage.orderbook.Orderbook;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.List;

public class ArbitrageTest {
    @Test
    public void testSpreadEquals() {
        Arbitrage arbitrage1 = createSampleSpread(Exchange.GATE, Exchange.BITGET);
        Arbitrage arbitrage2 = createSampleSpread(Exchange.GATE, Exchange.BITGET);
        Arbitrage arbitrage3 = createSampleSpread(Exchange.BITGET, Exchange.GATE);

        Assertions.assertEquals(arbitrage1, arbitrage2);
        Assertions.assertNotEquals(arbitrage2, arbitrage3);
    }

    @Test
    public void testTopOrderbookSpread() {
        Arbitrage arbitrage = createSampleSpread(Exchange.GATE, Exchange.BITGET);

        double topSpread = arbitrage.getTopOrderbookSpread();

        Assertions.assertEquals(-0.1, topSpread, 0.001);
    }

    @Test
    public void testVolumeSpread() {
        Arbitrage arbitrage = createSampleSpread(Exchange.GATE, Exchange.BITGET);

        double volumeSpread9000USDT = arbitrage.getSpread(9000);
        double volumeSpread10000USDT = arbitrage.getSpread(10000);
        double volumeSpread15000USDT = arbitrage.getSpread(15000);
        double volumeSpread23000USDT = arbitrage.getSpread(23000);
        double volumeSpread30000USDT = arbitrage.getSpread(30000);

        Assertions.assertEquals(-0.1, volumeSpread9000USDT, 0.0001);
        Assertions.assertEquals(-0.1, volumeSpread10000USDT, 0.0001);
        Assertions.assertEquals(-0.16, volumeSpread15000USDT, 0.01);
        Assertions.assertEquals(-0.223, volumeSpread23000USDT, 0.01);
        Assertions.assertEquals(-1, volumeSpread30000USDT, 0.0001);
    }

    @Test
    public void testToJsonVolume() {
        Arbitrage arbitrage = createSampleSpread(Exchange.GATE, Exchange.BITGET);

        JSONObject expectedJson = new JSONObject();

        expectedJson.put("coin", "BTC");
        expectedJson.put("origin", "Gate.io");
        expectedJson.put("destination", "Bitget");
        expectedJson.put("buy", 100d);
        expectedJson.put("sell", 90d);
        expectedJson.put("spread", -1);
        expectedJson.put("top_spread", -0.1);

        JSONObject json = arbitrage.toJSON();

        Assertions.assertEquals(expectedJson, json); // fucking floating-point math is broken again
    }

    private Arbitrage createSampleSpread(Exchange firstExchange, Exchange secondExchange) {
        Orderbook orderbook1 = TestUtilities.getOrderBookInstance(firstExchange);
        Orderbook orderbook2 = TestUtilities.getOrderBookInstance(secondExchange);

        TestUtilities.updateOrderbook(orderbook1);
        TestUtilities.updateOrderbook(orderbook2);

        return new Arbitrage("BTC", List.of(orderbook1, orderbook2));
    }
}
