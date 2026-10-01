package io.yanmastra.quarkusBase.cache;

import io.vertx.core.Vertx;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class KeyValueCacheStoreAsyncTest {

    Vertx vertx;
    InMemoryKeyValueCacheStore store = new InMemoryKeyValueCacheStore();

    @BeforeEach
    void setUp() {
        vertx = Vertx.vertx();
        store.put("c", "k", "v");
    }

    @AfterEach
    void tearDown() {
        vertx.close().toCompletionStage().toCompletableFuture().join();
    }

    @Test
    void blockingWorkDoesNotRunOnTheEventLoop() throws Exception {
        AtomicReference<String> workThread = new AtomicReference<>();
        KeyValueCacheStore recording = new InMemoryKeyValueCacheStore() {
            @Override
            public String get(String cacheName, String key) {
                workThread.set(Thread.currentThread().getName());
                return "v";
            }
        };

        CompletableFuture<String> done = new CompletableFuture<>();
        vertx.getOrCreateContext().runOnContext(v ->
                recording.getAsync("c", "k").subscribe().with(done::complete, done::completeExceptionally));

        assertEquals("v", done.get(5, TimeUnit.SECONDS));
        assertFalse(workThread.get().contains("eventloop"), "blocking get ran on " + workThread.get());
    }

    @Test
    void resultIsDeliveredBackOnTheCallersEventLoop() throws Exception {
        CompletableFuture<String> resumedOn = new CompletableFuture<>();
        vertx.getOrCreateContext().runOnContext(v ->
                store.getAsync("c", "k").subscribe().with(
                        value -> resumedOn.complete(Thread.currentThread().getName()),
                        resumedOn::completeExceptionally));

        String thread = resumedOn.get(5, TimeUnit.SECONDS);
        assertTrue(thread.contains("eventloop"), "continuation ran on " + thread);
    }

    @Test
    void worksWithoutAnyVertxContext() {
        assertEquals("v", store.getAsync("c", "k").await().indefinitely());
        store.putAsync("c", "k2", "v2").await().indefinitely();
        assertEquals("v2", store.get("c", "k2"));
        store.removeAsync("c", "k2").await().indefinitely();
        assertNull(store.get("c", "k2"));
    }
}
