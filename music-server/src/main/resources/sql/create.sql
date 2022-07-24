
CREATE TABLE `hk_stock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL,
  `hk_code` varchar(255) DEFAULT NULL,
  `source` varchar(255) DEFAULT NULL,
  `stock_name` varchar(255) DEFAULT NULL,
  `stock_price` decimal(10,2) DEFAULT NULL,
  `stock_num` decimal(20,2) DEFAULT NULL,
  `stock_percent` decimal(10,2) DEFAULT NULL,
  `sum_value` decimal(10,2) DEFAULT NULL,
  `stock_date` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
	KEY(code),
	KEY(stock_name),
	key(stock_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4


CREATE TABLE `hk_stock_rel` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL,
  `hk_code` varchar(255) DEFAULT NULL,
   PRIMARY KEY (`id`),
	KEY(code),
	KEY(hk_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4

update hk_stock a SET a.code = (select code from hk_stock_rel where hk_code = a.hk_code ) where a.code is null;

CREATE TABLE `stock_price_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL COMMENT '股票代码',
  `source` varchar(255) DEFAULT NULL COMMENT '沪深',
  `open_price` decimal(10,2) DEFAULT NULL COMMENT '开盘价',
  `stock_date` datetime DEFAULT NULL COMMENT '开盘时间',
  `closing_price` decimal(10,2) DEFAULT NULL COMMENT '收盘价',
  `price_change` decimal(10,2) DEFAULT NULL COMMENT '涨跌额',
  `change_percent` decimal(10,2) DEFAULT NULL COMMENT '涨跌幅',
  `low_price` decimal(10,2) DEFAULT NULL COMMENT '最低价',
  `high_price` decimal(10,2) DEFAULT NULL COMMENT '最高价',
  `tran_vol` decimal(10,2) DEFAULT NULL COMMENT '成交量',
  `tran_value` decimal(10,2) DEFAULT NULL COMMENT '成交额',
  `huan_shou_lv` decimal(10,2) DEFAULT NULL COMMENT '换手率',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `code` (`code`),
  KEY `stock_date` (`stock_date`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4

