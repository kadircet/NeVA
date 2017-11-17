CREATE DATABASE IF NOT EXISTS `neva`
  DEFAULT CHARACTER SET utf8
  DEFAULT COLLATE utf8_general_ci;
USE `neva`;

CREATE USER IF NOT EXISTS 'neva'@'localhost';
GRANT ALL PRIVILEGES ON `neva`.* to 'neva'@'localhost';
FLUSH PRIVILEGES;

/* Contains base information for each user to be identified.
 * id is the unique identifier of a user.
 * email is the primary credential used for identification.
 * salt is the randomness associated with the user that will be used throughout
 *      all authentication system.
 * status is account status of user as defined in neva.backend.User.Status.
 */
CREATE TABLE IF NOT EXISTS `user` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(255) NOT NULL UNIQUE,
  `salt` VARCHAR(255) NOT NULL,
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY(id)
);

/* Contains profile information for each user.
 * id is the unique identifier of a user.
 * date_of_birth is the timestamp for user's birthday in seconds.
 * name is the real name of the user.
 * gender is the gender of a user as defined in neva.backend.User.Gender.
 * weight is the weight of the user in kg's.
 * photo is the url to a users profile photo.
 * register_data is the timestamp for user's account creation in seconds.
 */
CREATE TABLE IF NOT EXISTS `user_info` (
  `id` INTEGER NOT NULL,
  `date_of_birth` INTEGER UNSIGNED,
  `name` VARCHAR(255),
  `gender` TINYINT UNSIGNED,
  `weight` FLOAT,
  `photo` VARCHAR(255),
  `register_date` INTEGER UNSIGNED,
  FOREIGN KEY(`id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  PRIMARY KEY(`id`)
);

/* Contains session information for each connection.
 * A user might have logged in from more than one devices, therefore there can
 * be more than one token associated with a user.
 * id is the unique identifier of a user which token belongs.
 * token is a random key which authenticates a user.
 * expire is the expire date of the token in seconds.
 */
CREATE TABLE IF NOT EXISTS `user_session` (
  `id` INTEGER NOT NULL,
  `token` VARCHAR(255) NOT NULL UNIQUE,
  `expire` INTEGER UNSIGNED NOT NULL,
  FOREIGN KEY(`id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  PRIMARY KEY(`id`)
);

/* Contains default user_credentials that will be used for login.
 * id is the unique identifier of the user.
 * password field contains the hash of login key and salt.
 *          No matter what type of authentication mechanism has been used this
 *          field will hold the authentication key.
 */
CREATE TABLE IF NOT EXISTS `user_credential` (
  `id` INTEGER NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  FOREIGN KEY(`id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  PRIMARY KEY(`id`)
);

/* Contains the salted hash of tokens for linked social media accounts.
 * id is the unique identifier of the user.
 * token is the salted authentication token of a user obtained through OAuth.
 * social_media represents from which social_media app does the token
 *              originates as defined in
 *              neva.backend.LinkedAccount.SocialMediaType.
 */
CREATE TABLE IF NOT EXISTS `user_social_media` (
  `id` INTEGER NOT NULL,
  `token` VARCHAR(255) NOT NULL,
  `social_media` TINYINT UNSIGNED NOT NULL,
  FOREIGN KEY(`id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  PRIMARY KEY(`id`, `social_media`)
);


/* Contains the salted hash of verification tokens for user accounts.
 * It might be any request that uses user's email address to verify integrity.
 * id is the unique identifier of the user.
 * token is the salted verification token of a user generated with each request.
 * expire is the expire date of the token in seconds.
 */
CREATE TABLE IF NOT EXISTS `user_verification` (
  `id` INTEGER NOT NULL,
  `token` VARCHAR(255) NOT NULL,
  `expire` INTEGER UNSIGNED NOT NULL,
  FOREIGN KEY(`id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  PRIMARY KEY(`id`)
);
