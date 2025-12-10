-- phpMyAdmin SQL Dump
-- version 5.2.2
-- https://www.phpmyadmin.net/
--
-- Servidor: mysql
-- Tiempo de generación: 10-12-2025 a las 18:14:52
-- Versión del servidor: 8.0.43
-- Versión de PHP: 8.2.27

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `AUREUS`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `COIN`
--

CREATE TABLE `COIN` (
                        `COIN_ID` int NOT NULL,
                        `COIN_NAME` varchar(255) NOT NULL,
                        `COIN_YEAR` int NOT NULL,
                        `COIN_MATERIAL` varchar(255) NOT NULL,
                        `COIN_WEIGHT` decimal(10,2) NOT NULL,
                        `COIN_DIAMETER` decimal(10,2) NOT NULL,
                        `ESTIMATED_VALUE` decimal(15,2) NOT NULL,
                        `ORIGIN_COUNTRY` varchar(255) NOT NULL,
                        `HISTORICAL_SIGNIFICANCE` varchar(1000) DEFAULT NULL,
                        `COLLECTION_ID` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `COIN`
--

INSERT INTO `COIN` (`COIN_ID`, `COIN_NAME`, `COIN_YEAR`, `COIN_MATERIAL`, `COIN_WEIGHT`, `COIN_DIAMETER`, `ESTIMATED_VALUE`, `ORIGIN_COUNTRY`, `HISTORICAL_SIGNIFICANCE`, `COLLECTION_ID`) VALUES
                                                                                                                                                                                               (4, 'Silver Coin', 123, 'gold', 43.40, 42.10, 400.00, 'Rome', 'no hist', 4),
                                                                                                                                                                                               (6, 'JapanWar', 1600, 'Silver', 5.25, 21.50, 150.00, 'Japan', 'Historic Coin about War in Japan', 1),
                                                                                                                                                                                               (7, 'Aureus', 123, 'silver', 34.00, 21.89, 150.00, 'Bcn', 'hist awesome', 1),
                                                                                                                                                                                               (8, 'Myst Grecee', 321, 'gold', 34.60, 12.68, 998.99, 'Anthropolys', NULL, 1),
                                                                                                                                                                                               (9, 'MundialSpain', 1970, 'silver', 45.50, 23.56, 160.00, 'Spain', 'Spain World Football Cup', 4),
                                                                                                                                                                                               (11, 'Grecee BC', 322, 'Gold', 5.34, 43.42, 999.00, 'Greece', NULL, 5),
                                                                                                                                                                                               (14, 'mikho', 1970, 'Gold', 12.40, 30.90, 100.00, 'Germany', NULL, 4),
                                                                                                                                                                                               (20, 'Test Hash Coin', 1281, 'gold', 12.89, 20.00, 0.87, 'spain', NULL, 5);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `COIN_COLLECTION`
--

CREATE TABLE `COIN_COLLECTION` (
                                   `COIN_ID` int NOT NULL,
                                   `COLLECTION_ID` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `COIN_COLLECTION`
--

INSERT INTO `COIN_COLLECTION` (`COIN_ID`, `COLLECTION_ID`) VALUES
                                                               (4, 4),
                                                               (4, 5);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `COIN_TRANSACTION`
--

CREATE TABLE `COIN_TRANSACTION` (
                                    `COIN_ID` int NOT NULL,
                                    `TRANSACTION_ID` int NOT NULL,
                                    `TRANSACTION_PRICE` decimal(38,2) DEFAULT NULL,
                                    `CURRENCY` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `COIN_TRANSACTION`
--

INSERT INTO `COIN_TRANSACTION` (`COIN_ID`, `TRANSACTION_ID`, `TRANSACTION_PRICE`, `CURRENCY`) VALUES
                                                                                                  (4, 12, 33.00, 'EUR'),
                                                                                                  (4, 14, 1000.00, 'EUR'),
                                                                                                  (4, 15, 1000.00, 'EUR'),
                                                                                                  (4, 16, 1000.00, 'EUR');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `COLLECTION`
--

CREATE TABLE `COLLECTION` (
                              `COLLECTION_ID` int NOT NULL,
                              `COLLECTION_NAME` varchar(255) NOT NULL,
                              `DESCRIPTION` varchar(1000) DEFAULT NULL,
                              `USER_ID` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `COLLECTION`
--

INSERT INTO `COLLECTION` (`COLLECTION_ID`, `COLLECTION_NAME`, `DESCRIPTION`, `USER_ID`) VALUES
                                                                                            (1, 'GOLD ROMANÇ', 'no description is a test', 2),
                                                                                            (4, 'coins for me', 'only buy', 16),
                                                                                            (5, 'test', 'test', 14);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `TRANSACTION`
--

CREATE TABLE `TRANSACTION` (
                               `TRANSACTION_ID` int NOT NULL,
                               `TRANSACTION_DATE` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                               `BUYER_ID` int NOT NULL,
                               `SELLER_ID` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `TRANSACTION`
--

INSERT INTO `TRANSACTION` (`TRANSACTION_ID`, `TRANSACTION_DATE`, `BUYER_ID`, `SELLER_ID`) VALUES
                                                                                              (10, '2025-09-20 14:21:16', 16, 2),
                                                                                              (11, '2025-09-20 14:23:25', 16, 2),
                                                                                              (12, '2025-09-20 14:27:34', 2, 16),
                                                                                              (13, '2025-09-20 16:27:03', 16, 2),
                                                                                              (14, '2025-09-20 16:28:49', 2, 16),
                                                                                              (15, '2025-09-20 17:07:52', 2, 14),
                                                                                              (16, '2025-09-20 17:31:29', 2, 14);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `USER`
--

CREATE TABLE `USER` (
                        `USER_ID` int NOT NULL,
                        `USERNAME` varchar(255) NOT NULL,
                        `EMAIL` varchar(255) NOT NULL,
                        `PASSWORD_HASH` varchar(255) NOT NULL,
                        `JOIN_DATE` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `USER`
--

INSERT INTO `USER` (`USER_ID`, `USERNAME`, `EMAIL`, `PASSWORD_HASH`, `JOIN_DATE`) VALUES
                                                                                      (2, 'pau', 'pau@example.com', 'papapjfde12', '2025-09-17 23:49:33'),
                                                                                      (14, 'duban', 'duban@gmail.com', 'rrrrrrr', '2025-09-18 15:21:09'),
                                                                                      (16, 'flore', 'frontmatec@gmail.com', 'fcveeeeeee1212', '2025-09-18 15:22:18');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `USER_DETAIL`
--

CREATE TABLE `USER_DETAIL` (
                               `USER_ID` int NOT NULL,
                               `BIRTHDATE` date DEFAULT NULL,
                               `PHONE` varchar(255) DEFAULT NULL,
                               `GENDER` varchar(255) DEFAULT NULL,
                               `NATIONALITY` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `USER_DETAIL`
--

INSERT INTO `USER_DETAIL` (`USER_ID`, `BIRTHDATE`, `PHONE`, `GENDER`, `NATIONALITY`) VALUES
                                                                                         (2, '2006-09-23', '722674754', 'male', 'spain'),
                                                                                         (16, '1965-07-07', '619267825', 'male', 'spain');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `COIN`
--
ALTER TABLE `COIN`
    ADD PRIMARY KEY (`COIN_ID`),
  ADD KEY `fk_coin_collection_main` (`COLLECTION_ID`);

--
-- Indices de la tabla `COIN_COLLECTION`
--
ALTER TABLE `COIN_COLLECTION`
    ADD PRIMARY KEY (`COIN_ID`,`COLLECTION_ID`),
  ADD KEY `COLLECTION_ID` (`COLLECTION_ID`);

--
-- Indices de la tabla `COIN_TRANSACTION`
--
ALTER TABLE `COIN_TRANSACTION`
    ADD PRIMARY KEY (`COIN_ID`,`TRANSACTION_ID`),
  ADD KEY `TRANSACTION_ID` (`TRANSACTION_ID`);

--
-- Indices de la tabla `COLLECTION`
--
ALTER TABLE `COLLECTION`
    ADD PRIMARY KEY (`COLLECTION_ID`),
  ADD KEY `USER_ID` (`USER_ID`);

--
-- Indices de la tabla `TRANSACTION`
--
ALTER TABLE `TRANSACTION`
    ADD PRIMARY KEY (`TRANSACTION_ID`),
  ADD KEY `BUYER_ID` (`BUYER_ID`),
  ADD KEY `SELLER_ID` (`SELLER_ID`);

--
-- Indices de la tabla `USER`
--
ALTER TABLE `USER`
    ADD PRIMARY KEY (`USER_ID`),
  ADD UNIQUE KEY `EMAIL` (`EMAIL`);

--
-- Indices de la tabla `USER_DETAIL`
--
ALTER TABLE `USER_DETAIL`
    ADD PRIMARY KEY (`USER_ID`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `COIN`
--
ALTER TABLE `COIN`
    MODIFY `COIN_ID` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT de la tabla `COLLECTION`
--
ALTER TABLE `COLLECTION`
    MODIFY `COLLECTION_ID` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `TRANSACTION`
--
ALTER TABLE `TRANSACTION`
    MODIFY `TRANSACTION_ID` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT de la tabla `USER`
--
ALTER TABLE `USER`
    MODIFY `USER_ID` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `COIN`
--
ALTER TABLE `COIN`
    ADD CONSTRAINT `fk_coin_collection_main` FOREIGN KEY (`COLLECTION_ID`) REFERENCES `COLLECTION` (`COLLECTION_ID`) ON DELETE CASCADE;

--
-- Filtros para la tabla `COIN_COLLECTION`
--
ALTER TABLE `COIN_COLLECTION`
    ADD CONSTRAINT `COIN_COLLECTION_ibfk_1` FOREIGN KEY (`COIN_ID`) REFERENCES `COIN` (`COIN_ID`) ON DELETE CASCADE,
  ADD CONSTRAINT `COIN_COLLECTION_ibfk_2` FOREIGN KEY (`COLLECTION_ID`) REFERENCES `COLLECTION` (`COLLECTION_ID`) ON DELETE CASCADE;

--
-- Filtros para la tabla `COIN_TRANSACTION`
--
ALTER TABLE `COIN_TRANSACTION`
    ADD CONSTRAINT `COIN_TRANSACTION_ibfk_1` FOREIGN KEY (`COIN_ID`) REFERENCES `COIN` (`COIN_ID`) ON DELETE CASCADE,
  ADD CONSTRAINT `COIN_TRANSACTION_ibfk_2` FOREIGN KEY (`TRANSACTION_ID`) REFERENCES `TRANSACTION` (`TRANSACTION_ID`) ON DELETE CASCADE;

--
-- Filtros para la tabla `COLLECTION`
--
ALTER TABLE `COLLECTION`
    ADD CONSTRAINT `COLLECTION_ibfk_1` FOREIGN KEY (`USER_ID`) REFERENCES `USER` (`USER_ID`) ON DELETE CASCADE;

--
-- Filtros para la tabla `TRANSACTION`
--
ALTER TABLE `TRANSACTION`
    ADD CONSTRAINT `TRANSACTION_ibfk_1` FOREIGN KEY (`BUYER_ID`) REFERENCES `USER` (`USER_ID`),
  ADD CONSTRAINT `TRANSACTION_ibfk_2` FOREIGN KEY (`SELLER_ID`) REFERENCES `USER` (`USER_ID`);

--
-- Filtros para la tabla `USER_DETAIL`
--
ALTER TABLE `USER_DETAIL`
    ADD CONSTRAINT `USER_DETAIL_ibfk_1` FOREIGN KEY (`USER_ID`) REFERENCES `USER` (`USER_ID`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
