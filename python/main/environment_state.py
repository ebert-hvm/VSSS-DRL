from shared_object import SharedObject
from FiraSim_message_pb2 import Environment
import traceback
import gymnasium as gym

class EnvironmentState(gym.Env):
    class Field:
        def __init__(self):
            self.length = SharedObject(0.0)
            self.width = SharedObject(0.0)
            self.goal_depth = SharedObject(0.0)
            self.goal_width = SharedObject(0.0)

        def set(self, field):
            if field is None:
                print("field is null")
                return
            self.length.set_value(1000 * field.length)
            self.width.set_value(1000 * field.width)
            self.goal_depth.set_value(1000 * field.goal_depth)
            self.goal_width.set_value(1000 * field.goal_width)

    class Ball:
        def __init__(self):
            self.x = SharedObject(0.0)
            self.y = SharedObject(0.0)
            self.vX = SharedObject(0.0)
            self.vY = SharedObject(0.0)

        def set(self, ball, field_length, goals_blue, goals_yellow):
            if ball is None:
                print("ball is null")
                return
            x_ball = ball.x
            if x_ball > field_length / 2:
                goals_blue.set_value(goals_blue.get_value() + 1)
            elif x_ball < -field_length / 2:
                goals_yellow.set_value(goals_yellow.get_value() + 1)
            self.x.set_value(1000 * x_ball)
            self.y.set_value(1000 * ball.y)
            self.vX.set_value(ball.vx)
            self.vY.set_value(ball.vy)

    class Robot:
        def __init__(self):
            self.x = SharedObject(0.0)
            self.y = SharedObject(0.0)
            self.angle = SharedObject(0.0)
            self.vX = SharedObject(0.0)
            self.vY = SharedObject(0.0)
            self.vAngle = SharedObject(0.0)
            self.id = SharedObject(0)

        def set(self, robot):
            if robot is None:
                print("robot is null")
                return
            self.x.set_value(1000 * robot.x)
            self.y.set_value(1000 * robot.y)
            self.angle.set_value(robot.orientation)
            self.vX.set_value(robot.vx)
            self.vY.set_value(robot.vy)
            self.vAngle.set_value(robot.vorientation)
            self.id.set_value(robot.robot_id)

    def __init__(self):
        self.field = self.Field()
        self.ball = self.Ball()
        self.blue_robots = [self.Robot() for _ in range(3)]
        self.yellow_robots = [self.Robot() for _ in range(3)]
        self.goals_blue = SharedObject(0)
        self.goals_yellow = SharedObject(0)
        self.last_env = None

    def print_state(self):
        try:
            # Print state information here
            pass
        except Exception as ex:
            # Handle exceptions
            pass

    def update_state(self, env):
        self.last_env = env
        if env is None:
            print("env is null")
            return
        self.field.set(env.field)
        self.ball.set(env.frame.ball, env.field.length, self.goals_blue, self.goals_yellow)
        for i in range(1):
            try:
                self.blue_robots[i].set(env.frame.robots_blue[i])
            except Exception as ex:
                traceback.print_exc()
                pass
            try:
                self.yellow_robots[i].set(env.frame.robots_yellow[i])
            except Exception as ex:
                traceback.print_exc()
                pass

    def step(self, action):
        print("cock")

    def reset():
        print("ball")