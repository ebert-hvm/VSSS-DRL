package Client;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A generic shared object that provides thread-safe read and write operations
 * on a shared value using a ReentrantLock.
 *
 * @param <T> The type of the shared value.
 */
public class SharedObject<T> {
    private T value;
    private Lock lock = new ReentrantLock();

    /**
     * Initializes a new instance of the SharedObject with the specified initial
     * value.
     *
     * @param value The initial value of the shared object.
     */
    SharedObject(T value) {
        this.value = value;
    }

    /**
     * Sets the shared value to the specified value, acquiring the lock for thread
     * safety.
     *
     * @param value The new value to set.
     */
    public void Set(T value) {
        lock.lock();
        try {
            this.value = value;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves the current shared value, acquiring the lock for thread safety.
     *
     * @return The current shared value.
     */
    public T Get() {
        lock.lock();
        try {
            return value;
        } finally {
            lock.unlock();
        }
    }
}
