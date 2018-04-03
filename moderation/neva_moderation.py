from flask import Flask, render_template, logging, request, jsonify
from flask_mysqldb import MySQL
import numpy as np

app = Flask(__name__)

app.config['MYSQL_HOST'] = 'localhost'
app.config['MYSQL_USER'] = 'neva'
app.config['MYSQL_PASSWORD'] = ''
app.config['MYSQL_DB'] = 'neva'
app.config['MYSQL_CURSORCLASS'] = 'DictCursor'

mysql = MySQL(app)


def levenshtein(source, target):
    if len(source) < len(target):
        return levenshtein(target, source)

    # So now we have len(source) >= len(target).
    if len(target) == 0:
        return len(source)

    # We call tuple() to force strings to be used as sequences
    # ('c', 'a', 't', 's') - numpy uses them as values by default.
    source = np.array(tuple(source))
    target = np.array(tuple(target))

    # We use a dynamic programming algorithm, but with the
    # added optimization that we only need the last two rows
    # of the matrix.
    previous_row = np.arange(target.size + 1)
    for s in source:
        # Insertion (target grows longer than source):
        current_row = previous_row + 1

        # Substitution or matching:
        # Target and source items are aligned, and either
        # are different (cost of 1), or are the same (cost of 0).
        current_row[1:] = np.minimum(current_row[1:],
                                     np.add(previous_row[:-1], target != s))

        # Deletion (target grows shorter than source):
        current_row[1:] = np.minimum(current_row[1:], current_row[0:-1] + 1)

        previous_row = current_row

    return previous_row[-1]


def getClosestElements(element, elements):
    minimums = []
    min_lev = levenshtein(element, elements[0])
    min_name = elements[0]
    minimums.append((min_name, min_lev))

    for elem in elements:
        dl = levenshtein(element, elem)
        if (len(minimums) < 5):
            minimums.append((elem, dl))
        else:
            for (x, y) in minimums:
                if y > dl:
                    minimums.remove((x, y))
                    minimums.append((elem, dl))
                    break

    minimums.sort(key=lambda x: x[1])
    return minimums


def processMeals():
    meal_names = []
    suggestion_names = []
    results = []
    cur = mysql.connection.cursor()
    cur.execute("SELECT * FROM suggestee")
    data = cur.fetchall()
    for row in data:
        meal_names.append(row['name'].lower())

    cur.execute("SELECT * FROM item_suggestion")
    data = cur.fetchall()
    for row in data:
        suggestion_names.append((row['id'], row['suggestion']))
    cur.close()

    for table_id, name in suggestion_names:
        minimums = getClosestElements(name.lower(), meal_names)
        results.append({'meal': name, 'mins': minimums, 'table_id': table_id})

    results.sort(key=lambda x: x['meal'])
    return results


def processTags():
    tag_names = []
    suggestion_names = []
    results = []
    cur = mysql.connect.cursor()
    cur.execute("SELECT * FROM tag")
    data = cur.fetchall()
    for row in data:
        tag_names.append(row['key'].lower())
    cur.execute("SELECT * FROM tag_suggestion")
    data = cur.fetchall()
    for row in data:
        suggestion_names.append((row['id'], row['tag']))
    cur.close()

    for table_id, tag in suggestion_names:
        minimums = getClosestElements(tag.lower(), tag_names)
        results.append({'tag': tag, 'mins': minimums, 'table_id': table_id})

    results.sort(key=lambda x: x['tag'])
    return results


def processTVS():
    results = []
    cur = mysql.connection.cursor()
    cur.execute(
        "SELECT `tag_value_suggestion`.`id` as table_id, `suggestee_id`, `name`, `tag_id`, `key`\
  FROM  `tag_value_suggestion`, `suggestee`, `tag`\
  WHERE `suggestee_id`=`suggestee`.`id` AND `tag_id`=`tag`.`id`")
    data = cur.fetchall()
    return data


@app.route('/processTagValue', methods=['POST'])
def tvsPostReqHandler():
    if request.method == "POST":
        table_id = request.form['id']
        meal_id = request.form['meal_id']
        tag_id = request.form['tag_id']
        action = request.form['action']
        cur = mysql.connection.cursor()
        if (action == "acceptTagValue"):
            cur.execute(
                "INSERT INTO `suggestee_tags` (`suggestee_id`, `tag_id`) VALUES (%s, %s)",
                (meal_id, tag_id))
            cur.execute("SELECT MAX(`last_updated`) as la_up FROM `suggestee`")
            last_updated = cur.fetchone()['la_up'] + 1
            cur.execute(
                "UPDATE `suggestee` SET `last_updated`=%s WHERE `id`=%s",
                (last_updated, meal_id))
            cur.execute("DELETE FROM `tag_value_suggestion` WHERE `id`=%s",
                        (table_id, ))
            mysql.connection.commit()
            cur.close()
        elif (action == "rejectTagValue"):
            cur.execute("DELETE FROM `tag_value_suggestion` WHERE `id`=%s",
                        (table_id, ))
            mysql.connection.commit()
            cur.close()
        else:
            cur.close()
            return jsonify({'result': 'fail'})
        return jsonify({'result': 'success'})
    return jsonify({'result': 'fail'})


