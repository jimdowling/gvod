-- MySQL dump 10.13  Distrib 5.6.11-ndb-7.3.2, for linux-glibc2.5 (x86_64)
--
-- Host: localhost    Database: gvod
-- ------------------------------------------------------
-- Server version	5.6.11-ndb-7.3.2-cluster-gpl

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `nat_reports`
--

DROP TABLE IF EXISTS `nat_reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nat_reports` (
  `src_addr` varchar(64) NOT NULL,
  `src_nat` varchar(64) NOT NULL,
  `target_addr` varchar(64) NOT NULL,
  `target_nat` varchar(64) NOT NULL,
  `msg` varchar(255) NOT NULL,
  `success_count` int(11) NOT NULL DEFAULT '0',
  `fail_count` int(11) NOT NULL DEFAULT '0',
  `time_taken` int(11) NOT NULL DEFAULT '0',
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`src_addr`,`target_addr`,`time_taken`,`msg`),
  KEY `nat_idx` (`src_nat`,`target_nat`),
  KEY `src_idx` (`src_addr`,`target_addr`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nat_reports`
--

LOCK TABLES `nat_reports` WRITE;
/*!40000 ALTER TABLE `nat_reports` DISABLE KEYS */;
/*!40000 ALTER TABLE `nat_reports` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nodes`
--

DROP TABLE IF EXISTS `nodes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nodes` (
  `id` int(11) NOT NULL,
  `ip` int(10) unsigned NOT NULL,
  `port` smallint(5) unsigned NOT NULL,
  `asn` smallint(5) unsigned NOT NULL DEFAULT '0',
  `country` char(2) NOT NULL DEFAULT 'se',
  `nat_type` tinyint(3) unsigned NOT NULL,
  `open` tinyint(1) NOT NULL,
  `mtu` smallint(5) unsigned NOT NULL DEFAULT '1500',
  `last_ping` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `joined` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`id`),
  KEY `open_stable_idx` (`open`,`last_ping`),
  KEY `country_idx` (`country`),
  KEY `asn_idx` (`asn`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nodes`
--

LOCK TABLES `nodes` WRITE;
/*!40000 ALTER TABLE `nodes` DISABLE KEYS */;
/*!40000 ALTER TABLE `nodes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `overlay_details`
--

DROP TABLE IF EXISTS `overlay_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `overlay_details` (
  `overlay_id` int(11) NOT NULL,
  `overlay_name` char(128) NOT NULL,
  `overlay_description` varchar(512) DEFAULT NULL,
  `date_added` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `overlay_picture` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`overlay_id`),
  KEY `name_idx` (`overlay_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `overlay_details`
--

LOCK TABLES `overlay_details` WRITE;
/*!40000 ALTER TABLE `overlay_details` DISABLE KEYS */;
/*!40000 ALTER TABLE `overlay_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `overlays`
--

DROP TABLE IF EXISTS `overlays`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `overlays` (
  `id` int(11) NOT NULL,
  `overlay_id` int(11) NOT NULL,
  `utility` int(11) NOT NULL,
  PRIMARY KEY (`id`,`overlay_id`),
  KEY `overlay_idx` (`overlay_id`),
  KEY `utility_idx` (`utility`),
  CONSTRAINT `id_fk` FOREIGN KEY (`id`) REFERENCES `nodes` (`id`) ON DELETE CASCADE,
  CONSTRAINT `overlay_fk` FOREIGN KEY (`overlay_id`) REFERENCES `overlay_details` (`overlay_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `overlays`
--

LOCK TABLES `overlays` WRITE;
/*!40000 ALTER TABLE `overlays` DISABLE KEYS */;
/*!40000 ALTER TABLE `overlays` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `parents`
--

DROP TABLE IF EXISTS `parents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `parents` (
  `parent_id` int(10) unsigned NOT NULL,
  `id` int(11) NOT NULL,
  PRIMARY KEY (`parent_id`,`id`),
  KEY `id_idx` (`id`),
  CONSTRAINT `parents_ibfk_1` FOREIGN KEY (`id`) REFERENCES `nodes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parents`
--

LOCK TABLES `parents` WRITE;
/*!40000 ALTER TABLE `parents` DISABLE KEYS */;
/*!40000 ALTER TABLE `parents` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-01-28 13:12:21
