import json

all_categories = []
food_related_categories = []
food_related_businesses = []

with open('business.json') as business_file:
    for line in business_file:
        data_loaded = json.loads(line)

        c = data_loaded["categories"]
        business_id = data_loaded["business_id"]

        food_related = False
        if (("Food" in c) or ("Restaurant" in c) or ("Kitchen" in c)):
            food_related = True
            food_related_businesses.append(business_id)
            
        for item in c:
            all_categories.append(item)

            if food_related:
                food_related_categories.append(item)

all_categories = set(all_categories)
food_related_categories = set(food_related_categories)

category_file = open("categories.txt", "w")
food_related_file = open("food_categories.txt", "w")
food_business_file = open("food_related_businesses.txt", "w")

for category in all_categories:
    category_file.write(category)
    category_file.write("\n")

for category in food_related_categories:
    food_related_file.write(category)
    food_related_file.write("\n")

for business in food_related_businesses:
    food_business_file.write(business)
    food_business_file.write("\n")

food_business_file.close()
category_file.close()
food_related_file.close()