package Client;

import Protobuf.Environment;

public class EnvironmentState {
    public class Field {
        public SharedObject<Double> lenght, width, goalDepth, goalWidth;

        public Field() {
            lenght = new SharedObject<Double>(0.0);
            width = new SharedObject<Double>(0.0);
            goalDepth = new SharedObject<Double>(0.0);
            goalWidth = new SharedObject<Double>(0.0);
        }

        public void Set(Protobuf.Field field) {
            if (field == null) {
                System.out.println("field is null");
                return;
            }
            lenght.Set(1000 * field.getLength());
            width.Set(1000 * field.getWidth());
            goalDepth.Set(1000 * field.getGoalDepth());
            goalWidth.Set(1000 * field.getGoalWidth());
        }
    }

    public class Ball {
        public SharedObject<Double> x, y, vX, vY;

        public Ball() {
            x = new SharedObject<Double>(0.0);
            y = new SharedObject<Double>(0.0);
            vX = new SharedObject<Double>(0.0);
            vY = new SharedObject<Double>(0.0);
        }

        public void Set(Protobuf.Ball ball) {
            if (ball == null) {
                System.out.println("ball is null");
                return;
            }
            x.Set(1000 * ball.getX());
            y.Set(1000 * ball.getY());
            vX.Set(ball.getVx());
            vY.Set(ball.getVy());
        }
    }

    public class Robot {
        public SharedObject<Double> x, y, angle, vX, vY, vAngle;
        public SharedObject<Integer> id;

        public Robot() {
            x = new SharedObject<Double>(0.0);
            y = new SharedObject<Double>(0.0);
            angle = new SharedObject<Double>(0.0);
            vX = new SharedObject<Double>(0.0);
            vY = new SharedObject<Double>(0.0);
            vAngle = new SharedObject<Double>(0.0);
            id = new SharedObject<Integer>(0);
        }

        public void Set(Protobuf.Robot robot) {
            if (robot == null) {
                System.out.println("robot is null");
                return;
            }
            x.Set(1000 * robot.getX());
            y.Set(1000 * robot.getY());
            angle.Set(robot.getOrientation());
            vX.Set(robot.getVx());
            vY.Set(robot.getVy());
            vAngle.Set(robot.getVorientation());
            id.Set(robot.getRobotId());
        }
    }

    public Field field;
    public Ball ball;
    public Robot[] blueRobots, yellowRobots;

    public EnvironmentState() {
        field = new Field();
        ball = new Ball();
        blueRobots = new Robot[3];
        yellowRobots = new Robot[3];
        for (int i = 0; i < 3; i++) {
            blueRobots[i] = new Robot();
            yellowRobots[i] = new Robot();
        }
    }

    public void printState() {
        try {
            System.out.println("Field:");
            System.out.println(
                    "Length: " + field.lenght.Get() +
                            "; Width: " + field.width.Get());
            System.out.println(
                    "GoalDepth: " + field.goalDepth.Get() +
                            "; GoalWidth: " + field.width.Get());
            System.out.println("Ball:");
            double vx = Main.round(ball.vX.Get(), 4),
                    vy = Main.round(ball.vY.Get(), 4), vnorm = Math.sqrt(vx * vx + vy * vy);
            System.out.println(
                    "x: " + Main.round(ball.x.Get(), 4) +
                            "; y: " + Main.round(ball.y.Get(), 4) +
                            "; vX: " + vx +
                            "; vY: " + vy +
                            "; |v|: " + vnorm);
            System.out.println("Blue Robots:");
            for (Robot robot : blueRobots) {
                System.out.println(
                        "id: " + robot.id.Get() +
                                "; x: " + Main.round(robot.x.Get(), 4) +
                                "; y: " + Main.round(robot.y.Get(), 4) +
                                "; vx: " + Main.round(robot.vX.Get(), 4) +
                                "; vy: " + Main.round(robot.vY.Get(), 4));
            }
            System.out.println("Yellow Robots:");
            for (Robot robot : yellowRobots) {
                System.out.println(
                        "id: " + robot.id.Get() +
                                "; x: " + Main.round(robot.x.Get(), 4) +
                                "; y: " + Main.round(robot.y.Get(), 4) +
                                "; vx: " + Main.round(robot.vX.Get(), 4) +
                                "; vy: " + Main.round(robot.vY.Get(), 4));
            }
        } catch (Exception ex) {

        }
    }

    public void updateState(Environment env) {
        if (env == null) {
            System.out.println("env is null");
            return;
        }
        field.Set(env.getField());
        ball.Set(env.getFrame().getBall());
        for (int i = 0; i < 3; i++) {
            try {
                blueRobots[i].Set(env.getFrame().getRobotsBlue(i));
            } catch (Exception ex) {
            }
            try {
                yellowRobots[i].Set(env.getFrame().getRobotsYellow(i));
            } catch (Exception ex) {
            }
        }
    }
}