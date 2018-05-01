INSERT INTO `user` (`email`, `salt`) VALUES ("test", "test");
INSERT INTO `suggestion_category` (`name`) VALUES ("test");
INSERT INTO `suggestee` (`category_id`, `name`, `last_updated`) VALUES 
  (1, "test1", 0), (1, "test2", 0), (1, "test3", 0), (1, "test4", 0),
  (1, "test5", 0), (1, "test6", 0);
INSERT INTO `recommender_cache` (`user_id`, `suggestee_id`) VALUES
  (1, 1), (1, 2), (1, 3);
