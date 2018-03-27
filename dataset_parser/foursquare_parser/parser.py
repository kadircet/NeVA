
import csv
from datetime import datetime, timedelta

def createTimestamp(d, tz_offset):
  try:
    new_date = datetime.strptime(d, '%a %b %d %X %z %Y')
    new_date = new_date + timedelta(hours=(int(tz_offset)/60))
  except Exception as e:
    print(d, tz_offset)
    return "00000000" 
  return str(int(new_date.timestamp()))

# dataset_ubicomp2013 dataset includes check-ins, tips and tags data of restaurant venues in NYC collected from Foursquare from 24 October 2011 to 20 February 2012.
# It contains three files in tsv format, including 3112 users and 3298 venues with 27149 check-ins and 10377 tips.

# dataset_ubicomp2013_checkins.txt has two columns. 
# 1. user_id
# 2. venue_id

# dataset_ubicomp2013_tips.txt has three columns. 
# 1. user_id
# 2. venue_id
# 3. tip/comment

# dataset_ubicomp2013_tags.txt has two columns. 
# 1. venue_id
# 2. tag set

users = set()
venues = set()
tag_names = dict()
tags_table = dict()
venue_tags = dict()
history = []

with open("dataset_ubicomp2013/dataset_2013_tags.txt", "r") as tagfile_2013:
  reader = csv.reader(tagfile_2013, delimiter='\t')
  tag_count = 0
  tag_idx = 0
  for venue_id, tags in reader:
    tag_list = tags.split(',')
    venue_tag_list=[]
    for tag_name in tag_list:
      if tag_name in tag_names:
        venue_tag_list.append(str(tag_names[tag_name]))
      else:
        tag_names[tag_name] = tag_count
        venue_tag_list.append(str(tag_count))
        tag_count += 1
    venue_tags[venue_id] = venue_tag_list
  
for tag_name in tag_names:
  tags_table[tag_names[tag_name]] = tag_name

with open("dataset_ubicomp2013/dataset_2013_checkins.txt", "r") as checkins_2013:
  reader = csv.reader(checkins_2013, delimiter='\t')
  for user_id, venue_id in reader:
    users.add(user_id)
    venues.add(venue_id)
    history.append(user_id+","+venue_id+",0,0,0\n")

with open("2013/users.txt", "w") as user_file:
  user_file.write("user_id,email,salt,status\n")
  for user in users:
    line = user + ',' + user + '@foursquare,' + "SALT, 1\n"
    user_file.write(line)

with open("2013/suggestee.txt", "w") as suggestee_file:
  suggestee_file.write("category_id,name,last_updated\n")
  for venue in venues:
    line = "1," + venue + ",1\n"
    suggestee_file.write(line)
with open("2013/tag.txt", "w") as tag_file:
  tag_file.write("id,key\n")
  for tag_id in tags_table:
    line = str(tag_id) + "," + tags_table[tag_id] + "\n"
    tag_file.write(line)
with open("2013/suggestee_tags.txt", "w") as suggestee_tag_file:
  suggestee_tag_file.write("suggestee_id,tag_id,value\n")
  for venue_id in venue_tags:
    tags = venue_tags[venue_id]
    for tag in tags:
      line = venue_id + "," + tag + ", \n"
      suggestee_tag_file.write(line)
with open("2013/user_choice_history.txt", "w") as history_file:
  history_file.write("user_id,suggestee_id,timestamp,latitude,longitude\n")
  history_file.writelines(history)

del users
del venues
del tag_names
del tags_table
del venue_tags
del history

# dataset_tsmc2014 includes long-term (about 10 months) check-in data in New York city and Tokyo collected from Foursquare from 12 April 2012 to 16 February 2013.
# It contains two files in tsv format. Each file contains 8 columns, which are:

# 1. User ID (anonymized)
# 2. Venue ID (Foursquare)
# 3. Venue category ID (Foursquare)
# 4. Venue category name (Fousquare)
# 5. Latitude
# 6. Longitude
# 7. Timezone offset in minutes (The offset in minutes between when this check-in occurred and the same time in UTC)
# 8. UTC time

users = set() # user table
venues = set() # suggestee table
tag_names = {} # tag table
venue_tags = {} # suggestee tags table
user_choice_histrory = [] # user choice history table

user_history = {}

with open("dataset_tsmc2014/dataset_2014_NYC.txt", 'r') as data2014_file:
  reader = csv.reader(data2014_file, delimiter='\t')
  for user_id, venue_id, venue_cat_id, venue_cat_name, lat, lon, tz_offset, utc_time in reader:
    timestamp = createTimestamp(utc_time, tz_offset)
    user_choice_histrory.append(user_id +','+venue_id+','+timestamp+','+lat+','+lon+"\n")

    if user_id in user_history:
      user_history[user_id].append((venue_id,timestamp))
    else:
      user_history[user_id] = [(venue_id,timestamp)]

    users.add(user_id)
    venues.add(venue_id)
    tag_names[venue_cat_id] = venue_cat_name
    venue_tags[venue_id] = venue_cat_id

