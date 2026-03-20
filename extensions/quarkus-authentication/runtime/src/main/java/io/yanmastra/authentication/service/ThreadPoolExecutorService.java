package io.yanmastra.authentication.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolExecutorService{
    private final ExecutorService VIRTUAL_THREAD_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    private final ExecutorService PLATFORM_THREAD_EXECUTOR = Executors.newFixedThreadPool(
            10,
            Thread.ofPlatform().factory()
    );

    public void execVirtually(Runnable task) {
        VIRTUAL_THREAD_EXECUTOR.execute(task);
    }

    public void execPlatform(Runnable task) {
        PLATFORM_THREAD_EXECUTOR.execute(task);
    }
}