@app.route('/process', methods=['POST'])
def postReqHandler():
    if request.method == "POST":
        suggestion_id = request.form['id']
        suggestion_name = request.form['suggestion']
        action = request.form['action']
        cur = mysql.connection.cursor()
        if (action == "acceptMeal"):
            cur.execute("SELECT MAX(`last_updated`) as la_up FROM `suggestee`")
            last_updated = cur.fetchone()['la_up'] + 1
            cur.execute(
                "INSERT INTO suggestee (`category_id`, `name`, `last_updated`) values ( 1, %s, %s)",
                (suggestion_name, last_updated))
            cur.execute("DELETE FROM `item_suggestion` WHERE `id`=%s",
                        (suggestion_id, ))
            mysql.connection.commit()
            cur.close()
        elif (action == "rejectMeal"):
            cur.execute("DELETE FROM `item_suggestion` WHERE `id`=%s",
                        (suggestion_id, ))
            mysql.connection.commit()
            cur.close()
        elif (action == "acceptTag"):
            cur.execute("INSERT INTO `tag` (`key`) VALUES (%s)",
                        (suggestion_name, ))
            cur.execute("DELETE FROM `tag_suggestion` WHERE `id`=%s",
                        (suggestion_id, ))
            mysql.connection.commit()
            cur.close()
        elif (action == "rejectTag"):
            cur.execute("DELETE FROM `tag_suggestion` WHERE `id`=%s",
                        (suggestion_id, ))
            mysql.connection.commit()
            cur.close()
        else:
            cur.close()
            return jsonify({'result': 'fail'})
        return jsonify({'result': 'success'})
    return jsonify({'result': 'fail'})

@app.route('/processDbUpdate', methods=['POST'])
def updateDBHandler():
    if request.method == 'POST':
        id = request.form['id']
        name = request.form['name']
        cur = mysql.connection.cursor()
        cur.execute("SELECT MAX(`last_updated`) as la_up FROM `suggestee`")
        last_updated = cur.fetchone()['la_up'] + 1
        cur.execute("UPDATE `suggestee` SET `last_updated`=%s, `name`=%s WHERE `id`=%s", (last_updated, name, id))
        mysql.connection.commit()
        cur.close()
        return jsonify({'result':'success'})
    return jsonify({'result':'fail'})

@app.route('/processAdd', methods=['POST'])
def addNewStuff():
    if request.method == 'POST':
        action = request.form['action']
        cur = mysql.connection.cursor()
        if action == 'meal':
            meal_name = request.form['name']
            cur.execute("SELECT MAX(`last_updated`) as la_up FROM `suggestee`")
            last_updated = cur.fetchone()['la_up'] + 1
            cur.execute("INSERT INTO suggestee (`category_id`, `name`, `last_updated`) values ( 1, %s, %s)", (meal_name, last_updated))
        elif action == 'tag':
            tag_name = request.form['name']
            cur.execute("INSERT INTO `tag` (`key`) VALUES (%s)", (tag_name, ))
        elif action == 'tvs':
            meal_name = request.form['meal_name']
            tag_name = request.form['tag_name']
            cur.execute("INSERT INTO `suggestee_tags` (`suggestee_id`, `tag_id`)\
                         SELECT `suggestee`.`id`, `tag`.`id` FROM `suggestee`, `tag`\
                         WHERE `name`=%s AND `key`=%s", (meal_name, tag_name))
            cur.execute("SELECT MAX(`last_updated`) as la_up FROM `suggestee`")
            last_updated = cur.fetchone()['la_up'] + 1
            cur.execute("UPDATE `suggestee` SET `last_updated`=%s WHERE `name`=%s", (last_updated, meal_name))
        else:
            return jsonify({'result':'fail'})
        mysql.connection.commit()
        cur.close()
        return jsonify({'result':'success'})
    return jsonify({'result': 'fail'})
        

@app.route('/')
def main():

    meals = processMeals()
    tags = processTags()
    tvs = processTVS()
    return render_template(
        "meals.html", meal_rec=meals, tag_rec=tags, tvs_rec=tvs)

@app.route('/db')
def show_db():
    cur = mysql.connect.cursor()
    cur.execute("SELECT * FROM suggestee")
    meals = cur.fetchall()
    cur.execute("SELECT * FROM tag")
    tags = cur.fetchall()
    cur.execute("SELECT * FROM suggestee_tags")
    suggestee_tags = cur.fetchall()
    data = []
    for meal in meals:
        meal_id = meal['id']
        meal_name = meal['name']
        meal_tags = []
        for r in  suggestee_tags:
            if(r['suggestee_id'] == meal_id):
                for tag in tags:
                    if(tag['id'] == r['tag_id']):
                        meal_tags.append(tag['key'])
        data.append({'id': meal_id, 'name':meal_name, 'tags':meal_tags})
    return render_template("database_view.html", db_values=data)

if __name__ == "__main__":
    app.run()
