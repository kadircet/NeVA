raw_data = open('frmtr.txt').readlines()
name_start = "&#8226; "
possible_food = map(
    lambda x: x[x.find(name_start) + len(name_start):x.find(':')],
    filter(lambda x: "&#8226;" in x, raw_data))

for food in possible_food:
    print(('INSERT INTO `item_suggestion` (`user_id`, `category_id`, ' +
           '`suggestion`) VALUES ({}, {}, "{}");').format(1, 1, food))
