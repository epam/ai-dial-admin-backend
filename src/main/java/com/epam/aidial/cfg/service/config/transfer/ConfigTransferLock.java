package com.epam.aidial.cfg.service.config.transfer;

import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

@Component
public class ConfigTransferLock {

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public <T> T withReadLock(Supplier<T> supplier) {
        var lock = readWriteLock.readLock();
        try {
            lock.lock();
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

    public void withWriteLock(Runnable runnable) {
        var lock = readWriteLock.writeLock();
        try {
            lock.lock();
            runnable.run();
        } finally {
            lock.unlock();
        }
    }

}
