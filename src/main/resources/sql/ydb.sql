SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `sys_company`;
CREATE TABLE `sys_company` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `expiration_time` bigint DEFAULT NULL,
  `status` int DEFAULT 0,
  `computer_check` int DEFAULT 0,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `deleted` int DEFAULT 0,
  `creator_id` bigint DEFAULT NULL,
  `editor_id` bigint DEFAULT NULL,
  `deleter_id` bigint DEFAULT NULL,
  `create_time` bigint DEFAULT NULL,
  `edit_time` bigint DEFAULT NULL,
  `delete_time` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `sys_company_permission`;
CREATE TABLE `sys_company_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `company_id` bigint DEFAULT NULL,
  `permission_id` bigint DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `sys_computer`;
CREATE TABLE `sys_computer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `device_id` varchar(255) DEFAULT NULL,
  `version` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `company_id` bigint DEFAULT NULL,
  `last_time` bigint DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `register` int DEFAULT 0,
  `note` varchar(255) DEFAULT NULL,
  `deleted` int DEFAULT 0,
  `creator_id` bigint DEFAULT NULL,
  `editor_id` bigint DEFAULT NULL,
  `deleter_id` bigint DEFAULT NULL,
  `create_time` bigint DEFAULT NULL,
  `edit_time` bigint DEFAULT NULL,
  `delete_time` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `sys_password`;
CREATE TABLE `sys_password` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `type` int DEFAULT NULL,
  `level` int DEFAULT NULL,
  `weight` int DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `enable` int DEFAULT 1,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_name` varchar(255) DEFAULT NULL,
  `company_id` bigint DEFAULT NULL,
  `role_id` bigint DEFAULT NULL,
  `nick_name` varchar(255) DEFAULT NULL,
  `firstSpell` varchar(255) DEFAULT NULL,
  `password_id` bigint DEFAULT NULL,
  `type` int DEFAULT 0,
  `note` varchar(255) DEFAULT NULL,
  `deleted` int DEFAULT 0,
  `creator_id` bigint DEFAULT NULL,
  `editor_id` bigint DEFAULT NULL,
  `deleter_id` bigint DEFAULT NULL,
  `create_time` bigint DEFAULT NULL,
  `edit_time` bigint DEFAULT NULL,
  `delete_time` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
