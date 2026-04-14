-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: dam
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `name_unique` (`name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `composite_model_components`
--

DROP TABLE IF EXISTS `composite_model_components`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `composite_model_components` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `composite_model_id` bigint NOT NULL,
  `model_id` varchar(255) DEFAULT NULL,
  `position_x` double DEFAULT '0',
  `position_y` double DEFAULT '0',
  `position_z` double DEFAULT '0',
  `rotation_x` double DEFAULT '0',
  `rotation_y` double DEFAULT '0',
  `rotation_z` double DEFAULT '0',
  `scale_x` double DEFAULT '1',
  `scale_y` double DEFAULT '1',
  `scale_z` double DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `composite_model_components_ibfk_1` (`composite_model_id`),
  CONSTRAINT `composite_model_components_ibfk_1` FOREIGN KEY (`composite_model_id`) REFERENCES `composite_models` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=110 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `composite_model_components`
--

LOCK TABLES `composite_model_components` WRITE;
/*!40000 ALTER TABLE `composite_model_components` DISABLE KEYS */;
/*!40000 ALTER TABLE `composite_model_components` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `composite_models`
--

DROP TABLE IF EXISTS `composite_models`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `composite_models` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` text,
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=75 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `composite_models`
--

LOCK TABLES `composite_models` WRITE;
/*!40000 ALTER TABLE `composite_models` DISABLE KEYS */;
INSERT INTO `composite_models` VALUES (65,'c','c','user','2025-11-12 14:04:53','2025-11-12 14:04:53'),(66,'c','c','user','2025-11-12 14:04:53','2025-11-12 14:04:53'),(67,'j','k','user','2025-11-12 14:09:07','2025-11-12 14:09:07'),(68,'j','k','user','2025-11-12 14:09:08','2025-11-12 14:09:08'),(69,'run','run','user','2025-11-12 14:13:39','2025-11-12 14:13:39'),(70,'cv','cv','user','2025-11-12 14:17:24','2025-11-12 14:17:24'),(74,'q','ȥ','user','2025-12-02 16:45:57','2025-12-02 16:45:57');
/*!40000 ALTER TABLE `composite_models` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `models`
--

DROP TABLE IF EXISTS `models`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `models` (
  `id` int NOT NULL AUTO_INCREMENT,
  `category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `category` (`category`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=200000216 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `models`
--

LOCK TABLES `models` WRITE;
/*!40000 ALTER TABLE `models` DISABLE KEYS */;
INSERT INTO `models` VALUES (-200000040,'水务水利/电气与动力设施','配电柜.glb','src/main/resources/static/models/水务水利/电气与动力设施/配电柜.glb','这是一个自动导入的模型：配电柜.glb'),(-200000039,'水务水利/电气与动力设施','水利发电机.glb','src/main/resources/static/models/水务水利/电气与动力设施/水利发电机.glb','这是一个自动导入的模型：水利发电机.glb'),(-200000038,'水务水利/电气与动力设施','变频器.glb','src/main/resources/static/models/水务水利/电气与动力设施/变频器.glb','这是一个自动导入的模型：变频器.glb'),(-200000037,'水务水利/监测设备','雨量监测设备.glb','src/main/resources/static/models/水务水利/监测设备/雨量监测设备.glb','这是一个自动导入的模型：雨量监测设备.glb'),(-200000036,'水务水利/监测设备','水质检测站.glb','src/main/resources/static/models/水务水利/监测设备/水质检测站.glb','这是一个自动导入的模型：水质检测站.glb'),(-200000035,'水务水利/监测设备','水位监测设备.glb','src/main/resources/static/models/水务水利/监测设备/水位监测设备.glb','这是一个自动导入的模型：水位监测设备.glb'),(-200000034,'水务水利/水坝工程','重力坝.glb','src/main/resources/static/models/水务水利/水坝工程/重力坝.glb','这是一个自动导入的模型：重力坝.glb'),(-200000033,'水务水利/管道体系','管道2.glb','src/main/resources/static/models/水务水利/管道体系/管道2.glb','这是一个自动导入的模型：管道2.glb'),(-200000032,'水务水利/管道体系','管道1.glb','src/main/resources/static/models/水务水利/管道体系/管道1.glb','这是一个自动导入的模型：管道1.glb'),(-200000031,'水务水利/测量设备','测量设备3.glb','src/main/resources/static/models/水务水利/测量设备/测量设备3.glb','这是一个自动导入的模型：测量设备3.glb'),(-200000030,'水务水利/测量设备','测量设备2.glb','src/main/resources/static/models/水务水利/测量设备/测量设备2.glb','这是一个自动导入的模型：测量设备2.glb'),(-200000029,'水务水利/测量设备','测量设备1.glb','src/main/resources/static/models/水务水利/测量设备/测量设备1.glb','这是一个自动导入的模型：测量设备1.glb'),(-200000028,'水务水利/水泵设备','水泵4.glb','src/main/resources/static/models/水务水利/水泵设备/水泵4.glb','这是一个自动导入的模型：水泵4.glb'),(-200000027,'水务水利/水泵设备','水泵3.glb','src/main/resources/static/models/水务水利/水泵设备/水泵3.glb','这是一个自动导入的模型：水泵3.glb'),(-200000026,'水务水利/水泵设备','水泵2.glb','src/main/resources/static/models/水务水利/水泵设备/水泵2.glb','这是一个自动导入的模型：水泵2.glb'),(-200000025,'水务水利/水泵设备','水泵1.glb','src/main/resources/static/models/水务水利/水泵设备/水泵1.glb','这是一个自动导入的模型：水泵1.glb'),(-200000024,'水务水利/水坝工程','监测点.glb','src/main/resources/static/models/水务水利/水坝工程/监测点.glb','这是一个自动导入的模型：监测点.glb'),(-200000023,'水务水利/水坝工程','溢洪道.glb','src/main/resources/static/models/水务水利/水坝工程/溢洪道.glb','这是一个自动导入的模型：溢洪道glb'),(-200000022,'水务水利/水坝工程','水坝结构.glb','src/main/resources/static/models/水务水利/水坝工程/水坝结构.glb','这是一个自动导入的模型：水坝结构.glb'),(-200000021,'水务水利/水坝工程','桥.glb','src/main/resources/static/models/水务水利/水坝工程/桥.glb','这是一个自动导入的模型：桥.glb'),(-200000020,'水务水利/水坝工程','导流洞.glb','src/main/resources/static/models/水务水利/水坝工程/导流洞.glb','这是一个自动导入的模型：导流洞.glb'),(-200000019,'水务水利/水闸设备','液压启闭机.glb','src/main/resources/static/models/水务水利/水闸设备/液压启闭机.glb','这是一个自动导入的模型：液压启闭机glb'),(-200000018,'水务水利/水闸设备','水位标尺.glb','src/main/resources/static/models/水务水利/水闸设备/水位标尺.glb','这是一个自动导入的模型：水位标尺.glb'),(-200000017,'水务水利/水闸设备','弧形闸门.glb','src/main/resources/static/models/水务水利/水闸设备/弧形闸门.glb','这是一个自动导入的模型：弧形闸门.glb'),(-200000016,'水务水利/水闸设备','平面钢闸门.glb','src/main/resources/static/models/水务水利/水闸设备/平面钢闸门.glb','这是一个自动导入的模型：平面钢闸门.glb'),(-200000015,'水务水利/施工机械','运输卡车.glb','src/main/resources/static/models/水务水利/施工机械/运输卡车.glb','这是一个自动导入的模型：运输卡车.glb'),(-200000014,'水务水利/施工机械','混凝土泵车.glb','src/main/resources/static/models/水务水利/施工机械/混凝土泵车.glb','这是一个自动导入的模型：混凝土泵车.glb'),(-200000013,'水务水利/施工机械','推土机.glb','src/main/resources/static/models/水务水利/施工机械/推土机.glb','这是一个自动导入的模型：推土机.glb'),(-200000012,'水务水利/施工机械','挖掘机.glb','src/main/resources/static/models/水务水利/施工机械/挖掘机.glb','这是一个自动导入的模型：挖掘机.glb'),(-200000011,'水务水利/施工机械','吊车.glb','src/main/resources/static/models/水务水利/施工机械/吊车.glb','这是一个自动导入的模型：吊车.glb'),(-200000010,'水务水利/环境要素','河道地形.glb','src/main/resources/static/models/水务水利/环境要素/河道地形.glb','这是一个自动导入的模型：河道地形.glb'),(-200000009,'水务水利/环境要素','植被.glb','src/main/resources/static/models/水务水利/环境要素/植被.glb','这是一个自动导入的模型：植被.glb'),(-200000008,'水务水利/环境要素','办公楼.glb','src/main/resources/static/models/水务水利/环境要素/办公楼.glb','这是一个自动导入的模型：办公楼.glb'),(-200000007,'水务水利/环境要素','施工围栏.glb','src/main/resources/static/models/水务水利/环境要素/施工围栏.glb','这是一个自动导入的模型：施工围栏.glb'),(-200000006,'水务水利/环境要素','六角块护坡.glb','src/main/resources/static/models/水务水利/环境要素/六角块护坡.glb','这是一个自动导入的模型：六角块护坡.glb'),(-200000005,'水务水利/水处理设备','絮凝池.glb','src/main/resources/static/models/水务水利/水处理设备/絮凝池.glb','这是一个自动导入的模型：絮凝池.glb'),(-200000004,'水务水利/水处理设备','紫外线消毒通道.glb','src/main/resources/static/models/水务水利/水处理设备/紫外线消毒通道.glb','这是一个自动导入的模型：紫外线消毒通道.glb'),(-200000003,'水务水利/水处理设备','污泥脱水机.glb','src/main/resources/static/models/水务水利/水处理设备/污泥脱水机.glb','这是一个自动导入的模型：污泥脱水机.glb'),(-200000002,'水务水利/水处理设备','加药罐.glb','src/main/resources/static/models/水务水利/水处理设备/加药罐.glb','这是一个自动导入的模型：加药罐.glb'),(-200000001,'水务水利/水处理设备','V型滤池.glb','src/main/resources/static/models/水务水利/水处理设备/V型滤池.glb','这是一个自动导入的模型：V型滤池.glb'),(-164839423,'办公室','办公室.glb','src/main/resources/static/models/办公室/办公室.glb','这是一个自动导入的模型：办公室.glb'),(-164839422,'办公室','字画1.glb','src/main/resources/static/models/办公室/字画1.glb','这是一个自动导入的模型：字画1.glb'),(-164839421,'办公室','字画2.glb','src/main/resources/static/models/办公室/字画2.glb','这是一个自动导入的模型：字画2.glb'),(-164839420,'办公室','字画3.glb','src/main/resources/static/models/办公室/字画3.glb','这是一个自动导入的模型：字画3.glb'),(-164839419,'办公室','柜子.glb','src/main/resources/static/models/办公室/柜子.glb','这是一个自动导入的模型：柜子.glb'),(-164839418,'办公室','桌子.glb','src/main/resources/static/models/办公室/桌子.glb','这是一个自动导入的模型：桌子.glb'),(-164839417,'办公室','椅子1.glb','src/main/resources/static/models/办公室/椅子1.glb','这是一个自动导入的模型：椅子1.glb'),(-164839416,'办公室','椅子2.glb','src/main/resources/static/models/办公室/椅子2.glb','这是一个自动导入的模型：椅子2.glb'),(-164839415,'办公室','沙发1.glb','src/main/resources/static/models/办公室/沙发1.glb','这是一个自动导入的模型：沙发1.glb'),(-164839414,'办公室','沙发2.glb','src/main/resources/static/models/办公室/沙发2.glb','这是一个自动导入的模型：沙发2.glb'),(-164839413,'办公室','沙发3.glb','src/main/resources/static/models/办公室/沙发3.glb','这是一个自动导入的模型：沙发3.glb'),(-164839412,'办公室','沙发4.glb','src/main/resources/static/models/办公室/沙发4.glb','这是一个自动导入的模型：沙发4.glb'),(-164839411,'办公室','沙发5.glb','src/main/resources/static/models/办公室/沙发5.glb','这是一个自动导入的模型：沙发5.glb'),(-164839410,'办公室','盆栽1.glb','src/main/resources/static/models/办公室/盆栽1.glb','这是一个自动导入的模型：盆栽1.glb'),(-164839409,'办公室','盆栽2.glb','src/main/resources/static/models/办公室/盆栽2.glb','这是一个自动导入的模型：盆栽2.glb'),(-164839408,'办公室','盆栽3.glb','src/main/resources/static/models/办公室/盆栽3.glb','这是一个自动导入的模型：盆栽3.glb'),(-135479303,'教室','三角尺.glb','src/main/resources/static/models/教室/三角尺.glb','这是一个自动导入的模型：三角尺.glb'),(-135479302,'教室','书架.glb','src/main/resources/static/models/教室/书架.glb','这是一个自动导入的模型：书架.glb'),(-135479301,'教室','黑板.glb','src/main/resources/static/models/教室/黑板.glb','这是一个自动导入的模型：黑板.glb'),(-135479295,'办公室','花盆1.glb','src/main/resources/static/models/办公室/花盆1.glb','这是一个自动导入的模型：花盆1.glb'),(-135479294,'办公室','花盆2.glb','src/main/resources/static/models/办公室/花盆2.glb','这是一个自动导入的模型：花盆2.glb'),(-135479293,'办公室','花盆3.glb','src/main/resources/static/models/办公室/花盆3.glb','这是一个自动导入的模型：花盆3.glb'),(-135479292,'实验室','剪子.glb','src/main/resources/static/models/实验室/剪子.glb','这是一个自动导入的模型：剪子.glb'),(-135479291,'实验室','吸瓶.glb','src/main/resources/static/models/实验室/吸瓶.glb','这是一个自动导入的模型：吸瓶.glb'),(-135479290,'实验室','吸附棉.glb','src/main/resources/static/models/实验室/吸附棉.glb','这是一个自动导入的模型：吸附棉.glb'),(-135479289,'实验室','天平.glb','src/main/resources/static/models/实验室/天平.glb','这是一个自动导入的模型：天平.glb'),(-135479288,'实验室','实验台.glb','src/main/resources/static/models/实验室/实验台.glb','这是一个自动导入的模型：实验台.glb'),(-135479287,'实验室','废液桶.glb','src/main/resources/static/models/实验室/废液桶.glb','这是一个自动导入的模型：废液桶.glb'),(-135479286,'实验室','废液桶托盘.glb','src/main/resources/static/models/实验室/废液桶托盘.glb','这是一个自动导入的模型：废液桶托盘.glb'),(-135479285,'实验室','手套.glb','src/main/resources/static/models/实验室/手套.glb','这是一个自动导入的模型：手套.glb'),(-135479284,'实验室','托盘.glb','src/main/resources/static/models/实验室/托盘.glb','这是一个自动导入的模型：托盘.glb'),(-135479283,'实验室','振荡器.glb','src/main/resources/static/models/实验室/振荡器.glb','这是一个自动导入的模型：振荡器.glb'),(-135479282,'实验室','易制毒试剂柜.glb','src/main/resources/static/models/实验室/易制毒试剂柜.glb','这是一个自动导入的模型：易制毒试剂柜.glb'),(-135479281,'实验室','显微镜.glb','src/main/resources/static/models/实验室/显微镜.glb','这是一个自动导入的模型：显微镜.glb'),(-135479280,'实验室','普通试剂柜.glb','src/main/resources/static/models/实验室/普通试剂柜.glb','这是一个自动导入的模型：普通试剂柜.glb'),(-135479279,'实验室','水浴锅.glb','src/main/resources/static/models/实验室/水浴锅.glb','这是一个自动导入的模型：水浴锅.glb'),(-135479278,'实验室','洗眼器.glb','src/main/resources/static/models/实验室/洗眼器.glb','这是一个自动导入的模型：洗眼器.glb'),(-135479277,'实验室','消防柜.glb','src/main/resources/static/models/实验室/消防柜.glb','这是一个自动导入的模型：消防柜.glb'),(-135479276,'实验室','滴管.glb','src/main/resources/static/models/实验室/滴管.glb','这是一个自动导入的模型：滴管.glb'),(-135479275,'实验室','漏斗.glb','src/main/resources/static/models/实验室/漏斗.glb','这是一个自动导入的模型：漏斗.glb'),(-135479274,'实验室','灭火器.glb','src/main/resources/static/models/实验室/灭火器.glb','这是一个自动导入的模型：灭火器.glb'),(-135479273,'实验室','灭菌锅.glb','src/main/resources/static/models/实验室/灭菌锅.glb','这是一个自动导入的模型：灭菌锅.glb'),(-135479272,'实验室','烘箱.glb','src/main/resources/static/models/实验室/烘箱.glb','这是一个自动导入的模型：烘箱.glb'),(-135479271,'实验室','烧杯.glb','src/main/resources/static/models/实验室/烧杯.glb','这是一个自动导入的模型：烧杯.glb'),(-135479270,'实验室','玻璃棒.glb','src/main/resources/static/models/实验室/玻璃棒.glb','这是一个自动导入的模型：玻璃棒.glb'),(-135479269,'实验室','研钵、研磨棒.glb','src/main/resources/static/models/实验室/研钵、研磨棒.glb','这是一个自动导入的模型：研钵、研磨棒.glb'),(-135479268,'实验室','离心机.glb','src/main/resources/static/models/实验室/离心机.glb','这是一个自动导入的模型：离心机.glb'),(-135479267,'实验室','离心管.glb','src/main/resources/static/models/实验室/离心管.glb','这是一个自动导入的模型：离心管.glb'),(-135479266,'实验室','称量纸.glb','src/main/resources/static/models/实验室/称量纸.glb','这是一个自动导入的模型：称量纸.glb'),(-135479265,'实验室','药勺.glb','src/main/resources/static/models/实验室/药勺.glb','这是一个自动导入的模型：药勺.glb'),(-135479264,'实验室','试剂瓶.glb','src/main/resources/static/models/实验室/试剂瓶.glb','这是一个自动导入的模型：试剂瓶.glb'),(-135479263,'实验室','试管.glb','src/main/resources/static/models/实验室/试管.glb','这是一个自动导入的模型：试管.glb'),(-135479262,'实验室','试管架.glb','src/main/resources/static/models/实验室/试管架.glb','这是一个自动导入的模型：试管架.glb'),(-135479261,'实验室','超净台.glb','src/main/resources/static/models/实验室/超净台.glb','这是一个自动导入的模型：超净台.glb'),(-135479260,'实验室','通风橱.glb','src/main/resources/static/models/实验室/通风橱.glb','这是一个自动导入的模型：通风橱.glb'),(-135479259,'实验室','酒精桶.glb','src/main/resources/static/models/实验室/酒精桶.glb','这是一个自动导入的模型：酒精桶.glb'),(-135479258,'实验室','酒精灯.glb','src/main/resources/static/models/实验室/酒精灯.glb','这是一个自动导入的模型：酒精灯.glb'),(-135479257,'实验室','防爆冰箱.glb','src/main/resources/static/models/实验室/防爆冰箱.glb','这是一个自动导入的模型：防爆冰箱.glb'),(-135479256,'实验室','马弗炉.glb','src/main/resources/static/models/实验室/马弗炉.glb','这是一个自动导入的模型：马弗炉.glb'),(-135479255,'律师所','储物柜.glb','src/main/resources/static/models/律师所/储物柜.glb','这是一个自动导入的模型：储物柜.glb'),(-135479254,'律师所','宣传牌.glb','src/main/resources/static/models/律师所/宣传牌.glb','这是一个自动导入的模型：宣传牌.glb'),(-135479253,'律师所','导览牌.glb','src/main/resources/static/models/律师所/导览牌.glb','这是一个自动导入的模型：导览牌.glb'),(-135479252,'律师所','标识牌.glb','src/main/resources/static/models/律师所/标识牌.glb','这是一个自动导入的模型：标识牌.glb'),(-135479251,'律师所','椅子.glb','src/main/resources/static/models/律师所/椅子.glb','这是一个自动导入的模型：椅子.glb'),(-135479250,'律师所','电脑.glb','src/main/resources/static/models/律师所/电脑.glb','这是一个自动导入的模型：电脑.glb'),(-135479249,'律师所','询问台.glb','src/main/resources/static/models/律师所/询问台.glb','这是一个自动导入的模型：询问台.glb'),(-135479248,'律师所','长椅.glb','src/main/resources/static/models/律师所/长椅.glb','这是一个自动导入的模型：长椅.glb'),(-135479246,'教室','book53.glb','src/main/resources/static/models/教室/book53.glb','这是一个自动导入的模型：book53.glb'),(-135479245,'教室','broom01_1.glb','src/main/resources/static/models/教室/broom01_1.glb','这是一个自动导入的模型：broom01_1.glb'),(-135479244,'教室','broom01_2.glb','src/main/resources/static/models/教室/broom01_2.glb','这是一个自动导入的模型：broom01_2.glb'),(-135479243,'教室','chair01.glb','src/main/resources/static/models/教室/chair01.glb','这是一个自动导入的模型：chair01.glb'),(-135479242,'教室','desk02.glb','src/main/resources/static/models/教室/desk02.glb','这是一个自动导入的模型：desk02.glb'),(-135479241,'教室','lamp01_1.glb','src/main/resources/static/models/教室/lamp01_1.glb','这是一个自动导入的模型：lamp01_1.glb'),(-135479240,'教室','lamp02.glb','src/main/resources/static/models/教室/lamp02.glb','这是一个自动导入的模型：lamp02.glb'),(-135479239,'教室','projector01.glb','src/main/resources/static/models/教室/projector01.glb','这是一个自动导入的模型：projector01.glb'),(-135479238,'教室','screen01_1.glb','src/main/resources/static/models/教室/screen01_1.glb','这是一个自动导入的模型：screen01_1.glb'),(-135479237,'教室','screen01_2.glb','src/main/resources/static/models/教室/screen01_2.glb','这是一个自动导入的模型：screen01_2.glb'),(-135479236,'教室','trashbox01.glb','src/main/resources/static/models/教室/trashbox01.glb','这是一个自动导入的模型：trashbox01.glb'),(-135479235,'教室','tuoba.glb','src/main/resources/static/models/教室/tuoba.glb','这是一个自动导入的模型：tuoba.glb'),(-135479234,'服装仓库','主机.glb','src/main/resources/static/models/服装仓库/主机.glb','这是一个自动导入的模型：主机.glb'),(-135479233,'服装仓库','传送带.glb','src/main/resources/static/models/服装仓库/传送带.glb','这是一个自动导入的模型：传送带.glb'),(-135479232,'服装仓库','分拣车.glb','src/main/resources/static/models/服装仓库/分拣车.glb','这是一个自动导入的模型：分拣车.glb'),(-135479231,'服装仓库','包装盒.glb','src/main/resources/static/models/服装仓库/包装盒.glb','这是一个自动导入的模型：包装盒.glb'),(-135479230,'服装仓库','塑料周转箱.glb','src/main/resources/static/models/服装仓库/塑料周转箱.glb','这是一个自动导入的模型：塑料周转箱.glb'),(-135479229,'服装仓库','夹克.glb','src/main/resources/static/models/服装仓库/夹克.glb','这是一个自动导入的模型：夹克.glb'),(-135479228,'服装仓库','平板车.glb','src/main/resources/static/models/服装仓库/平板车.glb','这是一个自动导入的模型：平板车.glb'),(-135479227,'服装仓库','手推车.glb','src/main/resources/static/models/服装仓库/手推车.glb','这是一个自动导入的模型：手推车.glb'),(-135479226,'服装仓库','打包机.glb','src/main/resources/static/models/服装仓库/打包机.glb','这是一个自动导入的模型：打包机.glb'),(-135479225,'服装仓库','扫码枪.glb','src/main/resources/static/models/服装仓库/扫码枪.glb','这是一个自动导入的模型：扫码枪.glb'),(-135479224,'服装仓库','播种墙.glb','src/main/resources/static/models/服装仓库/播种墙.glb','这是一个自动导入的模型：播种墙.glb'),(-135479223,'服装仓库','显示器.glb','src/main/resources/static/models/服装仓库/显示器.glb','这是一个自动导入的模型：显示器.glb'),(-135479222,'服装仓库','标识牌a.glb','src/main/resources/static/models/服装仓库/标识牌a.glb','这是一个自动导入的模型：标识牌a.glb'),(-135479221,'服装仓库','标识牌b.glb','src/main/resources/static/models/服装仓库/标识牌b.glb','这是一个自动导入的模型：标识牌b.glb'),(-135479220,'服装仓库','标识牌c.glb','src/main/resources/static/models/服装仓库/标识牌c.glb','这是一个自动导入的模型：标识牌c.glb'),(-135479219,'服装仓库','标识牌cc.glb','src/main/resources/static/models/服装仓库/标识牌cc.glb','这是一个自动导入的模型：标识牌cc.glb'),(-135479218,'服装仓库','标识牌d.glb','src/main/resources/static/models/服装仓库/标识牌d.glb','这是一个自动导入的模型：标识牌d.glb'),(-135479217,'服装仓库','标识牌dbc.glb','src/main/resources/static/models/服装仓库/标识牌dbc.glb','这是一个自动导入的模型：标识牌dbc.glb'),(-135479216,'服装仓库','标识牌ddc.glb','src/main/resources/static/models/服装仓库/标识牌ddc.glb','这是一个自动导入的模型：标识牌ddc.glb'),(-135479215,'服装仓库','标识牌fh.glb','src/main/resources/static/models/服装仓库/标识牌fh.glb','这是一个自动导入的模型：标识牌fh.glb'),(-135479214,'服装仓库','标识牌hj.glb','src/main/resources/static/models/服装仓库/标识牌hj.glb','这是一个自动导入的模型：标识牌hj.glb'),(-135479213,'服装仓库','标识牌rcbz.glb','src/main/resources/static/models/服装仓库/标识牌rcbz.glb','这是一个自动导入的模型：标识牌rcbz.glb'),(-135479212,'服装仓库','衬衣.glb','src/main/resources/static/models/服装仓库/衬衣.glb','这是一个自动导入的模型：衬衣.glb'),(-135479211,'服装仓库','裙子.glb','src/main/resources/static/models/服装仓库/裙子.glb','这是一个自动导入的模型：裙子.glb'),(-135479210,'服装仓库','裤子.glb','src/main/resources/static/models/服装仓库/裤子.glb','这是一个自动导入的模型：裤子.glb'),(-135479209,'服装仓库','西装.glb','src/main/resources/static/models/服装仓库/西装.glb','这是一个自动导入的模型：西装.glb'),(-135479208,'服装仓库','西裤.glb','src/main/resources/static/models/服装仓库/西裤.glb','这是一个自动导入的模型：西裤.glb'),(-135479207,'服装仓库','货架.glb','src/main/resources/static/models/服装仓库/货架.glb','这是一个自动导入的模型：货架.glb'),(-135479206,'服装仓库','运输.glb','src/main/resources/static/models/服装仓库/运输.glb','这是一个自动导入的模型：运输.glb'),(-135479205,'服装仓库','键盘.glb','src/main/resources/static/models/服装仓库/键盘.glb','这是一个自动导入的模型：键盘.glb'),(-135479204,'服装仓库','鼠标.glb','src/main/resources/static/models/服装仓库/鼠标.glb','这是一个自动导入的模型：鼠标.glb'),(-135479203,'港口码头','a1.glb','src/main/resources/static/models/港口码头/a1.glb','这是一个自动导入的模型：a1.glb'),(-135479202,'港口码头','a10.glb','src/main/resources/static/models/港口码头/a10.glb','这是一个自动导入的模型：a10.glb'),(-135479201,'港口码头','a11.glb','src/main/resources/static/models/港口码头/a11.glb','这是一个自动导入的模型：a11.glb'),(-135479200,'港口码头','a2.glb','src/main/resources/static/models/港口码头/a2.glb','这是一个自动导入的模型：a2.glb'),(-135479199,'港口码头','a3.glb','src/main/resources/static/models/港口码头/a3.glb','这是一个自动导入的模型：a3.glb'),(-135479198,'港口码头','a4.glb','src/main/resources/static/models/港口码头/a4.glb','这是一个自动导入的模型：a4.glb'),(-135479197,'港口码头','a5.glb','src/main/resources/static/models/港口码头/a5.glb','这是一个自动导入的模型：a5.glb'),(-135479196,'港口码头','a6.glb','src/main/resources/static/models/港口码头/a6.glb','这是一个自动导入的模型：a6.glb'),(-135479195,'港口码头','a7.glb','src/main/resources/static/models/港口码头/a7.glb','这是一个自动导入的模型：a7.glb'),(-135479194,'港口码头','a8.glb','src/main/resources/static/models/港口码头/a8.glb','这是一个自动导入的模型：a8.glb'),(-135479193,'港口码头','a9.glb','src/main/resources/static/models/港口码头/a9.glb','这是一个自动导入的模型：a9.glb'),(-135479192,'港口码头','b.glb','src/main/resources/static/models/港口码头/b.glb','这是一个自动导入的模型：b.glb'),(-135479191,'港口码头','b1.glb','src/main/resources/static/models/港口码头/b1.glb','这是一个自动导入的模型：b1.glb'),(-135479190,'港口码头','baowen_c.glb','src/main/resources/static/models/港口码头/baowen_c.glb','这是一个自动导入的模型：baowen_c.glb'),(-135479189,'港口码头','c.glb','src/main/resources/static/models/港口码头/c.glb','这是一个自动导入的模型：c.glb'),(-135479188,'港口码头','ContainerSpreader_6m_01.glb','src/main/resources/static/models/港口码头/ContainerSpreader_6m_01.glb','这是一个自动导入的模型：ContainerSpreader_6m_01.glb'),(-135479187,'港口码头','CraneCable_5m_01.glb','src/main/resources/static/models/港口码头/CraneCable_5m_01.glb','这是一个自动导入的模型：CraneCable_5m_01.glb'),(-135479186,'港口码头','GantryCrane_01_Traction_01.glb','src/main/resources/static/models/港口码头/GantryCrane_01_Traction_01.glb','这是一个自动导入的模型：GantryCrane_01_Traction_01.glb'),(-135479185,'港口码头','GantryCrane_02_Structure_01.glb','src/main/resources/static/models/港口码头/GantryCrane_02_Structure_01.glb','这是一个自动导入的模型：GantryCrane_02_Structure_01.glb'),(-135479184,'港口码头','GantryCrane_02_Traction_01.glb','src/main/resources/static/models/港口码头/GantryCrane_02_Traction_01.glb','这是一个自动导入的模型：GantryCrane_02_Traction_01.glb'),(-135479183,'港口码头','GantryCrane_02_Trolley_01.glb','src/main/resources/static/models/港口码头/GantryCrane_02_Trolley_01.glb','这是一个自动导入的模型：GantryCrane_02_Trolley_01.glb'),(-135479182,'港口码头','guashi_c.glb','src/main/resources/static/models/港口码头/guashi_c.glb','这是一个自动导入的模型：guashi_c.glb'),(-135479181,'港口码头','kaiding_c.glb','src/main/resources/static/models/港口码头/kaiding_c.glb','这是一个自动导入的模型：kaiding_c.glb'),(-135479180,'港口码头','lengcang_c.glb','src/main/resources/static/models/港口码头/lengcang_c.glb','这是一个自动导入的模型：lengcang_c.glb'),(-135479179,'港口码头','qiaoxingqizhongji.glb','src/main/resources/static/models/港口码头/qiaoxingqizhongji.glb','这是一个自动导入的模型：qiaoxingqizhongji.glb'),(-135479178,'港口码头','qizhongji.glb','src/main/resources/static/models/港口码头/qizhongji.glb','这是一个自动导入的模型：qizhongji.glb'),(-135479177,'港口码头','ShipyardCrane_Bottom_01.glb','src/main/resources/static/models/港口码头/ShipyardCrane_Bottom_01.glb','这是一个自动导入的模型：ShipyardCrane_Bottom_01.glb'),(-135479176,'港口码头','ShipyardCrane_Cabin_01.glb','src/main/resources/static/models/港口码头/ShipyardCrane_Cabin_01.glb','这是一个自动导入的模型：ShipyardCrane_Cabin_01.glb'),(-135479175,'港口码头','ShipyardCrane_Front_01.glb','src/main/resources/static/models/港口码头/ShipyardCrane_Front_01.glb','这是一个自动导入的模型：ShipyardCrane_Front_01.glb'),(-135479174,'港口码头','ShipyardCrane_Shaft_01.glb','src/main/resources/static/models/港口码头/ShipyardCrane_Shaft_01.glb','这是一个自动导入的模型：ShipyardCrane_Shaft_01.glb'),(-135479173,'港口码头','ShipyardCrane_Top_01.glb','src/main/resources/static/models/港口码头/ShipyardCrane_Top_01.glb','这是一个自动导入的模型：ShipyardCrane_Top_01.glb'),(-135479172,'港口码头','中型货架.glb','src/main/resources/static/models/港口码头/中型货架.glb','这是一个自动导入的模型：中型货架.glb'),(-135479171,'港口码头','传送带.glb','src/main/resources/static/models/港口码头/传送带.glb','这是一个自动导入的模型：传送带.glb'),(-135479170,'港口码头','卡车.glb','src/main/resources/static/models/港口码头/卡车.glb','这是一个自动导入的模型：卡车.glb'),(-135479169,'港口码头','叉车.glb','src/main/resources/static/models/港口码头/叉车.glb','这是一个自动导入的模型：叉车.glb'),(-135479168,'港口码头','托盘.glb','src/main/resources/static/models/港口码头/托盘.glb','这是一个自动导入的模型：托盘.glb'),(-135479167,'港口码头','机械臂.glb','src/main/resources/static/models/港口码头/机械臂.glb','这是一个自动导入的模型：机械臂.glb'),(-135479166,'港口码头','海警船glb.glb','src/main/resources/static/models/港口码头/海警船glb.glb','这是一个自动导入的模型：海警船glb.glb'),(-135479165,'港口码头','液压抓斗.glb','src/main/resources/static/models/港口码头/液压抓斗.glb','这是一个自动导入的模型：液压抓斗.glb'),(-135479164,'港口码头','砂船.glb','src/main/resources/static/models/港口码头/砂船.glb','这是一个自动导入的模型：砂船.glb'),(-135479163,'港口码头','货船.glb','src/main/resources/static/models/港口码头/货船.glb','这是一个自动导入的模型：货船.glb'),(-135479162,'港口码头','货船2.glb','src/main/resources/static/models/港口码头/货船2.glb','这是一个自动导入的模型：货船2.glb'),(-135479161,'港口码头','轻型货架.glb','src/main/resources/static/models/港口码头/轻型货架.glb','这是一个自动导入的模型：轻型货架.glb'),(-135479160,'港口码头','重型货架.glb','src/main/resources/static/models/港口码头/重型货架.glb','这是一个自动导入的模型：重型货架.glb'),(-135479159,'港口码头','集装箱1.glb','src/main/resources/static/models/港口码头/集装箱1.glb','这是一个自动导入的模型：集装箱1.glb'),(-135479158,'港口码头','集装箱2.glb','src/main/resources/static/models/港口码头/集装箱2.glb','这是一个自动导入的模型：集装箱2.glb'),(-135479157,'港口码头','集装箱3.glb','src/main/resources/static/models/港口码头/集装箱3.glb','这是一个自动导入的模型：集装箱3.glb'),(-135479156,'港口码头','集装箱4.glb','src/main/resources/static/models/港口码头/集装箱4.glb','这是一个自动导入的模型：集装箱4.glb'),(-135479155,'港口码头','集装箱5.glb','src/main/resources/static/models/港口码头/集装箱5.glb','这是一个自动导入的模型：集装箱5.glb'),(200000041,'水务水利/监测与自动化设备','PH计.glb','src/main/resources/static/models/水务水利/监测与自动化设备/PH计.glb','这是一个自动导入的模型：PH计.glb'),(200000042,'水务水利/监测与自动化设备','压力传感器.glb','src/main/resources/static/models/水务水利/监测与自动化设备/压力传感器.glb','这是一个自动导入的模型：压力传感器.glb'),(200000043,'水务水利/监测与自动化设备','操作台.glb','src/main/resources/static/models/水务水利/监测与自动化设备/操作台.glb','这是一个自动导入的模型：操作台.glb'),(200000044,'水务水利/监测与自动化设备','浊度计.glb','src/main/resources/static/models/水务水利/监测与自动化设备/浊度计.glb','这是一个自动导入的模型：浊度计.glb'),(200000045,'水务水利/监测与自动化设备','浮子水位计.glb','src/main/resources/static/models/水务水利/监测与自动化设备/浮子水位计.glb','这是一个自动导入的模型：浮子水位计.glb'),(200000046,'水务水利/监测与自动化设备','溶氧仪.glb','src/main/resources/static/models/水务水利/监测与自动化设备/溶氧仪.glb','这是一个自动导入的模型：溶氧仪.glb'),(200000047,'水务水利/监测与自动化设备','电磁流量计.glb','src/main/resources/static/models/水务水利/监测与自动化设备/电磁流量计.glb','这是一个自动导入的模型：电磁流量计.glb'),(200000048,'水务水利/监测与自动化设备','自动化控制柜.glb','src/main/resources/static/models/水务水利/监测与自动化设备/自动化控制柜.glb','这是一个自动导入的模型：自动化控制柜.glb'),(200000049,'水务水利/监测与自动化设备','视频监控摄像头.glb','src/main/resources/static/models/水务水利/监测与自动化设备/视频监控摄像头.glb','这是一个自动导入的模型：视频监控摄像头.glb'),(200000050,'水务水利/监测与自动化设备','超声波水位计.glb','src/main/resources/static/models/水务水利/监测与自动化设备/超声波水位计.glb','这是一个自动导入的模型：超声波水位计.glb'),(200000051,'水务水利/监测与自动化设备','超声波流量计.glb','src/main/resources/static/models/水务水利/监测与自动化设备/超声波流量计.glb','这是一个自动导入的模型：超声波流量计.glb'),(200000052,'水务水利/监测与自动化设备','雷达水位计.glb','src/main/resources/static/models/水务水利/监测与自动化设备/雷达水位计.glb','这是一个自动导入的模型：雷达水位计.glb'),(200000053,'水务水利/水闸设备','叠梁闸门.glb','src/main/resources/static/models/水务水利/水闸设备/叠梁闸门.glb','这是一个自动导入的模型：叠梁闸门.glb'),(200000054,'水务水利/水闸设备','蝶阀.glb','src/main/resources/static/models/水务水利/水闸设备/蝶阀.glb','这是一个自动导入的模型：蝶阀.glb'),(200000055,'水务水利/水闸设备','拍门.glb','src/main/resources/static/models/水务水利/水闸设备/拍门.glb','这是一个自动导入的模型：拍门.glb'),(200000056,'水务水利/水泵设备','泵房.glb','src/main/resources/static/models/水务水利/水泵设备/泵房.glb','这是一个自动导入的模型：泵房.glb'),(200000057,'水务水利/水泵设备','离心泵.glb','src/main/resources/static/models/水务水利/水泵设备/离心泵.glb','这是一个自动导入的模型：离心泵.glb'),(200000058,'水务水利/水泵设备','潜水泵.glb','src/main/resources/static/models/水务水利/水泵设备/潜水泵.glb','这是一个自动导入的模型：潜水泵.glb'),(200000059,'水务水利/水泵设备','轴流泵.glb','src/main/resources/static/models/水务水利/水泵设备/轴流泵.glb','这是一个自动导入的模型：轴流泵.glb'),(200000060,'水务水利/水闸设备','电动启闭机.glb','src/main/resources/static/models/水务水利/水闸设备/电动启闭机.glb','这是一个自动导入的模型：电动启闭机.glb'),(200000061,'水务水利/水闸设备','卷扬式启闭机.glb','src/main/resources/static/models/水务水利/水闸设备/卷扬式启闭机.glb','这是一个自动导入的模型：卷扬式启闭机.glb'),(200000062,'水务水利/水闸设备','闸阀.glb','src/main/resources/static/models/水务水利/水闸设备/闸阀.glb','这是一个自动导入的模型：闸阀.glb'),(200000063,'水务水利/水闸设备','止回阀.glb','src/main/resources/static/models/水务水利/水闸设备/止回阀.glb','这是一个自动导入的模型：止回阀.glb'),(200000064,'水务水利/水闸设备','P型止水橡皮.glb','src/main/resources/static/models/水务水利/水闸设备/P型止水橡皮.glb','这是一个自动导入的模型：P型止水橡皮.glb'),(200000065,'水务水利/环境要素','道路.glb','src/main/resources/static/models/水务水利/环境要素/道路.glb','这是一个自动导入的模型：道路.glb'),(200000066,'水务水利/水处理设备','沉淀池.glb','src/main/resources/static/models/水务水利/水处理设备/沉淀池.glb','这是一个自动导入的模型：沉淀池.glb'),(200000067,'水务水利/水处理设备','活性炭过滤器.glb','src/main/resources/static/models/水务水利/水处理设备/活性炭过滤器.glb','这是一个自动导入的模型：活性炭过滤器.glb'),(200000068,'水务水利/水处理设备','砂滤器.glb','src/main/resources/static/models/水务水利/水处理设备/砂滤器.glb','这是一个自动导入的模型：砂滤器.glb'),(200000069,'水务水利/水处理设备','格栅除污机.glb','src/main/resources/static/models/水务水利/水处理设备/格栅除污机.glb','这是一个自动导入的模型：格栅除污机.glb'),(200000070,'水务水利/水处理设备','集水池.glb','src/main/resources/static/models/水务水利/水处理设备/集水池.glb','这是一个自动导入的模型：集水池.glb'),(200000071,'水务水利/电气与动力设施','控制台.glb','src/main/resources/static/models/水务水利/电气与动力设施/控制台.glb','这是一个自动导入的模型：控制台.glb'),(200000072,'水务水利/电气与动力设施','螺旋输送机.glb','src/main/resources/static/models/水务水利/电气与动力设施/螺旋输送机.glb','这是一个自动导入的模型：螺旋输送机.glb'),(200000073,'水务水利/电气与动力设施','台式电脑.glb','src/main/resources/static/models/水务水利/电气与动力设施/台式电脑.glb','这是一个自动导入的模型：台式电脑.glb'),(200000074,'水务水利/水闸设备','闸底板.glb','src/main/resources/static/models/水务水利/水闸设备/闸底板.glb','这是一个自动导入的模型：闸底板.glb'),(200000075,'水务水利/水闸设备','闸墩.glb','src/main/resources/static/models/水务水利/水闸设备/闸墩.glb','这是一个自动导入的模型：闸墩.glb'),(200000076,'水务水利/水闸设备','水闸建筑.glb','src/main/resources/static/models/水务水利/水闸设备/水闸建筑.glb','这是一个自动导入的模型：水闸建筑.glb'),(200000077,'水务水利/环境要素','安全防护栏.glb','src/main/resources/static/models/水务水利/环境要素/安全防护栏.glb','这是一个自动导入的模型：安全防护栏.glb'),(200000078,'水务水利/环境要素','安全警示标志.glb','src/main/resources/static/models/水务水利/环境要素/安全警示标志.glb','这是一个自动导入的模型：安全警示标志.glb'),(200000079,'水务水利/环境要素','消防栓.glb','src/main/resources/static/models/水务水利/环境要素/消防栓.glb','这是一个自动导入的模型：消防栓.glb'),(200000080,'水务水利/环境要素','应急照明设备.glb','src/main/resources/static/models/水务水利/环境要素/应急照明设备.glb','这是一个自动导入的模型：应急照明设备.glb'),(200000081,'水务水利/监测与自动化设备','AI摄像头.glb','src/main/resources/static/models/水务水利/监测与自动化设备/AI摄像头.glb','这是一个自动导入的模型：AI摄像头.glb'),(200000082,'水务水利/监测与自动化设备','水下机器人.glb','src/main/resources/static/models/水务水利/监测与自动化设备/水下机器人.glb','这是一个自动导入的模型：水下机器人.glb'),(200000083,'水务水利/水处理设备','拦污栅清理机.glb','src/main/resources/static/models/水务水利/水处理设备/拦污栅清理机.glb','这是一个自动导入的模型：拦污栅清理机.glb'),(200000084,'水务水利/监测设备','地下水监测井.glb','src/main/resources/static/models/水务水利/监测设备/地下水监测井.glb','这是一个自动导入的模型：地下水监测井.glb'),(200000085,'水务水利/环境要素','检修车间.glb','src/main/resources/static/models/水务水利/环境要素/检修车间.glb','这是一个自动导入的模型：检修车间.glb'),(200000086,'水务水利/水处理设备','空气阀.glb','src/main/resources/static/models/水务水利/水处理设备/空气阀.glb','这是一个自动导入的模型：空气阀.glb'),(200000087,'水务水利/水处理设备','离子交换器.glb','src/main/resources/static/models/水务水利/水处理设备/离子交换器.glb','这是一个自动导入的模型：离子交换器.glb'),(200000088,'水务水利/测量设备','流速仪(便携式).glb','src/main/resources/static/models/水务水利/测量设备/流速仪(便携式).glb','这是一个自动导入的模型：流速仪(便携式).glb'),(200000089,'水务水利/测量设备','流速仪(固定式).glb','src/main/resources/static/models/水务水利/测量设备/流速仪(固定式).glb','这是一个自动导入的模型：流速仪(固定式).glb'),(200000090,'水务水利/水处理设备','闸膜过滤系统.glb','src/main/resources/static/models/水务水利/水处理设备/膜过滤系统.glb','这是一个自动导入的模型：膜过滤系统.glb'),(200000091,'水务水利/监测设备','泥沙监测仪.glb','src/main/resources/static/models/水务水利/监测设备/泥沙监测仪.glb','这是一个自动导入的模型：泥沙监测仪.glb'),(200000092,'水务水利/监测设备','气象站(一体化).glb','src/main/resources/static/models/水务水利/监测设备/气象站(一体化).glb','这是一个自动导入的模型：气象站(一体化).glb'),(200000093,'水务水利/水处理设备','污泥浓缩机.glb','src/main/resources/static/models/水务水利/水处理设备/污泥浓缩机.glb','这是一个自动导入的模型：污泥浓缩机.glb'),(200000094,'水务水利/环境要素','安全爬梯.glb','src/main/resources/static/models/水务水利/环境要素/安全爬梯.glb','这是一个自动导入的模型：安全爬梯.glb'),(200000095,'水务水利/环境要素','防雷接地装置.glb','src/main/resources/static/models/水务水利/环境要素/防雷接地装置.glb','这是一个自动导入的模型：防雷接地装置.glb'),(200000096,'水务水利/环境要素','景观照明灯.glb','src/main/resources/static/models/水务水利/环境要素/景观照明灯.glb','这是一个自动导入的模型：景观照明灯.glb'),(200000097,'水务水利/环境要素','救生圈.glb','src/main/resources/static/models/水务水利/环境要素/救生圈.glb','这是一个自动导入的模型：救生圈.glb'),(200000098,'水务水利/环境要素','生态浮岛.glb','src/main/resources/static/models/水务水利/环境要素/生态浮岛.glb','这是一个自动导入的模型：生态浮岛.glb'),(200000099,'水务水利/环境要素','太阳能供电系统.glb','src/main/resources/static/models/水务水利/环境要素/太阳能供电系统.glb','这是一个自动导入的模型：太阳能供电系统.glb'),(200000100,'水务水利/环境要素','PLC控制箱.glb','src/main/resources/static/models/水务水利/环境要素/PLC控制箱.glb','这是一个自动导入的模型：PLC控制箱.glb'),(200000101,'水务水利/水坝工程','水坝.glb','src/main/resources/static/models/水务水利/水坝工程/水坝.glb','这是一个自动导入的模型：水坝.glb'),(200000102,'水务水利/施工机械','工程抢修车.glb','src/main/resources/static/models/水务水利/施工机械/工程抢修车.glb','这是一个自动导入的模型：工程抢修车.glb'),(200000103,'水务水利/管道体系','钢管.glb','src/main/resources/static/models/水务水利/管道体系/钢管.glb','这是一个自动导入的模型：钢管.glb'),(200000104,'水务水利/管道体系','混凝土管.glb','src/main/resources/static/models/水务水利/管道体系/混凝土管.glb','这是一个自动导入的模型：混凝土管.glb'),(200000105,'水务水利/环境要素','仓库.glb','src/main/resources/static/models/水务水利/环境要素/仓库.glb','这是一个自动导入的模型：仓库.glb'),(200000106,'水务水利/环境要素','配电室.glb','src/main/resources/static/models/水务水利/环境要素/配电室.glb','这是一个自动导入的模型：配电室.glb'),(200000107,'水务水利/环境要素','控制室.glb','src/main/resources/static/models/水务水利/环境要素/控制室.glb','这是一个自动导入的模型：控制室.glb'),(200000108,'水务水利/环境要素','厂区道路.glb','src/main/resources/static/models/水务水利/环境要素/厂区道路.glb','这是一个自动导入的模型：厂区道路.glb'),(200000109,'水务水利/环境要素','施工大门.glb','src/main/resources/static/models/水务水利/环境要素/施工大门.glb','这是一个自动导入的模型：施工大门.glb'),(200000110,'水务水利/环境要素','排水沟.glb','src/main/resources/static/models/水务水利/环境要素/排水沟.glb','这是一个自动导入的模型：排水沟.glb'),(200000111,'水务水利/环境要素','引水渠.glb','src/main/resources/static/models/水务水利/环境要素/引水渠.glb','这是一个自动导入的模型：引水渠.glb'),(200000112,'水务水利/环境要素','水塔.glb','src/main/resources/static/models/水务水利/环境要素/水塔.glb','这是一个自动导入的模型：水塔.glb'),(200000113,'水务水利/环境要素','净水厂.glb','src/main/resources/static/models/水务水利/环境要素/净水厂.glb','这是一个自动导入的模型：净水厂.glb'),(200000114,'水务水利/环境要素','污水处理厂.glb','src/main/resources/static/models/水务水利/环境要素/污水处理厂.glb','这是一个自动导入的模型：污水处理厂.glb'),(200000115,'水务水利/环境要素','码头.glb','src/main/resources/static/models/水务水利/环境要素/码头.glb','这是一个自动导入的模型：码头.glb'),(200000116,'水务水利/管道体系','管件(法兰).glb','src/main/resources/static/models/水务水利/管道体系/管件(法兰).glb','这是一个自动导入的模型：管件(法兰).glb'),(200000117,'水务水利/管道体系','管件(法兰)2.glb','src/main/resources/static/models/水务水利/管道体系/管件(法兰)2.glb','这是一个自动导入的模型：管件(法兰)2.glb'),(200000118,'水务水利/管道体系','管件(三通).glb','src/main/resources/static/models/水务水利/管道体系/管件(三通).glb','这是一个自动导入的模型：管件(三通).glb'),(200000119,'水务水利/管道体系','管件(弯头).glb','src/main/resources/static/models/水务水利/管道体系/管件(弯头).glb','这是一个自动导入的模型：管件(弯头).glb'),(200000120,'水务水利/管道体系','管件(弯头)2.glb','src/main/resources/static/models/水务水利/管道体系/管件(弯头)2.glb','这是一个自动导入的模型：管件(弯头)2.glb'),(200000121,'水务水利/环境要素','门卫机器人1.glb','src/main/resources/static/models/水务水利/环境要素/门卫机器人1.glb','这是一个自动导入的模型：门卫机器人1.glb'),(200000122,'水务水利/环境要素','门卫机器人2.glb','src/main/resources/static/models/水务水利/环境要素/门卫机器人2.glb','这是一个自动导入的模型：门卫机器人2.glb'),(200000123,'水务水利/水坝工程','减震装置.glb','src/main/resources/static/models/水务水利/水坝工程/减震装置.glb','这是一个自动导入的模型：减震装置.glb'),(200000124,'水务水利/施工机械','取水泵船.glb','src/main/resources/static/models/水务水利/施工机械/取水泵船.glb','这是一个自动导入的模型：取水泵船.glb'),(200000125,'水务水利/环境要素','取水隧洞.glb','src/main/resources/static/models/水务水利/环境要素/取水隧洞.glb','这是一个自动导入的模型：取水隧洞.glb'),(200000126,'水务水利/环境要素','护脚构造.glb','src/main/resources/static/models/水务水利/环境要素/护脚构造.glb','这是一个自动导入的模型：护脚构造.glb'),(200000127,'水务水利/环境要素','轨道.glb','src/main/resources/static/models/水务水利/环境要素/轨道.glb','这是一个自动导入的模型：轨道.glb'),(200000128,'水务水利/环境要素','路缘石.glb','src/main/resources/static/models/水务水利/环境要素/路缘石.glb','这是一个自动导入的模型：路缘石.glb'),(200000129,'水务水利/雨洪利用','处理机组.glb','src/main/resources/static/models/水务水利/雨洪利用/处理机组.glb','这是一个自动导入的模型：处理机组.glb'),(200000130,'水务水利/雨洪利用','回用泵站.glb','src/main/resources/static/models/水务水利/雨洪利用/回用泵站.glb','这是一个自动导入的模型：回用泵站.glb'),(200000131,'水务水利/雨洪利用','回用水箱.glb','src/main/resources/static/models/水务水利/雨洪利用/回用水箱.glb','这是一个自动导入的模型：回用水箱.glb'),(200000132,'水务水利/雨洪利用','下凹绿地.glb','src/main/resources/static/models/水务水利/雨洪利用/下凹绿地.glb','这是一个自动导入的模型：下凹绿地.glb'),(200000133,'水务水利/雨洪利用','溢流口.glb','src/main/resources/static/models/水务水利/雨洪利用/溢流口.glb','这是一个自动导入的模型：溢流口.glb'),(200000134,'水务水利/雨洪利用','雨水花园.glb','src/main/resources/static/models/水务水利/雨洪利用/雨水花园.glb','这是一个自动导入的模型：雨水花园.glb'),(200000135,'水务水利/取水与原水系统','沉砂槽.glb','src/main/resources/static/models/水务水利/取水与原水系统/沉砂槽.glb','这是一个自动导入的模型：沉砂槽.glb'),(200000136,'水务水利/取水与原水系统','出水管.glb','src/main/resources/static/models/水务水利/取水与原水系统/出水管.glb','这是一个自动导入的模型：出水管.glb'),(200000137,'水务水利/取水与原水系统','防涡板.glb','src/main/resources/static/models/水务水利/取水与原水系统/防涡板.glb','这是一个自动导入的模型：防涡板.glb'),(200000138,'水务水利/取水与原水系统','井门.glb','src/main/resources/static/models/水务水利/取水与原水系统/井门.glb','这是一个自动导入的模型：井门.glb'),(200000139,'水务水利/取水与原水系统','前池.glb','src/main/resources/static/models/水务水利/取水与原水系统/前池.glb','这是一个自动导入的模型：前池.glb'),(200000140,'水务水利/取水与原水系统','浅井.glb','src/main/resources/static/models/水务水利/取水与原水系统/浅井.glb','这是一个自动导入的模型：浅井.glb'),(200000141,'水务水利/取水与原水系统','深井.glb','src/main/resources/static/models/水务水利/取水与原水系统/深井.glb','这是一个自动导入的模型：深井.glb'),(200000142,'水务水利/取水与原水系统','吸水井.glb','src/main/resources/static/models/水务水利/取水与原水系统/吸水井.glb','这是一个自动导入的模型：吸水井.glb'),(200000143,'水务水利/取水与原水系统','原水泵.glb','src/main/resources/static/models/水务水利/取水与原水系统/原水泵.glb','这是一个自动导入的模型：原水泵.glb'),(200000144,'水务水利/取水与原水系统','阀门井.glb','src/main/resources/static/models/水务水利/取水与原水系统/阀门井.glb','这是一个自动导入的模型：阀门井.glb'),(200000145,'水务水利/污水再生水处理','超滤装置.glb','src/main/resources/static/models/水务水利/污水再生水处理/超滤装置.glb','这是一个自动导入的模型：超滤装置.glb'),(200000146,'水务水利/污水再生水处理','二沉池.glb','src/main/resources/static/models/水务水利/污水再生水处理/二沉池.glb','这是一个自动导入的模型：二沉池.glb'),(200000147,'水务水利/污水再生水处理','反硝化滤池.glb','src/main/resources/static/models/水务水利/污水再生水处理/反硝化滤池.glb','这是一个自动导入的模型：反硝化滤池.glb'),(200000148,'水务水利/污水再生水处理','鼓风机房.glb','src/main/resources/static/models/水务水利/污水再生水处理/鼓风机房.glb','这是一个自动导入的模型：鼓风机房.glb'),(200000149,'水务水利/污水再生水处理','化学洗涤塔.glb','src/main/resources/static/models/水务水利/污水再生水处理/化学洗涤塔.glb','这是一个自动导入的模型：化学洗涤塔.glb'),(200000150,'水务水利/污水再生水处理','活性炭吸附塔.glb','src/main/resources/static/models/水务水利/污水再生水处理/活性炭吸附塔.glb','这是一个自动导入的模型：活性炭吸附塔.glb'),(200000151,'水务水利/污水再生水处理','矿化装置.glb','src/main/resources/static/models/水务水利/污水再生水处理/矿化装置.glb','这是一个自动导入的模型：矿化装置.glb'),(200000152,'水务水利/污水再生水处理','浓缩池.glb','src/main/resources/static/models/水务水利/污水再生水处理/浓缩池.glb','这是一个自动导入的模型：浓缩池.glb'),(200000153,'水务水利/污水再生水处理','生物滤池除臭塔.glb','src/main/resources/static/models/水务水利/污水再生水处理/生物滤池除臭塔.glb','这是一个自动导入的模型：生物滤池除臭塔.glb'),(200000154,'水务水利/污水再生水处理','尾水排放口.glb','src/main/resources/static/models/水务水利/污水再生水处理/尾水排放口.glb','这是一个自动导入的模型：尾水排放口.glb'),(200000155,'水务水利/污水再生水处理','消毒接触池.glb','src/main/resources/static/models/水务水利/污水再生水处理/消毒接触池.glb','这是一个自动导入的模型：消毒接触池.glb'),(200000156,'水务水利/污水再生水处理','氧化池.glb','src/main/resources/static/models/水务水利/污水再生水处理/氧化池.glb','这是一个自动导入的模型：氧化池.glb'),(200000157,'水务水利/污水再生水处理','在线监测小屋.glb','src/main/resources/static/models/水务水利/污水再生水处理/在线监测小屋.glb','这是一个自动导入的模型：在线监测小屋.glb'),(200000158,'水务水利/污水再生水处理','贮泥仓.glb','src/main/resources/static/models/水务水利/污水再生水处理/贮泥仓.glb','这是一个自动导入的模型：贮泥仓.glb'),(200000159,'水务水利/污水再生水处理','装车平台.glb','src/main/resources/static/models/水务水利/污水再生水处理/装车平台.glb','这是一个自动导入的模型：装车平台.glb'),(200000160,'水务水利/污水再生水处理','A2O反应池.glb','src/main/resources/static/models/水务水利/污水再生水处理/A2O反应池.glb','这是一个自动导入的模型：A2O反应池.glb'),(200000161,'水务水利/污水再生水处理','MBR膜池.glb','src/main/resources/static/models/水务水利/污水再生水处理/MBR膜池.glb','这是一个自动导入的模型：MBR膜池.glb'),(200000162,'水务水利/水轮机','两击式水轮机.glb','src/main/resources/static/models/水务水利/水轮机/两击式水轮机.glb','这是一个自动导入的模型：两击式水轮机.glb'),(200000163,'水务水利/水轮机','斜击式水轮机.glb','src/main/resources/static/models/水务水利/水轮机/斜击式水轮机.glb','这是一个自动导入的模型：斜击式水轮机.glb'),(200000164,'水务水利/水轮机','水斗式水轮机.glb','src/main/resources/static/models/水务水利/水轮机/水斗式水轮机.glb','这是一个自动导入的模型：水斗式水轮机.glb'),(200000165,'水务水利/水轮机','混流式水轮机.glb','src/main/resources/static/models/水务水利/水轮机/混流式水轮机.glb','这是一个自动导入的模型：混流式水轮机.glb'),(200000166,'水务水利/水轮机','贯流式水轮机.glb','src/main/resources/static/models/水务水利/水轮机/贯流式水轮机.glb','这是一个自动导入的模型：贯流式水轮机.glb'),(200000167,'水务水利/水轮机','轴流式水轮机.glb','src/main/resources/static/models/水务水利/水轮机/轴流式水轮机.glb','这是一个自动导入的模型：轴流式水轮机.glb'),(200000168,'水务水利/管道体系','高压水道布置.glb','src/main/resources/static/models/水务水利/管道体系/高压水道布置.glb','这是一个自动导入的模型：高压水道布置.glb'),(200000169,'水务水利/管道体系','双室式调压室.glb','src/main/resources/static/models/水务水利/管道体系/双室式调压室.glb','这是一个自动导入的模型：双室式调压室.glb'),(200000170,'水务水利/管道体系','差动式调压室.glb','src/main/resources/static/models/水务水利/管道体系/差动式调压室.glb','这是一个自动导入的模型：差动式调压室.glb'),(200000171,'水务水利/监测设备','无人机巡检.glb','src/main/resources/static/models/水务水利/监测设备/无人机巡检.glb','这是一个自动导入的模型：无人机巡检.glb'),(200000172,'水务水利/雨洪利用','移动式防洪墙.glb','src/main/resources/static/models/水务水利/雨洪利用/移动式防洪墙.glb','这是一个自动导入的模型：移动式防洪墙.glb'),(200000173,'水务水利/环境要素','人工湿地单元.glb','src/main/resources/static/models/水务水利/环境要素/人工湿地单元.glb','这是一个自动导入的模型：人工湿地单元.glb'),(200000174,'水务水利/环境要素','鱼类洄游通道.glb','src/main/resources/static/models/水务水利/环境要素/鱼类洄游通道.glb','这是一个自动导入的模型：鱼类洄游通道.glb'),(200000175,'水务水利/雨洪利用','堤防河道整治.glb','src/main/resources/static/models/水务水利/雨洪利用/堤防河道整治.glb','这是一个自动导入的模型：堤防河道整治.glb'),(200000176,'水务水利/水坝工程','大坝BIM模型.glb','src/main/resources/static/models/水务水利/水坝工程/大坝BIM模型.glb','这是一个自动导入的模型：大坝BIM模型.glb'),(200000177,'水务水利/环境要素','数字地表模型.glb','src/main/resources/static/models/水务水利/环境要素/数字地表模型.glb','这是一个自动导入的模型：数字地表模型.glb'),(200000178,'水务水利/水闸设备','水闸泵站glb','src/main/resources/static/models/水务水利/水闸设备/水闸泵站.glb','这是一个自动导入的模型：水闸泵站glb'),(200000179,'水务水利/管道体系','输水管道.glb','src/main/resources/static/models/水务水利/管道体系/输水管道.glb','这是一个自动导入的模型：输水管道.glb'),(200000180,'水务水利/水坝工程','拱坝.glb','src/main/resources/static/models/水务水利/水坝工程/拱坝.glb','这是一个自动导入的模型：拱坝.glb'),(200000181,'水务水利/水坝工程','土石坝.glb','src/main/resources/static/models/水务水利/水坝工程/土石坝.glb','这是一个自动导入的模型：土石坝.glb'),(200000182,'水务水利/监测设备','操控室.glb','src/main/resources/static/models/水务水利/监测设备/操控室.glb','这是一个自动导入的模型：操控室.glb'),(200000183,'水务水利/雨洪利用','泄洪洞.glb','src/main/resources/static/models/水务水利/雨洪利用/泄洪洞.glb','这是一个自动导入的模型：泄洪洞.glb'),(200000184,'水务水利/环境要素','交通设施.glb','src/main/resources/static/models/水务水利/环境要素/交通设施.glb','这是一个自动导入的模型：交通设施.glb'),(200000185,'水务水利/环境要素','河道建筑物.glb','src/main/resources/static/models/水务水利/环境要素/河道建筑物.glb','这是一个自动导入的模型：河道建筑物.glb'),(200000186,'水务水利/环境要素','通信设备.glb','src/main/resources/static/models/水务水利/环境要素/通信设备.glb','这是一个自动导入的模型：通信设备.glb'),(200000187,'水务水利/雨洪利用','排涝泵站.glb','src/main/resources/static/models/水务水利/雨洪利用/排涝泵站.glb','这是一个自动导入的模型：排涝泵站.glb'),(200000188,'水务水利/雨洪利用','倒虹吸.glb','src/main/resources/static/models/水务水利/雨洪利用/倒虹吸.glb','这是一个自动导入的模型：倒虹吸.glb'),(200000189,'水务水利/雨洪利用','冲锋舟.glb','src/main/resources/static/models/水务水利/雨洪利用/冲锋舟.glb','这是一个自动导入的模型：冲锋舟.glb'),(200000190,'水务水利/灌溉系统','滴灌带.glb','src/main/resources/static/models/水务水利/灌溉系统/滴灌带.glb','这是一个自动导入的模型：滴灌带.glb'),(200000191,'水务水利/灌溉系统','滴灌管.glb','src/main/resources/static/models/水务水利/灌溉系统/滴灌管.glb','这是一个自动导入的模型：滴灌管.glb'),(200000192,'水务水利/灌溉系统','喷灌喷头.glb','src/main/resources/static/models/水务水利/灌溉系统/喷灌喷头.glb','这是一个自动导入的模型：喷灌喷头.glb'),(200000193,'水务水利/灌溉系统','施肥罐.glb','src/main/resources/static/models/水务水利/灌溉系统/施肥罐.glb','这是一个自动导入的模型：施肥罐.glb'),(200000194,'水务水利/灌溉系统','田间渠道.glb','src/main/resources/static/models/水务水利/灌溉系统/田间渠道.glb','这是一个自动导入的模型：田间渠道.glb'),(200000195,'水务水利/环境要素','雨棚.glb','src/main/resources/static/models/水务水利/环境要素/雨棚.glb','这是一个自动导入的模型：雨棚.glb'),(200000196,'水务水利/海洋工程','波浪测量仪.glb','src/main/resources/static/models/水务水利/海洋工程/波浪测量仪.glb','这是一个自动导入的模型：波浪测量仪.glb'),(200000197,'水务水利/海洋工程','潮位站.glb','src/main/resources/static/models/水务水利/海洋工程/潮位站.glb','这是一个自动导入的模型：潮位站.glb'),(200000198,'水务水利/海洋工程','防波堤.glb','src/main/resources/static/models/水务水利/海洋工程/防波堤.glb','这是一个自动导入的模型：防波堤.glb'),(200000199,'水务水利/海洋工程','海底管道.glb','src/main/resources/static/models/水务水利/海洋工程/海底管道.glb','这是一个自动导入的模型：海底管道.glb'),(200000200,'水务水利/海洋工程','护岸.glb','src/main/resources/static/models/水务水利/海洋工程/护岸.glb','这是一个自动导入的模型：护岸.glb'),(200000201,'水务水利/海洋工程','系船设施.glb','src/main/resources/static/models/水务水利/海洋工程/系船设施.glb','这是一个自动导入的模型：系船设施.glb'),(200000202,'水务水利/海洋工程','海上风电基础.glb','src/main/resources/static/models/水务水利/海洋工程/海上风电基础.glb','这是一个自动导入的模型：海上风电基础.glb'),(200000203,'水务水利/海洋工程','高桩码头.glb','src/main/resources/static/models/水务水利/海洋工程/高桩码头.glb','这是一个自动导入的模型：高桩码头.glb'),(200000204,'水务水利/海洋工程','海上风电叶片.glb','src/main/resources/static/models/水务水利/海洋工程/海上风电叶片.glb','这是一个自动导入的模型：海上风电叶片.glb'),(200000205,'水务水利/海洋工程','逃生艇.glb','src/main/resources/static/models/水务水利/海洋工程/逃生艇.glb','这是一个自动导入的模型：逃生艇.glb'),(200000206,'水务水利/海洋工程','海底地震仪.glb','src/main/resources/static/models/水务水利/海洋工程/海底地震仪.glb','这是一个自动导入的模型：海底地震仪.glb'),(200000207,'水务水利/海洋工程','重力式码头.glb','src/main/resources/static/models/水务水利/海洋工程/重力式码头.glb','这是一个自动导入的模型：重力式码头.glb'),(200000208,'水务水利/海洋工程','栈桥.glb','src/main/resources/static/models/水务水利/海洋工程/栈桥.glb','这是一个自动导入的模型：栈桥.glb'),(200000209,'水务水利/海洋工程','钻井平台.glb','src/main/resources/static/models/水务水利/海洋工程/钻井平台.glb','这是一个自动导入的模型：钻井平台.glb'),(200000210,'水务水利/海洋工程','潮汐能发电机组.glb','src/main/resources/static/models/水务水利/海洋工程/潮汐能发电机组.glb','这是一个自动导入的模型：潮汐能发电机组.glb'),(200000211,'水务水利/海洋工程','潜水装备.glb','src/main/resources/static/models/水务水利/海洋工程/潜水装备.glb','这是一个自动导入的模型：潜水装备.glb'),(200000212,'水务水利/海洋工程','板桩码头.glb','src/main/resources/static/models/水务水利/海洋工程/板桩码头.glb','这是一个自动导入的模型：板桩码头.glb'),(200000213,'水务水利/海洋工程','海床基.glb','src/main/resources/static/models/水务水利/海洋工程/海床基.glb','这是一个自动导入的模型：海床基.glb'),(200000214,'水务水利/海洋工程','锚链.glb','src/main/resources/static/models/水务水利/海洋工程/锚链.glb','这是一个自动导入的模型：锚链.glb'),(200000215,'水务水利/海洋工程','波浪能发电装置.glb','src/main/resources/static/models/水务水利/海洋工程/波浪能发电装置.glb','这是一个自动导入的模型：波浪能发电装置.glb');
/*!40000 ALTER TABLE `models` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `scene_assets`
--

DROP TABLE IF EXISTS `scene_assets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scene_assets` (
  `id` bigint NOT NULL,
  `name` varchar(255) NOT NULL,
  `file_type` varchar(10) NOT NULL,
  `path` varchar(512) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `texture_info` text COMMENT '??ͼ??Ϣ(JSON??ʽ)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scene_assets`
--

LOCK TABLES `scene_assets` WRITE;
/*!40000 ALTER TABLE `scene_assets` DISABLE KEYS */;
INSERT INTO `scene_assets` VALUES (1769608329421,'场景_2026/1/28_1个模型','glb','/scenes/1769608329383-3c8cedc2-383e-41bf-87a2-47466e4fe22e.glb','csa','2026-01-28 13:52:09',NULL),(1772165047239,'场景_2026/2/27_1个模型','glb','/scenes/1772165047221-eb071455-b4d2-48ed-9a18-03f79c7dfd35.glb','cs','2026-02-27 04:04:07',NULL);
/*!40000 ALTER TABLE `scene_assets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subcategories`
--

DROP TABLE IF EXISTS `subcategories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subcategories` (
  `id` int NOT NULL AUTO_INCREMENT,
  `category_id` int NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `category_id` (`category_id`) USING BTREE,
  CONSTRAINT `subcategories_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subcategories`
--

LOCK TABLES `subcategories` WRITE;
/*!40000 ALTER TABLE `subcategories` DISABLE KEYS */;
/*!40000 ALTER TABLE `subcategories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_scenes`
--

DROP TABLE IF EXISTS `user_scenes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_scenes` (
  `id` varchar(36) NOT NULL,
  `user_id` varchar(36) NOT NULL,
  `name` varchar(255) NOT NULL,
  `scene_data` longtext NOT NULL COMMENT 'JSON??ʽ?ĳ????',
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_scenes`
--

LOCK TABLES `user_scenes` WRITE;
/*!40000 ALTER TABLE `user_scenes` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_scenes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `userdata`
--

DROP TABLE IF EXISTS `userdata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `userdata` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `user_info` json NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE,
  CONSTRAINT `userdata_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`ID`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `userdata`
--

LOCK TABLES `userdata` WRITE;
/*!40000 ALTER TABLE `userdata` DISABLE KEYS */;
INSERT INTO `userdata` VALUES (2,4,'{}','2024-10-30 06:23:54','2024-10-30 06:23:54'),(3,5,'{}','2024-11-06 06:06:22','2024-11-06 06:06:22'),(4,6,'{}','2024-11-06 08:06:51','2024-11-06 08:06:51'),(5,7,'{}','2024-11-13 04:42:56','2024-11-13 04:42:56'),(6,8,'{}','2024-11-13 04:44:04','2024-11-13 04:44:04'),(7,10,'{}','2024-11-13 06:24:38','2024-11-13 06:24:38'),(8,11,'{}','2024-11-13 06:25:30','2024-11-13 06:25:30'),(9,12,'{}','2024-12-07 17:35:21','2024-12-07 17:35:21'),(10,13,'{}','2025-06-09 11:29:56','2025-06-09 11:29:56');
/*!40000 ALTER TABLE `userdata` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `phone` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `role` enum('ADMIN','USER') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (4,'NormalUser','96e79218965eb72c92a549dd5a330112','1111111111','111111@qq.com','USER',1),(5,'admin','21232f297a57a5a743894a0e4a801fc3','12345678901','123456@qq.com','ADMIN',0),(6,'user','e10adc3949ba59abbe56e057f20f883e','1199','111W@qq.com','USER',0),(7,'test','098f6bcd4621d373cade4e832627b4f6',NULL,'111W@qq.com','USER',1),(8,'1','c4ca4238a0b923820dcc509a6f75849b',NULL,'1','USER',1),(10,'0987654321','5f4dcc3b5aa765d61d8327deb882cf99','0987654321','0987654321@q.com','USER',0),(11,'4','5f4dcc3b5aa765d61d8327deb882cf99','4','4','USER',0),(12,'5','e4da3b7fbbce2345d7772b0674a318d5',NULL,'5','USER',0),(13,'as','e10adc3949ba59abbe56e057f20f883e',NULL,'13962067837@163.com','USER',0),(14,'q','c4ca4238a0b923820dcc509a6f75849b',NULL,'123@qq','USER',0);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-30 20:42:19
