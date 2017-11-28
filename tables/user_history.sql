USE `neva`;

/* Contains choice histories for users.
 * id is the unique identifier.
 * user_id shows to which user choice belongs to.
 * suggestee_id shows which suggestion the user chose.
 * timestamp holds the time information on choice in seconds.
 * latitude and longitude holds gps coordinates where the user made the choice.
 */
CREATE TABLE IF NOT EXISTS `user_choice_history` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `user_id` INTEGER NOT NULL,
  `suggestee_id` INTEGER NOT NULL,
  `timestamp` INTEGER UNSIGNED NOT NULL,
  `latitude` DOUBLE NOT NULL,
  `longitude` DOUBLE NOT NULL,
  FOREIGN KEY(`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY(`suggestee_id`) REFERENCES `suggestee`(`id`) ON DELETE CASCADE,
  PRIMARY KEY(`id`)
);
  
