INSERT INTO `user` (`email`, `salt`) VALUES ("test@gtest.com", "1");
INSERT INTO `user_credentials` (`user_id`, `credential`, `type`) VALUES ("0", "some_salted_hash", "1");
INSERT INTO `user_info` (`date_of_birth`, `name`, `gender`, `weight`, `photo`, `register_date`) VALUES ("0", "testuser", "0", "55", "AAAA", "0");
INSERT INTO `user_session` (`user_id`, `token`, `expire`) VALUES ("0", "TOKEN", "666");
INSERT INTO `user_social_media` (`token`, `social_media`) VALUES ("TOKEN", "1");
INSERT INTO `user_verification` (`token`, `expire`) VALUES ("TOKEN", "666");