-- IRAS 智能简历诊断系统 - 数据库初始化脚本

CREATE DATABASE IF NOT EXISTS `iras` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `iras`;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT 'BCrypt加密密码',
  `email` varchar(100) DEFAULT NULL COMMENT '联系邮箱',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户系统表';

-- 2. 职位库表
CREATE TABLE IF NOT EXISTS `job_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `job_name` varchar(100) NOT NULL COMMENT '职位名称',
  `company_name` varchar(100) NOT NULL COMMENT '公司名称',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `salary` varchar(50) DEFAULT NULL COMMENT '薪资范围',
  `jd_text` text NOT NULL COMMENT '职位详情(JD内容)',
  `type` varchar(50) DEFAULT NULL COMMENT '职位分类(如: 全职/实习/校招)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '数据导入时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_job_name` (`job_name`),
  KEY `idx_city` (`city`),
  FULLTEXT KEY `idx_fulltext_jd` (`jd_text`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职位库表';

-- 插入示例职位数据
INSERT INTO `job_info` (`job_name`, `company_name`, `city`, `salary`, `jd_text`, `type`) VALUES
('Java后台开发工程师', '上海联泉智能科技有限公司', '上海', '15000', '5年及以上 本科 Java 后台开发 java 软件开发 软件工程师 开发 java开发 开发工程师 软件 工程师 五险一金 年终奖金 员工旅游 绩效奖金 餐饮补贴 交通补贴 通讯补贴', '全职'),
('前端开发工程师', '北京字节跳动科技有限公司', '北京', '20000', '3年及以上 本科 前端开发 JavaScript TypeScript Vue React HTML CSS Web开发 五险一金 弹性工作 免费三餐', '全职'),
('Python测试开发工程师', '深圳腾讯计算机系统有限公司', '深圳', '18000', '3年及以上 本科 Python 测试开发 自动化测试 性能测试 Selenium pytest 接口测试 五险一金 股票期权', '全职'),
('Android开发工程师', '杭州阿里巴巴网络科技有限公司', '杭州', '22000', '3年及以上 本科 Android Java Kotlin 移动开发 MVVM Jetpack 五险一金 补充医疗 带薪年假', '全职'),
('iOS开发工程师', '北京小米科技有限公司', '北京', '20000', '3年及以上 本科 iOS Swift Objective-C 移动开发 UIKit SwiftUI 五险一金 年终奖金', '全职'),
('后端开发工程师', '上海拼多多信息技术有限公司', '上海', '25000', '5年及以上 本科 Java Go 微服务 分布式 高并发 Spring Cloud Kubernetes 五险一金 股票期权', '全职'),
('数据分析师', '广州网易计算机系统有限公司', '广州', '15000', '2年及以上 本科 SQL Python 数据分析 Tableau 数据可视化 统计学 五险一金 年终奖金', '全职'),
('算法工程师', '北京百度网讯科技有限公司', '北京', '30000', '3年及以上 硕士 机器学习 深度学习 NLP CV Python TensorFlow PyTorch 五险一金 股票期权', '全职');
