package Client;

import com.fasterxml.jackson.databind.JsonNode;

public class Navigation {
    public class PIDController {
        private double kp, ki, kd, limit;
        private double error, integrativeError, derivativeError;

        public PIDController(JsonNode pidParameters) {

            kp = pidParameters.get("kp").asDouble();
            ki = pidParameters.get("ki").asDouble();
            kd = pidParameters.get("kd").asDouble();
            limit = pidParameters.get("limit").asDouble();
            reset();
        }

        public void reset() {
            error = integrativeError = derivativeError = 0.0;
        }

        public double calculate(double error) {
            integrativeError += error;
            derivativeError = error - this.error;
            this.error = error;
            return Main.clamp(kp * error + ki * integrativeError + kd * derivativeError, -limit, limit);
        }
    }

    public static int framePeriod = 12;
    public PIDController positionPid, anglePid;

    public Navigation() {
        positionPid = new PIDController(Main.parameters.get("pid").get("position"));
        anglePid = new PIDController(Main.parameters.get("pid").get("angle"));
    }

    public double[] execute(EnvironmentState environmentState, double[] destiny) {
        double[] command = new double[2];
        // // double calc = positionPid.calculate(destiny[0] -
        // // environmentState.blueRobots[0].x.Get());
        // // System.out.println(environmentState.blueRobots[0].angle.Get());
        // double calc = anglePid.calculate(destiny[2] - 180 / Math.PI *
        // environmentState.blueRobots[0].angle.Get());
        command[0] = 40;
        command[1] = 40;
        // if (calc < 0) {
        // command[0] *= -1;
        // command[1] *= -1;
        // }
        return command;
    }
}
