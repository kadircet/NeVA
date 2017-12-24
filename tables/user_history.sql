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

/* Contains users' feedbacks on recommendations.
 * id is the unique identifier.
 * user_id shows to which user choice belongs to.
 * suggestee_id shows which suggestion the user chose.
 * last_choice_id points to the user's last history entry when that feedback was
   sent.
 * timestamp holds the time information on choice in seconds.
 * latitude and longitude holds gps coordinates where the user made the choice.
 * feedback holds the user's rating for the recommendation, it is of type
   `neva.backend.UserFeedback.Feedback`.
 */
CREATE TABLE IF NOT EXISTS `user_recommendation_feedback` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `user_id` INTEGER NOT NULL,
  `suggestee_id` INTEGER NOT NULL,
  `last_choice_id` INTEGER NOT NULL,
  `timestamp` INTEGER UNSIGNED NOT NULL,
  `latitude` DOUBLE NOT NULL,
  `longitude` DOUBLE NOT NULL,
  `feedback` INTEGER NOT NULL,
  FOREIGN KEY(`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY(`suggestee_id`) REFERENCES `suggestee`(`id`) ON DELETE CASCADE,
  FOREIGN KEY(`last_choice_id`) REFERENCES `user_choice_history`(`id`) 
    ON DELETE CASCADE,
  PRIMARY KEY(`id`)
);
