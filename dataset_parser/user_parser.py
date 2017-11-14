
foods = {}

with open("items.tsv") as items_file:
    for line in items_file:
        if(line.startswith("id")):
            continue
        line = line.split()
        food_id = line[0]
        foods[food_id] = []

        items = line[1].split(",")
        for item in items:
            tags = item.split("__")

            food = tags[2].replace("_"," ")

            foods[food_id].append(food)

last_user = "-1"
user_file = open("users/user_0.txt", "w")

with open("data.tsv") as data_file:
    for line in data_file:
        if(line.startswith("meal_id")):
            continue

        line = line.split()
        cur_user = line[1]

        if(cur_user != last_user):
            user_file.close()
            user_file = open("users/user_" + cur_user + ".txt", "w")
            last_user = cur_user

        user_file.write("|| ")

        meals = line[4].split(",")
        for meal in meals:
            for food in foods[meal]:
                user_file.write(food + " || ")

        user_file.write("\n")

user_file.close()