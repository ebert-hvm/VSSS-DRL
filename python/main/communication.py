import traceback
import threading
import os
import socket
from FiraSim_message_pb2 import Environment, Command, BallReplacement, RobotReplacement, Robot, Replacement, Commands, Packet
from shared_object import SharedObject
import time
import json

class Communication:
    def __init__(self):
        self.rx = SharedObject(False)
        self.message = SharedObject(bytearray(2048))
        self.message_length = SharedObject(0)
        self.environment = SharedObject(None)
        self.commands = SharedObject(None)
        self.packet = SharedObject(bytearray(2048))
        self.packet_length = SharedObject(0)
        self.replace = SharedObject(False)
        self.robot_replace_pos = SharedObject([0.0, 0.0, 0.0])
        self.ball_replace_pos = SharedObject([0.0, 0.0])
    def receive_frame(self):
        multicast_group = '224.0.0.1'
        port = 10002

        receive_data = bytearray(2048)
        multicast_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        multicast_socket.bind((multicast_group, port))

        multicast_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        multicast_socket.settimeout(0.5)  # Set the socket timeout
        try:
            while True:
                start_time = time.time()  # Get the current time
                connected = True  # Assume we are initially connected
                try:
                    #print('try get data')
                    data, _ = multicast_socket.recvfrom(2048)
                    # Set the flag to True if data is received
                    self.rx.set_value(True)
                    self.message.set_value(data)
                    self.message_length.set_value(len(data))
                except socket.timeout:
                    print('timeout')
                except socket.error as e:
                    # Connection lost, need to reconnect
                    print("Socket error:", e)
                    connected = False

                if not connected:
                    # Connection lost, reset the flag and attempt to reconnect
                    self.rx.set_value(True)
                    multicast_socket.close()
                    multicast_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
                    multicast_socket.bind(('', port))
                    multicast_socket.settimeout(0.5)  # Set the socket timeout
                    multicast_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        except KeyboardInterrupt:
            multicast_socket.close()

    def decode_message(self):
        while True:
            try:
                message = self.message.get_value()
                length = self.message_length.get_value()
                if length == 0:
                    continue

                actual_message = message[:length]
                env = Environment()

                try:
                    env.ParseFromString(actual_message)
                    if env:
                        self.environment.set_value(env)
                    else:
                        print("Environment is null")
                except Exception as ex:
                    pass
            except Exception as ex:
                print(f"Error decoding the message: {ex}")
                traceback.print_exc()

    def print_received_frame(self):
        while True:
            try:
                time.sleep(0.08)  # Sleeping for 80 milliseconds
                os.system('clear')
                env = self.environment.get_value()  # Assuming self.environment is a SharedObject
                if self.rx.get_value() and env is not None:
                    print("rx")
                    print("Field:")
                    print(f"Length: {env.field.length}")
                    print(f"Width: {env.field.width}")
                    print(f"GoalDepth: {env.field.goal_depth}")
                    print(f"GoalWidth: {env.field.goal_width}")

                    print("Ball:")
                    ball = env.frame.ball
                    vx = round(ball.vx, 4)
                    vy = round(ball.vy, 4)
                    vnorm = (vx ** 2 + vy ** 2) ** 0.5
                    print(f"x: {round(ball.x, 4)}; y: {round(ball.y, 4)}; vX: {vx}; vY: {vy}; |v|: {vnorm}")

                    print("Blue Robots:")
                    for robot in env.frame.robots_blue:
                        print(f"id: {robot.robot_id}; x: {round(robot.x, 4)}; y: {round(robot.y, 4)}; vx: {round(robot.vx, 4)}; vy: {round(robot.vy, 4)}")

                    print("Yellow Robots:")
                    for robot in env.frame.robots_yellow:
                        print(f"id: {robot.robot_id}; x: {round(robot.x, 4)}; y: {round(robot.y, 4)}; vx: {round(robot.vx, 4)}; vy: {round(robot.vy, 4)}")
            except Exception as e:
                pass
    def send_package(self):
        parameters = json.loads(open("../resources/parameters.json").read())
        ip_address = parameters["address"]
        port = 20011
        while True:
            try:
                time.sleep(0.014)  # 14 milliseconds
            except Exception as ex:
                pass
            packet_array = self.packet.get_value()
            if len(packet_array) == 0:
                continue
            # Uncomment the following lines to print the packet data in hex format
            # for i in packet_array:
            #     print(format(i, '02x'), end='')
            # print()

            with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as sock:
                address = (ip_address, port)
                sock.sendto(bytes(packet_array), address)
    
    def default_replacement(self):
        ball_replacement = BallReplacement(
            x=self.ball_replace_pos.get_value()[0],
            y=self.ball_replace_pos.get_value()[1],
            vx=0,
            vy=0
        )

        robots_replacement = [
            RobotReplacement(
                position=Robot(
                    robot_id=0,
                    x=self.robot_replace_pos.get_value()[0],
                    y=self.robot_replace_pos.get_value()[1],
                    orientation=self.robot_replace_pos.get_value()[2],
                    vx=0,
                    vy=0,
                    vorientation=0
                ),
                yellowteam=False,
                turnon=True
            ),
            RobotReplacement(
                position=Robot(
                    robot_id=1,
                    x=-0.6,
                    y=0.9,
                    orientation=-90,
                    vx=0,
                    vy=0,
                    vorientation=0
                ),
                yellowteam=False,
                turnon=False
            ),
            RobotReplacement(
                position=Robot(
                    robot_id=2,
                    x=-0.3,
                    y=0.9,
                    orientation=-90,
                    vx=0,
                    vy=0,
                    vorientation=0
                ),
                yellowteam=False,
                turnon=False
            ),
            RobotReplacement(
                position=Robot(
                    robot_id=0,
                    x=1.5,
                    y=0,
                    orientation=180,
                    vx=0,
                    vy=0,
                    vorientation=0
                ),
                yellowteam=True,
                turnon=True
            ),
            RobotReplacement(
                position=Robot(
                    robot_id=1,
                    x=0.3,
                    y=0.9,
                    orientation=-90,
                    vx=0,
                    vy=0,
                    vorientation=0
                ),
                yellowteam=True,
                turnon=False
            ),
            RobotReplacement(
                position=Robot(
                    robot_id=2,
                    x=0.6,
                    y=0.9,
                    orientation=-90,
                    vx=0,
                    vy=0,
                    vorientation=0
                ),
                yellowteam=True,
                turnon=False
            )
        ]

        replacement = Replacement(
            ball=ball_replacement,
            robots=robots_replacement
        )
        return replacement

    def encode_message(self):
        while True:
            try:
                commands_array = self.commands.get_value()
                if commands_array is None:
                    continue

                # Process Command messages
                robot_commands = []
                for command in commands_array:

                    robot_command = Command(
                        id=command.id,
                        yellowteam=command.yellowteam,
                        wheel_left=command.wheel_left,
                        wheel_right=command.wheel_right
                    )
                    robot_commands.append(robot_command)

                commands = Commands(robot_commands=robot_commands)

                packet = Packet(cmd=commands)
                
                if self.replace.get_value():
                    replacement = self.default_replacement()
                    packet.replace.CopyFrom(replacement)

                # Serialize the Packet
                serialized_packet = packet.SerializeToString()
                self.packet.set_value(serialized_packet)

            except Exception as ex:
                traceback.print_exc()
    def start_receiving(self):
        threads = [
            threading.Thread(target=self.receive_frame),
            threading.Thread(target=self.decode_message),
           # threading.Thread(target=self.print_received_frame)
        ]
        for thread in threads:
            thread.start()
        try:
            while True:
                time.sleep(1)
        except KeyboardInterrupt:
            for thread in threads:
                thread.join()

    def start_sending(self):
        sender_thread = threading.Thread(target=self.send_package)
        encoder_thread = threading.Thread(target=self.encode_message)

        sender_thread.start()
        encoder_thread.start()

        try:
            while True:
                time.sleep(1)
        except KeyboardInterrupt:
            sender_thread.join()
            encoder_thread.join()

# Usage
#comm = Communication()
#comm.startReceiving()
#comm.startSending()
