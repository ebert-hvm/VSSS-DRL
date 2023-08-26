package Client;

import java.util.ArrayList;

import Protobuf.Command;

public class AI {
    private Communication communication;

    public AI(Communication communication) {
        this.communication = communication;
    }

    public void start() {
        long time = System.currentTimeMillis() - 5001;
        boolean replace;
        while (true) {
            try {
                replace = communication.replace.Get();
                if (replace && System.currentTimeMillis() - time > 500) {
                    communication.replace.Set(false);
                    time = System.currentTimeMillis();
                } else if (!replace && System.currentTimeMillis() - time > 5000) {
                    communication.replace.Set(true);
                    time = System.currentTimeMillis();
                }
                ArrayList<Protobuf.Command> commands = new ArrayList<Protobuf.Command>();
                Command.Builder command = Command.newBuilder().setId(0).setYellowteam(false).setWheelLeft(75)
                        .setWheelRight(75);
                commands.add(command.build());
                communication.commands.Set(commands);
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }
}
