USE `neva`;

/* Contains information on items suggested by users.
 * id is the unique identifier of the suggestion.
 * user_id is the id of the user who suggested given food.
 * category_id is the category to which suggestion has been made.
 * suggestion is the name of the item proposed by the user.
 */
CREATE TABLE IF NOT EXISTS `item_suggestion` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `user_id` INTEGER NOT NULL,
  `category_id` INTEGER NOT NULL,
  `suggestion` VARCHAR(255) NOT NULL,
  FOREIGN KEY(`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY(`category_id`) REFERENCES `suggestion_category`(`id`)
    ON DELETE CASCADE,
  PRIMARY KEY(`id`)
);
