
CREATE USER ``@`localhost` IDENTIFIED BY 'local';
CREATE DATABASE IF NOT EXISTS `db_authentication`;
GRANT ALL PRIVILEGES ON `db_authentication`.* TO ``@`%`;
CREATE DATABASE IF NOT EXISTS `db_integration_test`;
GRANT ALL PRIVILEGES ON `db_integration_test`.* TO ``@`%`;
CREATE DATABASE IF NOT EXISTS `db_sample`;
GRANT ALL PRIVILEGES ON `db_sample`.* TO ``@`%`;
