package org.arbitrage.semaphore;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface SemaphoreManager<T> {
    CompletableFuture<T> request(Supplier<CompletableFuture<T>> processResponse);
}
