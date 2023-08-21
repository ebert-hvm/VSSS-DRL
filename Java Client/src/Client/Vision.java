package Client;

public class Vision {
    public SharedObject<Boolean> rx;
    public SharedObject<byte[]> message;
    public SharedObject<Integer> messageLenght;

    Vision() {
        rx = new SharedObject<>(false);
        message = new SharedObject<>(new byte[2048]);
        messageLenght = new SharedObject<>(0);
    }
}
