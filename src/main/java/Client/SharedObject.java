package Client;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SharedObject<T> {
    private T value;
    private Lock lock = new ReentrantLock();

    SharedObject(T value) {
        this.value = value;
    }

    public void Set(T value) {
        lock.lock();
        try {
            this.value = value;
        } finally {
            lock.unlock();
        }
    }

    public T Get() {
        lock.lock();
        try {
            return value;
        } finally {
            lock.unlock();
        }
    }
}
