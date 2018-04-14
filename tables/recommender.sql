CREATE TABLE IF NOT EXISTS `recommender_cache` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `user_id` INTEGER NOT NULL,
  `suggestee_id` INTEGER NOT NULL,
  FOREIGN KEY(`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY(`suggestee_id`) REFERENCES `suggestee`(`id`) ON DELETE CASCADE,
  PRIMARY KEY(`id`)
);