with open("2014/users.txt", "w") as user_file:
  user_file.write("user_id,email,salt,status\n")
  for user in users:
    line = user + ',' + user + '@foursquare,' + "SALT, 1\n"
    user_file.write(line)

with open("2014/suggestee.txt", "w") as suggestee_file:
  suggestee_file.write("category_id,name,last_updated\n")
  for venue in venues:
    line = "1," + venue + ",1\n"
    suggestee_file.write(line)
with open("2014/tag.txt", "w") as tag_file:
  tag_file.write("id,key\n")
  for tag_id in tag_names:
    line = tag_id + "," + tag_names[tag_id] + "\n"
    tag_file.write(line)
with open("2014/suggestee_tags.txt", "w") as suggestee_tag_file:
  suggestee_tag_file.write("suggestee_id,tag_id,value\n")
  for venue_id in venue_tags:
    line = venue_id + "," + venue_tags[venue_id] + ", \n"
    suggestee_tag_file.write(line)
with open("2014/user_choice_history.txt", "w") as history_file:
  history_file.write("user_id,suggestee_id,timestamp,latitude,longitude\n")
  history_file.writelines(user_choice_histrory)

del users
del venues
del tag_names
del venue_tags
del user_choice_histrory


# Global dataset includes long-term (about 18 months from April 2012 to September 2013) global-scale check-in data collected from Foursquare.
# It contains 33,278,683 checkins by 266,909 users on 3,680,126 venues (in 415 cities in 77 countries). Those 415 cities are the most checked 415 cities in the world, each of which contains at least 10000 check-ins). Please see the references for more details about data collection and processing.
# It contains three files in tsv format.

# - File dataset_TIST2015_Checkins.txt contains all check-ins with 4 columns, which are:
# 1. User ID (anonymized)
# 2. Venue ID (Foursquare)
# 3. UTC time
# 4. Timezone offset in minutes (The offset in minutes between when this check-sin occurred and the same time in UTC, i.e., UTC time + offset is the local time)

# - File dataset_TIST2015_POIs.txt contains all venue data with 7 columns, which are:
# 1. Venue ID (Foursquare) 
# 2. Latitude
# 3. Longitude
# 4. Venue category name (Foursquare)
# 5. Country code (ISO 3166-1 alpha-2 two-letter country codes)

# - File dataset_TIST2015_Cities.txt contains all 415 cities data with 6 columns, which are:
# 1. City name
# 2. Latitude (of City center)
# 3. Longitude (of City center)
# 4. Country code (ISO 3166-1 alpha-2 two-letter country codes)
# 5. Country name
# 6. City type (e.g., national capital, provincial capital)

users = set()
venues = set()
tag_names = {}
venue_coords = {}
venue_tags = {}
tags = {}
global_history = []
with open("dataset_TIST2015/dataset_2015_POIs.txt", "r") as venues_2015:
  reader = csv.reader(venues_2015, delimiter='\t')
  tag_id = 0
  for venue_id, lat, lon, venue_cat_name, country in reader:
    venues.add(venue_id)
    
    tag_idx = tag_id
    if venue_cat_name in tag_names:
      tag_idx = tag_names[venue_cat_name]
    else:
      tag_names[venue_cat_name] = tag_id
      tag_id+=1

    venue_coords[venue_id] = (lat, lon)
    venue_tags[venue_id] = str(tag_idx)

for name in tag_names:
  tags[tag_names[name]]=name

with open("dataset_TIST2015/dataset_2015_Checkins.txt", "r") as checkins_2015:
  i = 0
  percent = 0
  reader = csv.reader(checkins_2015, delimiter='\t')
  for user_id,venue_id,utc_time,tz_offset in reader:
    i+=1
    users.add(user_id)
    timestamp = createTimestamp(utc_time, tz_offset)
    global_history.append(user_id + ","+venue_id+","+timestamp+","+venue_coords[venue_id][0]+","+venue_coords[venue_id][1]+"\n")
    if(i%1000000 == 0):
      print(i/33263633)


with open("2015/users.txt", "w") as user_file:
  user_file.write("user_id,email,salt,status\n")
  for user in users:
    line = user + ',' + user + '@foursquare,' + "SALT, 1\n"
    user_file.write(line)
with open("2015/suggestee.txt", "w") as suggestee_file:
  suggestee_file.write("category_id,name,last_updated\n")
  for venue in venues:
    line = "1," + venue + ",1\n"
    suggestee_file.write(line)
with open("2015/tag.txt", "w") as tag_file:
  tag_file.write("id,key\n")
  for tag_id in tags:
    line = str(tag_id) + "," + tags[tag_id] + "\n"
    tag_file.write(line)
with open("2015/suggestee_tags.txt", "w") as suggestee_tag_file:
  suggestee_tag_file.write("suggestee_id,tag_id,value\n")
  for venue_id in venue_tags:
    line = venue_id + "," + venue_tags[venue_id] + ", \n"
    suggestee_tag_file.write(line)
with open("2015/user_choice_history.txt", "w") as history_file:
  history_file.write("user_id,suggestee_id,timestamp,latitude,longitude\n")
  history_file.writelines(global_history)
