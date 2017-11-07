import json
import os
import sys

rootdir = sys.argv[1]
food_related_reviews = {}
businesses = []

with open("food_related_businesses.txt") as business_file:
    for line in business_file:
        businesses.append(line)

for root, subFolders, files in os.walk(rootdir):
        for file in files:
            with open(os.path.join(root, file)) as review_file:
                for line in review_file:
                    data_loaded = json.loads(line)
                    business_id = data_loaded["business_id"]
                    if business_id in businesses:
                        user = data_loaded["user_id"]
                        review = data_loaded["review_id"]
                        if(user not in food_related_reviews):
                            food_related_reviews[user] = []

                        food_related_reviews[user].append(review)

user_file = open("users.txt", "w")

for key, value in food_related_reviews:
    user_file.write(key)
    user_file.write(" --> ")

    for review in value:
        user_file.write(review)
        user_file.write(" ")

    user_file.write("\n")

user_file.close()