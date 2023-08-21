package Client;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.InvalidProtocolBufferException;

import Protobuf.Ball;
import Protobuf.Environment;

import java.net.InetSocketAddress;

public class MulticastReceiver {
    private Vision vision;

    MulticastReceiver() {
        vision = new Vision();
    }

    public void receiveFrame() {
        try {
            byte[] receiveData = new byte[2048];
            InetAddress multicastAddress = InetAddress.getByName("224.5.23.2");
            int port = 10020;

            MulticastSocket socket = new MulticastSocket(new InetSocketAddress(port));

            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isLoopback() && networkInterface.isUp()) {
                    socket.joinGroup(new InetSocketAddress(multicastAddress, port), networkInterface);
                }
            }

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                long startTime = System.currentTimeMillis(); // Get the current time
                boolean connected = true; // Assume we are initially connected

                // Try to receive data for 500ms or until connection is lost
                while (System.currentTimeMillis() - startTime < 500) {
                    try {
                        socket.setSoTimeout(500); // Set the socket timeout
                        socket.receive(receivePacket);
                        vision.rx.Set(true); // Set the flag to true if data is received
                        vision.message.Set(receivePacket.getData());
                        vision.messageLenght.Set(receivePacket.getLength());
                        break; // Exit the loop if data is received
                    } catch (SocketTimeoutException ste) {
                        // Timeout occurred, no data received, continue waiting
                    } catch (java.io.IOException ioe) {
                        // Connection lost, need to reconnect
                        connected = false;
                        break;
                    }
                }

                if (!connected) {
                    // Connection lost, reset the flag and attempt to reconnect
                    vision.rx.Set(true);

                    // Leave the multicast group and rejoin with the new socket
                    NetworkInterface networkInterface = socket.getNetworkInterface();
                    if (networkInterface != null) {
                        socket.leaveGroup(new InetSocketAddress(multicastAddress, port), networkInterface);
                    }
                    socket.close();
                    socket = new MulticastSocket();
                    socket.setSoTimeout(500); // Set the socket timeout

                    Enumeration<NetworkInterface> newInterfaces = NetworkInterface.getNetworkInterfaces();
                    while (newInterfaces.hasMoreElements()) {
                        NetworkInterface newNetworkInterface = newInterfaces.nextElement();
                        if (!newNetworkInterface.isLoopback() && newNetworkInterface.isUp()) {
                            socket.joinGroup(new InetSocketAddress(multicastAddress, port), newNetworkInterface);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    // public void receiveFrame() {
    // try {
    // byte[] receiveData = new byte[2048];
    // InetAddress multicastAddress = InetAddress.getByName("224.0.0.1");
    // int port = 10002;
    // InetSocketAddress socketAddress = new InetSocketAddress(multicastAddress,
    // port);
    // MulticastSocket socket = new MulticastSocket(new InetSocketAddress(port));
    // boolean connected = false;
    // while (true) {
    // // Leave multicast group
    // if (!connected) {
    // try {
    // NetworkInterface networkInterface = socket.getNetworkInterface();
    // if (networkInterface != null) {
    // socket.leaveGroup(socketAddress, networkInterface);
    // }
    // socket.close();
    // socket = new MulticastSocket();
    // } catch (Exception ex) {
    // }
    // try {
    // Enumeration<NetworkInterface> interfaces =
    // NetworkInterface.getNetworkInterfaces();
    // while (interfaces.hasMoreElements()) {
    // NetworkInterface newNetworkInterface = interfaces.nextElement();
    // if (!newNetworkInterface.isLoopback() && newNetworkInterface.isUp()) {
    // socket.joinGroup(socketAddress, newNetworkInterface);
    // }
    // }
    // socket.setSoTimeout(500); // Set the socket timeout
    // connected = true;
    // } catch (Exception ex) {
    // continue;
    // }
    // }

    // DatagramPacket receivePacket = new DatagramPacket(receiveData,
    // receiveData.length);
    // vision.rx.Set(false);
    // try {
    // socket.receive(receivePacket);
    // vision.rx.Set(true); // Set the flag to true if data is received
    // vision.message.Set(receivePacket.getData());
    // vision.messageLenght.Set(receivePacket.getLength());
    // break; // Exit the loop if data is received
    // } catch (SocketTimeoutException ste) {
    // // Timeout occurred, no data received, continue waiting
    // } catch (java.io.IOException ioe) {
    // // Connection lost, need to reconnect
    // connected = false;
    // break;
    // }

    // }
    // } catch (Exception e) {
    // System.out.println(e);
    // }
    // }

    public void decodeMessage() {
        while (true) {
            try {
                byte[] message = vision.message.Get();
                int lenght = vision.messageLenght.Get();
                byte[] actualMessage = new byte[lenght];
                System.arraycopy(message, 0, actualMessage, 0, lenght);
                // Environment environment = Environment.parseFrom(actualMessage);
                // System.out.println("goal width: " + environment.getField().getGoalDepth());
            } catch (Exception e) {
                System.out.println("pqp");
            }
        }
    }

    public void printReceivedMessage() {
        while (true) {
            if (vision.rx.Get()) {
                int lenght = vision.messageLenght.Get();
                byte[] arr = vision.message.Get();
                for (int i = 0; i < lenght; i++) {
                    System.out.print(String.format("%02X", arr[i]));
                }
                // Main.clearTerminal();
                System.out.print("\033[H\033[2J");
                System.out.flush();
            } else {
                // System.out.println("Nothing received");
            }
        }
    }

    public void start() {
        Thread receiverThread = new Thread(this::receiveFrame);
        Thread printerThread = new Thread(this::printReceivedMessage);

        receiverThread.start();
        printerThread.start();

        // Allow the threads to run for a while
        try {
            Thread.sleep(60000); // Run for 1 minute
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Interrupt and join the threads
        receiverThread.interrupt();
        printerThread.interrupt();
        try {
            receiverThread.join();
            printerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
