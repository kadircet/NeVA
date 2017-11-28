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

/* Contains information on tags proposed by users.
 * id is the unique identifier of the proposition.
 * user_id is the id of the user who proposed it.
 * tag is the name of the tag proposed by the user.
 */
CREATE TABLE IF NOT EXISTS `tag_suggestion` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `user_id` INTEGER NOT NULL,
  `tag` VARCHAR(255) NOT NULL,
  FOREIGN KEY(`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  PRIMARY KEY(`id`)
);

/* Contains information on tag-item associations proposed by users.
 * id is the unique identifier of the proposition.
 * user_id is the id of the user who proposed it.
 * tag_id is id of the tag that participates in relation.
 * sugguestee_id is id of the item that particiaptes in relation.
 * value is propsed value for tag if exists.
 */
CREATE TABLE IF NOT EXISTS `tag_value_suggestion` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `user_id` INTEGER NOT NULL,
  `tag_id` INTEGER NOT NULL,
  `suggestee_id` INTEGER NOT NULL,
  `value` VARCHAR(255),
  FOREIGN KEY(`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY(`tag_id`) REFERENCES `tag`(`id`) ON DELETE CASCADE,
  FOREIGN KEY(`suggestee_id`) REFERENCES `suggestee`(`id`) ON DELETE CASCADE,
  PRIMARY KEY(`id`)
);
