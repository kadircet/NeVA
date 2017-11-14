import translator

en_names = open("foods-en.txt", "w")
tr_names = open("foods-tr.txt", "w")
tag_file = open("tags.txt", "w")

categories = []
foods = []

with open("items.tsv") as items_file:
    for line in items_file:
        if(line.startswith("id")):
            continue
        line = line.split()
        food_id = line[0]

        items = line[1].split(",")
        for item in items:
            tags = item.split("__")

            main_category = tags[0].replace("_", " ")
            sub_category = tags[1].replace("_", " ")
            food = tags[2].replace("_"," ")

            categories.append(main_category)
            categories.append(sub_category)

            if(main_category == "condiment" or main_category == "preparation"):
                categories.append(food)
            else:
                foods.append(food)

categories = set(categories)
foods = set(foods)

for category in categories:
    tag_file.write(category)
    tag_file.write("\n")

for food in foods:
    en_names.write(food)
    en_names.write("\n")

    tr_names.write(eval(translator.translate(food))["text"][0])
    tr_names.write("\n")

en_names.close()
tr_names.close()
tag_file.close()