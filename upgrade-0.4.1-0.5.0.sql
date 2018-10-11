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
