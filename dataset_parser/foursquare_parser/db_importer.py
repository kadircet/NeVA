import MySQLdb
import csv


db = MySQLdb.connect("localhost", "neva", "", "neva")
user_dict = {}
tag_dict = {}
suggestee_dict = {}

insert_user_query = "INSERT INTO `user` (`email`, `salt`, `status`) VALUES (%s, %s, 1)"
insert_suggestee_query = "INSERT INTO `suggestee` (`category_id`, `name`, `last_updated`) VALUES (1, %s, 0)"
insert_tag_query = "INSERT INTO `tag` (`key`) VALUES (%s)"
insert_suggestee_tags_query = "INSERT INTO `suggestee_tags` (`suggestee_id`, `tag_id`) VALUES (%s, %s)"
insert_user_choice_history = "INSERT INTO `user_choice_history` (`user_id`, `suggestee_id`, `timestamp`, `latitude`, `longitude`) VALUES (%s, %s, %s, %s, %s)"

def reset_db():
  global db
  if db == None:
    db = MySQLdb.connect("localhost", "neva", "", "neva")
  with db.cursor() as cur:
    cur.execute("DROP TABLE IF EXISTS `user_choice_history`")
    cur.execute("DROP TABLE IF EXISTS `suggestee_tags`")
    cur.execute("DROP TABLE IF EXISTS `tag`")
    cur.execute("DROP TABLE IF EXISTS `suggestee`")
    cur.execute("DROP TABLE IF EXISTS `user`")
    db.commit()
    cur.execute("CREATE TABLE IF NOT EXISTS `user` (`id` INTEGER NOT NULL AUTO_INCREMENT,`email` VARCHAR(255) NOT NULL UNIQUE,`salt` VARBINARY(255) NOT NULL,`status` TINYINT UNSIGNED NOT NULL DEFAULT 0,PRIMARY KEY(id))")
    cur.execute("CREATE TABLE IF NOT EXISTS `suggestee` (`id` INTEGER NOT NULL AUTO_INCREMENT,`category_id` INTEGER NOT NULL,`name` VARCHAR(255) NOT NULL,`last_updated` INTEGER NOT NULL,FOREIGN KEY(`category_id`) REFERENCES `suggestion_category`(`id`)ON DELETE CASCADE,PRIMARY KEY(`id`))")
    cur.execute("CREATE TABLE IF NOT EXISTS `tag` (`id` INTEGER NOT NULL AUTO_INCREMENT,`key` VARCHAR(255) NOT NULL,PRIMARY KEY(`id`))")
    cur.execute("CREATE TABLE IF NOT EXISTS `suggestee_tags` (`id` INTEGER NOT NULL AUTO_INCREMENT,`suggestee_id` INTEGER NOT NULL,`tag_id` INTEGER NOT NULL,`value` VARCHAR(255),FOREIGN KEY(`suggestee_id`) REFERENCES `suggestee`(`id`) ON DELETE CASCADE,FOREIGN KEY(`tag_id`) REFERENCES `tag`(`id`) ON DELETE CASCADE,UNIQUE(`suggestee_id`, `tag_id`),PRIMARY KEY(`id`))")
    cur.execute("CREATE TABLE IF NOT EXISTS `user_choice_history` ( `id` INTEGER NOT NULL AUTO_INCREMENT, `user_id` INTEGER NOT NULL, `suggestee_id` INTEGER NOT NULL, `timestamp` INTEGER UNSIGNED NOT NULL, `latitude` DOUBLE NOT NULL, `longitude` DOUBLE NOT NULL, FOREIGN KEY(`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE, FOREIGN KEY(`suggestee_id`) REFERENCES `suggestee`(`id`) ON DELETE CASCADE,PRIMARY KEY(`id`))")
    db.commit()

def insert_users(file_path):
  global db
  if db == None:
    db = MySQLdb.connect("localhost", "neva", "", "neva")
  with open(file_path, "r") as user_file:
    reader = csv.reader(user_file, delimiter=",")
    next(reader, None)
    with db.cursor() as cur:
      try:
        for id,email,salt,_ in reader:
          cur.execute(insert_user_query, (email, salt))
          user_dict[id] = int(cur.lastrowid)
        db.commit()
      except:
        db.rollback()

def insert_suggestees(file_path):
  global db
  if db == None:
    db = MySQLdb.connect("localhost", "neva", "", "neva")
  with open(file_path, "r") as suggestee_file:
    reader = csv.reader(suggestee_file, delimiter=",")
    next(reader, None)
    with db.cursor() as cur:
      try:
        for _,name,_ in reader:
          cur.execute(insert_suggestee_query, (name,))
          suggestee_dict[name]=int(cur.lastrowid)
        db.commit()
      except:
        db.rollback()

def insert_tags(file_path):
  global db
  if db == None:
    db = MySQLdb.connect("localhost", "neva", "", "neva")
  with open(file_path, "r") as tag_file:
    reader = csv.reader(tag_file, delimiter=",")
    next(reader, None)
    with db.cursor() as cur:
      try:
        for id, value in reader:
          cur.execute(insert_tag_query, (value,))
          tag_dict[id] = int(cur.lastrowid)
        db.commit()
      except:
        db.rollback()

def insert_suggestee_tags(file_path):
  global db
  if db==None:
    db = MySQLdb.connect("localhost", "neva", "", "neva")
  with open(file_path, "r") as suggestee_tag_file:
    reader = csv.reader(suggestee_tag_file, delimiter=",")
    next(reader, None)
    with db.cursor() as cur:
      try:
        for suggestee_name, tag_name, _ in reader:
          a = suggestee_dict[suggestee_name]
          b = tag_dict[tag_name]
          cur.execute(insert_suggestee_tags_query, (int(a),int(b)))
        db.commit()
      except Exception as e:
        print(e)
        db.rollback()
  
def insert_history(file_path):
  global db
  if db==None:
    db = MySQLdb.connect("localhost", "neva", "", "neva")
  with open(file_path, "r") as history_file:
    reader = csv.reader(history_file, delimiter=",")
    next(reader, None)
    with db.cursor() as cur:
      try:
        for user_id,suggestee_id,timestamp,latitude,longitude in reader:
          a = int(user_dict[user_id])
          b = int(suggestee_dict[suggestee_id])
          c = int(timestamp)
          d = float(latitude)
          e = float(longitude)
          cur.execute(insert_user_choice_history, (a,b,c,d,e))
        db.commit()
      except Exception as e:
        print(e)
        db.rollback()
print("Reset DB")
reset_db()
print("Insert Users")
insert_users("2014/users.txt")
print("Insert Suggestees")
insert_suggestees("2014/suggestee.txt")
print("Insert Tags")
insert_tags("2014/tag.txt")
print("Insert Suggestee_tags")
insert_suggestee_tags("2014/suggestee_tags.txt")
print("Insert History")
insert_history("2014/user_choice_history.txt")