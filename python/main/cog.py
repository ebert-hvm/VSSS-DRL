import torch
import torch.optim as optim
import gymnasium as gym
import matplotlib.pyplot as plt

from itertools import count
from classes import DQN, ReplayMemory
from functions import select_action, plot_durations, optimize_model
from hyperparams import TAU, LR

env = gym.make("CartPole-v1")
plt.ion()
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

n_actions = env.action_space.n
# Get the number of state observations
state, info = env.reset()
n_observations = len(state)
policy_net = DQN(n_observations, n_actions).to(device)
target_net = DQN(n_observations, n_actions).to(device)
target_net.load_state_dict(policy_net.state_dict())

optimizer = optim.AdamW(policy_net.parameters(), lr=LR, amsgrad=True)
memory = ReplayMemory(10000)

steps_done = [0]

episode_durations = []
if torch.cuda.is_available():
    num_episodes = 2000
else:
    num_episodes = 50

for i_episode in range(num_episodes):
    # Initialize the environment and get its state
    state, info = env.reset()
    state = torch.tensor(state, dtype=torch.float32, device=device).unsqueeze(0)
    for t in count():
        action = select_action(state, steps_done, env, device, policy_net)
        observation, reward, terminated, truncated, _ = env.step(action.item())
        reward = torch.tensor([reward], device=device)
        done = terminated or truncated

        if terminated:
            next_state = None
        else:
            next_state = torch.tensor(observation, dtype=torch.float32, device=device).unsqueeze(0)
        memory.push(state, action, next_state, reward)
        state = next_state
        # Perform one step of the optimization (on the policy network)
        optimize_model(device, memory, optimizer, policy_net, target_net)

        # Soft update of the target network's weights
        # θ′ ← τ θ + (1 −τ )θ′
        target_net_state_dict = target_net.state_dict()
        policy_net_state_dict = policy_net.state_dict()
        for key in policy_net_state_dict:
            target_net_state_dict[key] = policy_net_state_dict[key] * \
                TAU + target_net_state_dict[key]*(1-TAU)
        target_net.load_state_dict(target_net_state_dict)

        if done:
            episode_durations.append(t + 1)
            plot_durations(episode_durations)
            break

print('Complete')
plot_durations(show_result=True)
plt.ioff()
plt.show()
