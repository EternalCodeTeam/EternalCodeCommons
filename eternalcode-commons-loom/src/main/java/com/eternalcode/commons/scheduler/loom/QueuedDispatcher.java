package com.eternalcode.commons.scheduler.loom;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test dispatcher - queues tasks for manual execution.
 */
public final class QueuedDispatcher implements MainThreadDispatcher {

    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger dispatchCount = new AtomicInteger(0);
    private final AtomicInteger executeCount = new AtomicInteger(0);
    private volatile Thread mainThread = null;
    private volatile boolean executeImmediately = false;

    public void setExecuteImmediatelyOnMainThread(boolean immediate) {
        this.executeImmediately = immediate;
    }

    @Override
    public void dispatch(Runnable task) {
        this.dispatchCount.incrementAndGet();
        if (this.executeImmediately && isMainThread()) {
            executeTask(task);
            return;
        }
        this.queue.offer(task);
    }

    @Override
    public boolean isMainThread() {
        return this.mainThread != null && Thread.currentThread() == this.mainThread;
    }

    public void setMainThread(Thread thread) {
        this.mainThread = thread;
    }

    @Override
    public void dispatchLater(Runnable task, long ticks) {
        dispatch(task);
    }

    @Override
    public Cancellable dispatchTimer(Runnable task, long delay, long period) {
        dispatch(task);
        return () -> {
        };
    }

    public int runPending() {
        int count = 0;
        Runnable task;
        while ((task = this.queue.poll()) != null) {
            executeTask(task);
            count++;
        }
        return count;
    }

    public boolean runOne() {
        Runnable task = this.queue.poll();
        if (task != null) {
            executeTask(task);
            return true;
        }
        return false;
    }

    public int runUpTo(int max) {
        int count = 0;
        while (count < max) {
            Runnable task = this.queue.poll();
            if (task == null) {
                break;
            }
            executeTask(task);
            count++;
        }
        return count;
    }

    private void executeTask(Runnable task) {
        this.executeCount.incrementAndGet();
        try {
            task.run();
        }
        catch (Throwable t) {
            throw new RuntimeException("Exception in queued task", t);
        }
    }

    public int getPendingCount() {
        return this.queue.size();
    }

    public int getDispatchCount() {
        return this.dispatchCount.get();
    }

    public int getExecuteCount() {
        return this.executeCount.get();
    }

    public boolean hasPending() {
        return !this.queue.isEmpty();
    }

    public int clear() {
        int count = 0;
        while (this.queue.poll() != null) {
            count++;
        }
        return count;
    }

    public void reset() {
        clear();
        this.dispatchCount.set(0);
        this.executeCount.set(0);
    }
}
