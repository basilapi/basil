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
-- Indexes for table `ALIAS`
--
ALTER TABLE `ALIAS`
 ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `alias` (`alias`);

 --
 -- AUTO_INCREMENT for table `ALIAS`
 --
 ALTER TABLE `ALIAS`
 MODIFY `id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=1;


 --
 -- Constraints for table `ALIAS`
 --
 ALTER TABLE `ALIAS`
 ADD CONSTRAINT `alias_ibfk_1` FOREIGN KEY (`api`) REFERENCES `APIS` (`id`) ON DELETE CASCADE;
