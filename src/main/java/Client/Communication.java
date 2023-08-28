package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.ArrayList;

import Protobuf.BallReplacement;
import Protobuf.Commands;
import Protobuf.Environment;
import Protobuf.Packet;
import Protobuf.Replacement;
import Protobuf.Robot;
import Protobuf.RobotReplacement;

public class Communication {
    public SharedObject<Boolean> rx;
    public SharedObject<byte[]> message;
    public SharedObject<Integer> messageLength;
    public SharedObject<Environment> environment;
    public SharedObject<ArrayList<Protobuf.Command>> commands;
    public SharedObject<byte[]> packet;
    public SharedObject<Integer> packetLength;
    public SharedObject<Boolean> replace;

    Communication() {
        rx = new SharedObject<>(false);
        message = new SharedObject<>(new byte[2048]);
        messageLength = new SharedObject<>(0);
        environment = new SharedObject<>(null);
        commands = new SharedObject<>(null);
        packet = new SharedObject<>(new byte[2048]);
        packetLength = new SharedObject<>(0);
        replace = new SharedObject<>(false);
    }

    public boolean getRx() {
        return rx.Get();
    }

    private void receiveFrame() {
        try {
            byte[] receiveData = new byte[2048];
            InetAddress multicastAddress = InetAddress.getByName("224.0.0.1");
            int port = 10002;

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
                        rx.Set(true); // Set the flag to true if data is received
                        message.Set(receivePacket.getData());
                        messageLength.Set(receivePacket.getLength());
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
                    rx.Set(true);

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

    private void decodeMessage() {
        while (true) {
            try {
                byte[] message = this.message.Get();
                int length = messageLength.Get();
                if (length == 0)
                    continue;
                byte[] actualMessage = new byte[length];
                System.arraycopy(message, 0, actualMessage, 0, length);
                Environment env = Environment.parseFrom(actualMessage);
                if (env != null)
                    environment.Set(env);
                else {
                    System.out.println("env is null");
                }
            } catch (Exception ex) {
                // System.out.println(ex);
            }
        }
    }

    private void printReceivedFrame() {

        while (true) {
            try {
                Thread.sleep(80);
            } catch (Exception e) {
            }
            Environment env = environment.Get();
            if (rx.Get() && env != null) {
                // System.out.println("rx");
                // Main.clearTerminal();
                // System.out.println("Field:");
                // System.out.println(
                // "Length: " + env.getField().getLength() +
                // "; Width: " + env.getField().getWidth());
                // System.out.println(
                // "GoalDepth: " + env.getField().getGoalDepth() +
                // "; GoalWidth: " + env.getField().getGoalWidth());
                // System.out.println("Ball:");
                // double vx = Main.round(env.getFrame().getBall().getVx(), 4),
                // vy = Main.round(env.getFrame().getBall().getVx(), 4), vnorm = Math.sqrt(vx *
                // vx + vy * vy);
                // System.out.println(
                // "x: " + Main.round(env.getFrame().getBall().getX(), 4) +
                // "; y: " + Main.round(env.getFrame().getBall().getY(), 4) +
                // "; vX: " + vx +
                // "; vY: " + vy +
                // "; |v|: " + vnorm);
                // System.out.println("Blue Robots:");
                // for (Protobuf.Robot robot : env.getFrame().getRobotsBlueList()) {
                // System.out.println(
                // "id: " + robot.getRobotId() +
                // "; x: " + Main.round(robot.getX(), 4) +
                // "; y: " + Main.round(robot.getY(), 4) +
                // "; vx: " + Main.round(robot.getVx(), 4) +
                // "; vy: " + Main.round(robot.getVy(), 4));
                // }
                // System.out.println("Yellow Robots:");
                // for (Protobuf.Robot robot : env.getFrame().getRobotsYellowList()) {
                // System.out.println(
                // "id: " + robot.getRobotId() +
                // "; x: " + Main.round(robot.getX(), 4) +
                // "; y: " + Main.round(robot.getY(), 4) +
                // "; vx: " + Main.round(robot.getVx(), 4) +
                // "; vy: " + Main.round(robot.getVy(), 4));
                // }
            }
        }
    }

    private void sendPackage() {
        String ipAddress = Main.parameters.get("address").asText();
        int port = 20011;
        while (true) {
            try {
                Thread.sleep(14);
            } catch (Exception ex) {
            }
            byte[] packetArray = this.packet.Get();
            if (packetArray.length == 0)
                continue;
            // for (int i = 0; i < packetArray.length; i++) {
            // System.out.print(String.format("%02x", packetArray[i]));
            // }
            // System.out.print('\n');
            try (DatagramSocket socket = new DatagramSocket()) {
                InetAddress address = InetAddress.getByName(ipAddress);
                DatagramPacket packet = new DatagramPacket(packetArray, packetArray.length, address, port);
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Replacement DefaultReplacement() {
        return Replacement.newBuilder()
                .setBall(BallReplacement.newBuilder().setX(-0.375).setY(0).build())
                .addRobots(RobotReplacement.newBuilder()
                        .setPosition(Robot.newBuilder().setX(-0.6).setY(0).setRobotId(0).setOrientation(0)
                                .build())
                        .setYellowteam(false).setTurnon(true).build())
                .addRobots(RobotReplacement.newBuilder()
                        .setPosition(Robot.newBuilder().setX(-0.6).setY(0.9).setRobotId(1).setOrientation(-90)
                                .build())
                        .setYellowteam(false).setTurnon(false).build())
                .addRobots(RobotReplacement.newBuilder()
                        .setPosition(Robot.newBuilder().setX(-0.3).setY(0.9).setRobotId(2).setOrientation(-90)
                                .build())
                        .setYellowteam(false).setTurnon(false).build())
                .addRobots(RobotReplacement.newBuilder()
                        .setPosition(
                                Robot.newBuilder().setX(0.7).setY(0).setRobotId(0).setOrientation(180).build())
                        .setYellowteam(true).setTurnon(true).build())
                .addRobots(RobotReplacement.newBuilder()
                        .setPosition(Robot.newBuilder().setX(0.3).setY(0.9).setRobotId(1).setOrientation(-90)
                                .build())
                        .setYellowteam(true).setTurnon(false).build())
                .addRobots(RobotReplacement.newBuilder()
                        .setPosition(Robot.newBuilder().setX(0.6).setY(0.9).setRobotId(2).setOrientation(-90)
                                .build())
                        .setYellowteam(true).setTurnon(false).build())
                .build();
    }

    private void encodeMessage() {
        while (true) {
            try {
                Commands.Builder commandsBuilder = Commands.newBuilder();
                ArrayList<Protobuf.Command> commandsArray = this.commands.Get();
                if (commandsArray == null)
                    continue;
                // System.out.println(commandsArray.get(0).getWheelLeft());
                for (Protobuf.Command command : commandsArray) {
                    commandsBuilder.addRobotCommands(command);
                }
                Commands commands = commandsBuilder.build();
                Packet.Builder packetBuilder = Packet.newBuilder();
                packetBuilder = packetBuilder.setCmd(commands);
                if (replace.Get()) {
                    // System.out.println("replace");
                    packetBuilder = packetBuilder.setReplace(DefaultReplacement());
                }
                Packet packet = packetBuilder.build();
                byte[] serialized = packet.toByteArray();
                this.packet.Set(serialized);
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    public void startReceiving() {
        Thread receiverThread = new Thread(this::receiveFrame);
        Thread decoderThread = new Thread(this::decodeMessage);
        Thread printerThread = new Thread(this::printReceivedFrame);

        receiverThread.start();
        decoderThread.start();
        printerThread.start();

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Interrupt and join the threads
        receiverThread.interrupt();
        decoderThread.interrupt();
        printerThread.interrupt();
        try {
            receiverThread.join();
            decoderThread.join();
            printerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startSending() {
        Thread senderThread = new Thread(this::sendPackage);
        Thread encoderThread = new Thread(this::encodeMessage);

        senderThread.start();
        encoderThread.start();

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Interrupt and join the threads
        senderThread.interrupt();
        encoderThread.interrupt();
        try {
            senderThread.join();
            encoderThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
