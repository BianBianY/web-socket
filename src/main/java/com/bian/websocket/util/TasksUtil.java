package com.bian.websocket.util;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Component
public class TasksUtil {
    @Async
    public Future<?> doTask(Supplier<?> task) {
        return new AsyncResult<>(task.get());
    }

    @Async
    public Future<Boolean> doTask(BooleanSupplier task) {
        return new AsyncResult<>(task.getAsBoolean());
    }

    @Async
    public void doTask(Runnable task) {
        task.run();
    }
}
