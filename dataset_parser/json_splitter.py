import json

count = 0
file_count = 0
filename = "reviews/review_0.json"
current_file = open(filename, "w")
with open('review.json') as review_file:
    for line in review_file:
        if(count > 100000):
            count = 0
            file_count += 1

            filename = "reviews/review_" + str(file_count) + ".json"

            current_file.close()
            current_file = open(filename, "w")
        else:
            count += 1
            
        current_file.write(line)

current_file.close()