package org.arbitrage.semaphore;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

public class DelayedSemaphoreManager<T> implements SemaphoreManager<T> {
    private final Semaphore semaphore;
    private final int requestCountPerTime;
    private final int delay;
    private int count = 0;

    public DelayedSemaphoreManager(int requestCountPerTime, int delay) {
        this.semaphore = new Semaphore(requestCountPerTime);
        this.requestCountPerTime = requestCountPerTime;
        this.delay = delay;
    }

    @Override
    public CompletableFuture<T> request(Supplier<CompletableFuture<T>> processResponse) {
        CompletableFuture<T> future = new CompletableFuture<>();

        try {
            semaphore.acquire();

            count++;

            if (count % requestCountPerTime == 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread()
                        .interrupt();
                }
            }

            processResponse.get()
                .whenComplete((result, exception) -> {
                    semaphore.release();

                    if (exception != null) {
                        future.completeExceptionally(exception);
                    } else {
                        future.complete(result);
                    }
                });

        } catch (InterruptedException e) {
            Thread.currentThread()
                .interrupt();
            future.completeExceptionally(e);
        }

        return future;
    }
}
