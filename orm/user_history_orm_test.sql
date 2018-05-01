INSERT INTO `user` (`email`, `salt`) VALUES ("history_user@neva.com", "salt");
INSERT INTO `suggestion_category` (`name`) VALUES ("test_category");
INSERT INTO `suggestee` (`category_id`, `name`, `last_updated`) VALUES (1, "test_suggestee_1", 0);
INSERT INTO `suggestee` (`category_id`, `name`, `last_updated`) VALUES (1, "test_suggestee_2", 0);
INSERT INTO `user_choice_history` (`user_id`,`suggestee_id`, `timestamp`, `latitude`, `longitude`)
  VALUES (1, 1, 31313131, 39.89306792, 32.78321898);
INSERT INTO `user_choice_history` (`user_id`,`suggestee_id`, `timestamp`, `latitude`, `longitude`)
  VALUES (1, 1, 31313141, 39.89306792, 32.78321898);

INSERT INTO `user_choice_history` (`user_id`,`suggestee_id`, `timestamp`, `latitude`, `longitude`)
  VALUES (1, 2, 31313131, 39.89306792, 32.78321898);
INSERT INTO `user_choice_history` (`user_id`,`suggestee_id`, `timestamp`, `latitude`, `longitude`)
  VALUES (1, 2, 31313141, 39.89306792, 32.78321898);

INSERT INTO `user_recommendation_feedback` (`user_id`, `suggestee_id`, `last_choice_id`, `timestamp`,
  `latitude`, `longitude`, `feedback`) VALUES (1, 1, 1, 31313132, 39.89306792, 32.78321898, 1);

INSERT INTO `user_recommendation_feedback` (`user_id`, `suggestee_id`, `last_choice_id`, `timestamp`,
  `latitude`, `longitude`, `feedback`) VALUES (1, 1, 1, 31313132, 39.89306792, 32.78321898, 2);