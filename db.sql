-- MYSQL DATABASE 
--
--
--
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";
--
--
-- Database: `basil`
--

-- --------------------------------------------------------

--
-- Table structure for table `APIS`
--

CREATE TABLE `APIS` (
`id` int(11) NOT NULL,
  `nickname` varchar(255) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;


-- --------------------------------------------------------

--
-- Table structure for table `DATA`
--

CREATE TABLE `DATA` (
`id` int(11) NOT NULL,
  `api` int(11) NOT NULL,
  `property` varchar(255) NOT NULL,
  `value` text NOT NULL,
  `touched` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;


-- --------------------------------------------------------

--
-- Table structure for table `permissions`
--

CREATE TABLE `permissions` (
  `name` varchar(30) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `permissions`
--

INSERT INTO `permissions` (`name`) VALUES
('read'),
('write');

-- --------------------------------------------------------

--
-- Table structure for table `roles`
--

CREATE TABLE `roles` (
  `name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `roles_permissions`
--

CREATE TABLE `roles_permissions` (
  `role_name` varchar(255) NOT NULL,
  `permission` varchar(30) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `username` varchar(255) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `password` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `users_roles`
--

CREATE TABLE `users_roles` (
  `username` varchar(255) NOT NULL,
  `role_name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `VIEWS`
--

CREATE TABLE `VIEWS` (
`id` int(11) NOT NULL,
  `api` int(11) NOT NULL,
  `view` varchar(255) NOT NULL,
  `language` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `template` text NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;

-- --------------------------------------------------------
--
-- Table structure for table `ALIAS`
--

CREATE TABLE `ALIAS` (
  `id` int(11) NOT NULL,
  `api` int(11) NOT NULL,
  `alias` varchar(16) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;


--
-- Indexes for dumped tables
--

--
-- Indexes for table `APIS`
--
ALTER TABLE `APIS`
 ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `nickname` (`nickname`);

--
-- Indexes for table `DATA`
--
ALTER TABLE `DATA`
 ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `api_property` (`api`,`property`);

--
-- Indexes for table `permissions`
--
ALTER TABLE `permissions`
 ADD PRIMARY KEY (`name`);

--
-- Indexes for table `roles`
--
ALTER TABLE `roles`
 ADD PRIMARY KEY (`name`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
 ADD PRIMARY KEY (`username`);

--
-- Indexes for table `VIEWS`
--
ALTER TABLE `VIEWS`
 ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `api_view` (`api`,`view`);

--
-- Indexes for table `ALIAS`
--
ALTER TABLE `ALIAS`
 ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `alias` (`alias`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `APIS`
--
ALTER TABLE `APIS`
MODIFY `id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=6;
/*
 * Copyright (c) 2021. Enrico Daga and Luca Panziera
 *
 * MLicensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

--
-- AUTO_INCREMENT for table `DATA`
--
ALTER TABLE `DATA`
MODIFY `id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=13;
--
-- AUTO_INCREMENT for table `VIEWS`
--
ALTER TABLE `VIEWS`
MODIFY `id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `ALIAS`
--
ALTER TABLE `ALIAS`
MODIFY `id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=1;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `DATA`
--
ALTER TABLE `DATA`
ADD CONSTRAINT `data_ibfk_1` FOREIGN KEY (`api`) REFERENCES `APIS` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `VIEWS`
--
ALTER TABLE `VIEWS`
ADD CONSTRAINT `views_ibfk_1` FOREIGN KEY (`api`) REFERENCES `APIS` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `ALIAS`
--
ALTER TABLE `ALIAS`
ADD CONSTRAINT `alias_ibfk_1` FOREIGN KEY (`api`) REFERENCES `APIS` (`id`) ON DELETE CASCADE;
