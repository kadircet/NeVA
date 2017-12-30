USE `neva`;

/* Contains suggestion categories like food, wear, technology, etc.
 * It aims to provide flexibility to suggest different types of products in the
 * future.
 * id is the unique identifier of a category.
 * name is the name of the category.
 */
CREATE TABLE IF NOT EXISTS `suggestion_category` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL UNIQUE,
  PRIMARY KEY(`id`)
);

/* Contains individual suggestee's like "lahmacun".
 * These are the most basic units that will be suggested to users.
 * id is the unique identifier of each suggestee.
 * category_id shows to which category this suggestion belongs to.
 * name is the name of the suggestee like "lahmacun".
 */
CREATE TABLE IF NOT EXISTS `suggestee` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `category_id` INTEGER NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `last_updated` INTEGER NOT NULL,
  FOREIGN KEY(`category_id`) REFERENCES `suggestion_category`(`id`)
    ON DELETE CASCADE,
  PRIMARY KEY(`id`)
);

/* Contains tag names that can be associated with a suggestee.
 * It can be calories for a suggestee in food category or writer for a suggestee
 * in book category, or igredient information.
 * id is the unique identifier of each tag.
 * key is the name of the tag.
 */
CREATE TABLE IF NOT EXISTS `tag` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `key` VARCHAR(255) NOT NULL,
  PRIMARY KEY(`id`)
);

/* Contains suggestee-tag relations.
 * It is a one to at most one relation, a suggestee can have at most one tag
 * with the same tag_id.
 * id is unique identifier for the relation.
 * suggestee_id shows to which suggestee that relation belongs to.
 * tag_id shows to which tag that relation belongs to.
 * value field might hold specific information related with a given tag. It
 * might be empty for some tags.
 */
CREATE TABLE IF NOT EXISTS `suggestee_tags` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `suggestee_id` INTEGER NOT NULL,
  `tag_id` INTEGER NOT NULL,
  `value` VARCHAR(255),
  FOREIGN KEY(`suggestee_id`) REFERENCES `suggestee`(`id`) ON DELETE CASCADE,
  FOREIGN KEY(`tag_id`) REFERENCES `tag`(`id`) ON DELETE CASCADE,
  UNIQUE(`suggestee_id`, `tag_id`),
  PRIMARY KEY(`id`)
);

INSERT INTO `suggestion_category` (`name`) VALUES ("meal");
