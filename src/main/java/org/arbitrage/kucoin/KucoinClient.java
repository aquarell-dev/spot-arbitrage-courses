package org.arbitrage.kucoin;

public class KucoinClient {
//    private long c = 0;
//
//    public KucoinClient(CoinsProcessor processor) {
//        super(Exchange.KUCOIN, processor, URI.create(
//                String.format("%s/?token=%s&[connectId=%s]", Exchange.KUCOIN.baseConnectionUri, obtainConnectionToken(),
//                        "hQvf8jkno")));
//    }
//
//    @Override
//    public void onMessage(String s) {
//        JSONObject message = new JSONObject(s);
//
//        String messageType = message.getString("type");
//
//        if (messageType.equals("welcome")) {
//            logger.info("KuCoin: Authorization successful");
//            return;
//        }
//
//        if (messageType.equals("pong")) return;
//
//        if (messageType.equals("ack")) {
//            String id = message.getString("id");
//
//            String[] tickers = id.split(",");
//
//            for (String ticker : tickers) {
//                String coin = tickerToCoin(ticker);
//                c++;
//                logger.info("KuCoin: Subscribed to " + coin + "(" + c + "/" + coins.size() + ")");
//            }
//        }
//
//        if (messageType.equals("error")) {
//            String id = message.getString("id");
//
//            String[] tickers = id.split(",");
//
//            Arrays.stream(tickers)
//                  .forEach(ticker -> logger.error("KuCoin: Error subscribing to " + tickerToCoin(ticker)));
//        }
//
////        System.out.println(message);
//    }
//
//    @Override
//    protected String createSubscriptionMessage(List<String> coins) {
//        JSONObject message = new JSONObject();
//
//        String topic = coins.stream().map(this::coinToTicker).collect(Collectors.joining(","));
//
//        message.put("id", topic);
//        message.put("type", "subscribe");
//        message.put("topic", String.format("/spotMarket/level2Depth50:%s", topic));
//        message.put("response", true);
//
//        return message.toString();
//    }
//
//    @Override
//    protected String getPingMessage() {
//        JSONObject ping = new JSONObject();
//
//        ping.put("id", System.currentTimeMillis());
//        ping.put("type", "ping");
//
//        return ping.toString();
//    }
//
//    @Override
//    public String coinToTicker(String coin) {
//        return coin + "-USDT";
//    }
//
//    @Override
//    public String tickerToCoin(String ticker) {
//        return ticker.replace("-USDT", "");
//    }
}
