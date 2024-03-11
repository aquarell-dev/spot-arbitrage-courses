package org.arbitrage.sockets;

import org.arbitrage.exchange.Exchange;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;

public class CustomSocketClient extends WebSocketClient {
    private final Exchange exchange;
    private final Consumer<String> onMessage;
    private final Supplier<String> getPingMessage;
    private final Runnable reconnect;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final Logger logger = LoggerFactory.getLogger(CustomSocketClient.class);

    public CustomSocketClient(Exchange exchange, Consumer<String> onMessage, Supplier<String> getPingMessage, Runnable reconnect) {
        super(exchange.baseUri);
        this.exchange = exchange;
        this.onMessage = onMessage;
        this.getPingMessage = getPingMessage;
        this.reconnect = reconnect;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        logger.info(String.format("%s: Connection established", exchange.exchange));
        scheduledExecutorService.scheduleAtFixedRate(this::sendPing, 5, 30, TimeUnit.SECONDS);
    }

    @Override
    public void onMessage(String s) {
        onMessage.accept(s);
    }

    @Override
    public void onMessage(ByteBuffer buffer) {
        if (!buffer.hasArray()) return;

        byte[] decompressedData = decompress(buffer.array());

        if (decompressedData == null) return;

        String stringMessage = new String(decompressedData, StandardCharsets.UTF_8);

        this.onMessage(cleanupString(stringMessage));
    }

    @Override
    public void sendPing() {
        this.send(getPingMessage.get());
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        logger.info(String.format("%s: Connection stopped. Due to %s. %d", exchange.exchange, s, i));
        reconnect.run();
    }

    @Override
    public void onError(Exception e) {
        String message = e.getMessage();
        logger.error(String.format("%s: Error - %s", exchange.exchange, message == null ? "Unknown error" : message));
    }

    private byte[] decompress(byte[] compressedData) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(compressedData); GZIPInputStream gzis = new GZIPInputStream(
                bais); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            // Read decompressed data in a loop until the end of the stream
            while ((bytesRead = gzis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String cleanupString(String input) {
        // Remove non-printable characters
        return input.replaceAll("[^\\x20-\\x7e]", "");
    }
}
