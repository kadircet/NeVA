import MySQLdb


class Cacher:
    def __init__(self):
        self.db = MySQLdb.connect("localhost", "neva", "", "neva")
        self.db.autocommit(False)

    def UpdateUserCache(self, user_id, suggestee_ids):
        cur = self.db.cursor()
        try:
            cur.execute("DELETE FROM `recommender_cache` WHERE `user_id` = %s",
                        user_id)
            for suggestee_id in suggestee_ids:
                cur.execute(
                    "INSERT INTO `recommender_cache` (`user_id`, `suggestee_id`) VALUES (%s, %s)",
                    (user_id, suggestee_id))
            self.db.commit()
        except:
            self.db.rollback()
