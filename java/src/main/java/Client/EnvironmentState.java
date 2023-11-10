package Client;

import Protobuf.Environment;

/**
 * This class represents the state of the environment, including field
 * dimensions, ball information, and robot states.
 */
public class EnvironmentState {
    /**
     * Inner class representing the field dimensions.
     */
    public class Field {
        public SharedObject<Double> length, width, goalDepth, goalWidth;

        /**
         * Initializes the field dimensions with default values.
         */
        public Field() {
            length = new SharedObject<Double>(0.0);
            width = new SharedObject<Double>(0.0);
            goalDepth = new SharedObject<Double>(0.0);
            goalWidth = new SharedObject<Double>(0.0);
        }

        /**
         * Sets the field dimensions based on the provided Protobuf field data.
         *
         * @param field The Protobuf field data.
         */
        public void Set(Protobuf.Field field) {
            if (field == null) {
                System.out.println("field is null");
                return;
            }
            length.Set(1000 * field.getLength());
            width.Set(1000 * field.getWidth());
            goalDepth.Set(1000 * field.getGoalDepth());
            goalWidth.Set(1000 * field.getGoalWidth());
        }
    }

    /**
     * Inner class representing the ball state.
     */
    public class Ball {
        public SharedObject<Double> x, y, vX, vY;

        /**
         * Initializes the ball state with default values.
         */
        public Ball() {
            x = new SharedObject<Double>(0.0);
            y = new SharedObject<Double>(0.0);
            vX = new SharedObject<Double>(0.0);
            vY = new SharedObject<Double>(0.0);
        }

        /**
         * Sets the ball state based on the provided Protobuf ball data.
         *
         * @param ball The Protobuf ball data.
         */
        public void Set(Protobuf.Ball ball, double fieldLenght) {
            if (ball == null) {
                System.out.println("ball is null");
                return;
            }
            double xBall = ball.getX();
            if (xBall > fieldLenght / 2) {
                goalsBlue.Set(goalsBlue.Get() + 1);
            } else if (xBall < -fieldLenght / 2) {
                goalsYellow.Set(goalsYellow.Get() + 1);
            }
            x.Set(1000 * xBall);
            y.Set(1000 * ball.getY());
            vX.Set(ball.getVx());
            vY.Set(ball.getVy());
        }
    }

    /**
     * Inner class representing the robot state.
     */
    public class Robot {
        public SharedObject<Double> x, y, angle, vX, vY, vAngle;
        public SharedObject<Integer> id;

        /**
         * Initializes the robot state with default values.
         */
        public Robot() {
            x = new SharedObject<Double>(0.0);
            y = new SharedObject<Double>(0.0);
            angle = new SharedObject<Double>(0.0);
            vX = new SharedObject<Double>(0.0);
            vY = new SharedObject<Double>(0.0);
            vAngle = new SharedObject<Double>(0.0);
            id = new SharedObject<Integer>(0);
        }

        /**
         * Sets the robot state based on the provided Protobuf robot data.
         *
         * @param robot The Protobuf robot data.
         */
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
    public Environment lastEnv;
    public SharedObject<Integer> goalsBlue, goalsYellow;

    /**
     * Initializes a new instance of the EnvironmentState class with field, ball,
     * and robot states.
     */
    public EnvironmentState() {
        field = new Field();
        ball = new Ball();
        blueRobots = new Robot[3];
        yellowRobots = new Robot[3];
        for (int i = 0; i < 3; i++) {
            blueRobots[i] = new Robot();
            yellowRobots[i] = new Robot();
        }
        goalsBlue = new SharedObject<>(0);
        goalsYellow = new SharedObject<>(0);
        lastEnv = null;
    }

    /**
     * Prints the current state of the environment to the console.
     */
    public void printState() {
        try {
            // Print state information here
        } catch (Exception ex) {
            // Handle exceptions
        }
    }

    /**
     * Updates the state of the environment based on the provided Protobuf
     * environment data.
     *
     * @param env The Protobuf environment data.
     */
    public void updateState(Environment env) {
        lastEnv = env;
        if (env == null) {
            System.out.println("env is null");
            return;
        }
        field.Set(env.getField());
        ball.Set(env.getFrame().getBall(), env.getField().getLength());
        for (int i = 0; i < 3; i++) {
            try {
                blueRobots[i].Set(env.getFrame().getRobotsBlue(i));
            } catch (Exception ex) {
                // Handle exceptions
            }
            try {
                yellowRobots[i].Set(env.getFrame().getRobotsYellow(i));
            } catch (Exception ex) {
                // Handle exceptions
            }
        }
    }
}
