#%load_ext autoreload
#%autoreload 2

import os, sys, inspect
currentdir = os.path.dirname(
    os.path.abspath(inspect.getfile(inspect.currentframe())))
parentdir = os.path.dirname(currentdir)
sys.path.insert(0, parentdir)

import utils

all_user_ids = list(utils.GetUserIDs())
user_ids = []
for user_id in all_user_ids:
    accuracies = utils.MeasureAccuracy(user_id)
    print(user_id, accuracies)
    if len(accuracies) > 3:
        user_ids.append(user_id)

accuracies = {}
for user_id in user_ids:
    accuracies[user_id] = utils.MeasureAccuracy(user_id)

import matplotlib.pyplot as plt
from random import shuffle

number_of_users = 9
random_batch = user_ids
shuffle(random_batch)
random_batch = random_batch[:number_of_users]

plt.figure(figsize=(18, 16), dpi=80)

for idx, user_id in enumerate(random_batch):
    plt.subplot(331 + idx)
    plt.plot(list(x[1] for x in accuracies[user_id]))
    plt.grid(True)
    plt.title('User id:' + str(user_id))

plt.show()

plt.figure(figsize=(18, 16), dpi=80)

for idx, user_id in enumerate(random_batch):
    plt.subplot(331 + idx)
    plt.plot(list(x[0] for x in accuracies[user_id]))
    plt.grid(True)
    plt.title('User id:' + str(user_id))

plt.show()
