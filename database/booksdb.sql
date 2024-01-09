CREATE TABLE `pagamento` (
  `id` tinyint(4) NOT NULL AUTO_INCREMENT,
  `id_locacao` tinyint(4) DEFAULT NULL,
  `total_pagar` varchar(20) DEFAULT NULL,
  `forma_pagamento` enum('pix','cartão','dinheiro') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id_locacao` (`id_locacao`),
  CONSTRAINT `pagamento_ibfk_1` FOREIGN KEY (`id_locacao`) REFERENCES `locacao` (`id_locacao`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci