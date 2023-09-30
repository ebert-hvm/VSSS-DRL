package Client;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * This class represents the navigation control for a robot using PID
 * controllers.
 */
public class Navigation {
    /**
     * Inner class representing a PID controller.
     */
    public class PIDController {
        private double kp, ki, kd, limit;
        private double error, integrativeError, derivativeError;

        /**
         * Initializes a new PID controller with the provided parameters.
         *
         * @param pidParameters A JSON node containing PID controller parameters.
         */
        public PIDController(JsonNode pidParameters) {
            kp = pidParameters.get("kp").asDouble();
            ki = pidParameters.get("ki").asDouble();
            kd = pidParameters.get("kd").asDouble();
            limit = pidParameters.get("limit").asDouble();
            reset();
        }

        /**
         * Resets the PID controller's internal state.
         */
        public void reset() {
            error = integrativeError = derivativeError = 0.0;
        }

        /**
         * Calculates the control output based on the given error.
         *
         * @param error The current error value.
         * @return The control output within the specified limit.
         */
        public double calculate(double error) {
            integrativeError += error;
            derivativeError = error - this.error;
            this.error = error;
            return Main.clamp(kp * error + ki * integrativeError + kd * derivativeError, -limit, limit);
        }
    }

    public static int framePeriod = 12;
    public PIDController positionPid, anglePid;

    /**
     * Initializes a new instance of the Navigation class with PID controllers.
     */
    public Navigation() {
        positionPid = new PIDController(Main.parameters.get("pid").get("position"));
        anglePid = new PIDController(Main.parameters.get("pid").get("angle"));
    }

    /**
     * Executes the navigation control based on the environment state and desired
     * destination.
     *
     * @param environmentState The current state of the environment.
     * @param destiny          The desired destination coordinates.
     * @return An array of control commands for position and angle.
     */
    public double[] execute(EnvironmentState environmentState, double[] destiny) {
        double[] command = new double[2];
        // Calculate control commands here
        
        command[0] = -40;
        command[1] = 40;
        // Apply control commands as needed
        return command;
    }
}
