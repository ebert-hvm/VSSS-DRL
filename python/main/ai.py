import threading
import traceback
import time
import random
from FiraSim_message_pb2 import Command
from shared_object import SharedObject
from communication import Communication
from environment_state import EnvironmentState

class AI:
    def __init__(self):
        self.random = random.Random()
        self.communication = Communication()
        self.environment_state = EnvironmentState()
        self.last_state = EnvironmentState()
        self.blue_velocity = SharedObject([[0.0] * 2 for _ in range(3)])
        self.yellow_velocity = SharedObject([[0.0] * 2 for _ in range(3)])
        self.acceleration = 4

    def get_random_double(self):
        return -1.0 + 2.0 * self.random.random()

    def execute_command(self):
        while True:
            try:
                time.sleep(0.08)
                commands = []
                blue_velocity = self.blue_velocity.get_value()
                yellow_velocity = self.yellow_velocity.get_value()
                for i in range(3):
                    #commands.append(Command(id=i, yellowteam=False, wheel_left=10, wheel_right=10))
                    commands.append(Command(id=i, yellowteam=False, wheel_left=blue_velocity[i][0], wheel_right=blue_velocity[i][1]))
                    commands.append(Command(id=i, yellowteam=True, wheel_left=yellow_velocity[i][0], wheel_right=yellow_velocity[i][1]))
                self.communication.commands.set_value(commands)
            except Exception as ex:
                print(ex)
                traceback.print_exc()

    def distance(self, v, u):
        dist = sum((v[i] - u[i]) ** 2 for i in range(2))
        return dist ** 0.5

    def subtract(self, v, u):
        return [v[i] - u[i] for i in range(2)]

    def reward(self):
        ball_pos = [self.environment_state.ball.x.get_value(), self.environment_state.ball.y.get_value()]
        robot_pos = [self.environment_state.blue_robots[0].x.get_value(), self.environment_state.blue_robots[0].y.get_value()]
        robot_ball = self.subtract(ball_pos, robot_pos)
        zero = [0.0, 0.0]
        ball_replace_pos = self.communication.ball_replace_pos.get_value()
        ini_pos = self.subtract(ball_replace_pos, self.communication.robot_replace_pos.get_value())
        initial_d = 1000.0 * self.distance(zero, ini_pos)

        robot_ball_reward = 1 - self.distance(robot_ball, zero) / initial_d
        if ball_pos[0] - ball_replace_pos[0] * 1000 > 0:
            ball_goal_reward = (ball_pos[0] - ball_replace_pos[0] * 1000) / (self.environment_state.field.length.get_value() / 2 - ball_replace_pos[0] * 1000)
        else:
            ball_goal_reward = -(ball_pos[0] - ball_replace_pos[0] * 1000) / (-self.environment_state.field.length.get_value() / 2 - ball_replace_pos[0] * 1000)

        total_reward = 0.1 * robot_ball_reward + 0.9 * ball_goal_reward
        if self.environment_state.goals_blue.get_value() > self.last_state.goals_blue.get_value():
            total_reward = 1
        elif self.environment_state.goals_yellow.get_value() > self.last_state.goals_yellow.get_value():
            total_reward = -1
        return total_reward

    def execute_ai(self):
        timer = 7
        time_val = time.time() - timer
        #dqn = DQN(12, 3, 8, 32, 32, 0.99, 0.001)
        blue_velocity = [[0.0] * 2 for _ in range(3)]
        first = True
        try:
            cnt=0
            while True:
                cnt = (cnt +1)%5
                if not self.communication.rx.get_value():
                    continue
                
                if self.environment_state.last_env is not None:
                    self.last_state.update_state(self.environment_state.last_env)

                self.environment_state.update_state(self.communication.environment.get_value())
                replace = self.communication.replace.get_value()
                if replace and time.time() - time_val > 0.5:
                    self.communication.replace.set_value(False)
                    time_val = time.time()

                elif not replace and (time.time() - time_val > timer or
                                    self.environment_state.goals_blue.get_value() != self.last_state.goals_blue.get_value() or
                                    self.environment_state.goals_yellow.get_value() != self.last_state.goals_yellow.get_value()):
                    time_val = time.time()
                    x1 = self.get_random_double() * (self.environment_state.field.length.get_value() / 2 - 40) / 1000
                    x2 = self.get_random_double() * (self.environment_state.field.length.get_value() / 2 - 21) / 1000
                    while abs(x2 - x1) < 0.061:
                        x2 = self.get_random_double() * (self.environment_state.field.length.get_value() / 2 - 21) / 1000

                    robot_replace = [x1, 0.0, 0.0]
                    ball_replace = [x2, 0.0, 0.0]

                    if not first:
                        #reward = self.reward()
                        #dqn.collect(reward, False)
                        pass
                    else:
                        first = False
                    self.communication.replace.set_value(True)
                    self.communication.robot_replace_pos.set_value(robot_replace)
                    self.communication.ball_replace_pos.set_value(ball_replace)
                robot = self.environment_state.blue_robots[0]
                ball = self.environment_state.ball
                state = [
                    float(robot.x.get_value()),
                    float(robot.y.get_value()),
                    float(robot.vX.get_value()),
                    float(robot.vY.get_value()),
                    float(robot.angle.get_value()),
                    float(robot.vAngle.get_value()),
                    float(ball.x.get_value()),
                    float(ball.y.get_value()),
                    float(ball.vX.get_value()),
                    float(ball.vY.get_value()),
                    float(self.environment_state.goals_blue.get_value()),
                    float(self.environment_state.goals_yellow.get_value())
                ]

                #action = dqn.react(state)
                blue_velocity = self.blue_velocity.get_value()
                action = cnt
                try:
                    if action == 0:
                        # FORWARD
                        blue_velocity[0][0] = 15
                        blue_velocity[0][1] = 15
                        time.sleep(0.2)
                    elif action == 1:
                        # BACKWARD
                        blue_velocity[0][0] = -15
                        blue_velocity[0][1] = -15
                        time.sleep(0.2)
                    elif action == 2:
                        # STOP
                        blue_velocity[0][0] = 0
                        blue_velocity[0][1] = 0
                        time.sleep(0.1)
                    elif action == 3:
                        # TURN LEFT
                        blue_velocity[0][0] = max(blue_velocity[0][0] + self.acceleration, -20)
                        blue_velocity[0][1] = max(blue_velocity[0][0] - self.acceleration, -20)
                        time.sleep(0.1)
                    elif action == 4:
                        # TURN RIGHT
                        blue_velocity[0][0] = max(blue_velocity[0][0] - self.acceleration, -20)
                        blue_velocity[0][1] = max(blue_velocity[0][0] + self.acceleration, -20)
                        time.sleep(0.1)
                except Exception as ex:
                    print(ex)
                    traceback.print_exc()
                self.blue_velocity.set_value(blue_velocity)
        except Exception as ex:
            print(ex)
            traceback.print_exc()
    def start(self):
        threads = [
            threading.Thread(target=self.communication.start_receiving),
            threading.Thread(target=self.communication.start_sending),
            threading.Thread(target=self.execute_ai),
            threading.Thread(target=self.execute_command),
        ]
        for thread in threads:
            thread.start()

        try:
            while True:
                time.sleep(1)
        except KeyboardInterrupt:
            for thread in threads:
                thread.interrupt()

            for thread in threads:
                thread.join()
