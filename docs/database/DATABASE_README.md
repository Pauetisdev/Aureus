# 📚 AUREUS Database Documentation

**Idioma / Language:** [🇪🇸 Español](#-español) | [🇬🇧 English](#-english)
---

## 🇪🇸 Español

# 📊 Documentación de la Base de Datos AUREUS

## 🎯 Visión General
Base de datos relacional diseñada para gestionar un sistema de colección de monedas, permitiendo el seguimiento detallado de monedas, colecciones, usuarios y transacciones.

## 📋 Tablas Principales

### 1. USERS
Almacena la información de los usuarios del sistema.

- `USER_ID` (PK)
- `USERNAME`
- `EMAIL`
- `PASSWORD_HASH`
- `JOIN_DATE`

### 2. USER_DETAIL
Información adicional de los usuarios.

- `USER_ID` (FK → USER)
- `BIRTHDATE`
- `PHONE`
- `GENDER`
- `NATIONALITY`

### 3. COLLECTION
Gestiona las colecciones de monedas.

- `COLLECTION_ID` (PK)
- `USER_ID` (FK → USERS)
- `NAME`
- `DESCRIPTION`

### 4. COIN
Registro detallado de cada moneda.

- `COIN_ID` (PK)
- `COIN_NAME`
- `COIN_YEAR`
- `COIN_MATERIAL`
- `COIN_WEIGHT`
- `COIN_DIAMETER`
- `ESTIMATED_VALUE`
- `ORIGIN_COUNTRY`
- `HISTORICAL_SIGNIFICANCE`
- `COLLECTION_ID` (FK → COLLECTION)

### 5. COIN_COLLECTION
Tabla puente para relación muchos a muchos entre monedas y colecciones.

- `COIN_ID` (FK → COIN)
- `COLLECTION_ID` (FK → COLLECTION)

### 6. TRANSACTION
Registra todas las transacciones.

- `TRANSACTION_ID` (PK)
- `TRANSACTION_DATE`
- `BUYER_ID`
- `SELLER_ID`

### 7. COIN_TRANSACTION
Tabla puente para relación muchos a muchos entre monedas y transacciones, con información adicional.

- `COIN_ID` (FK → COIN)
- `TRANSACTION_ID` (FK → TRANSACTION)
- `TRANSACTION_PRICE`
- `CURRENCY`
## 🔄 Relaciones

### USER y USER_DETAIL (1:1)
- Cada usuario tiene un detalle asociado
- `USER_DETAIL.USER_ID` referencia a `USER.USER_ID`

### USER y COLLECTION (1:N)
- Un usuario puede tener múltiples colecciones
- Cada colección pertenece a un usuario

### COLLECTION y COIN (1:N)
- Una colección puede contener múltiples monedas
- Cada moneda pertenece a una colección principal

### COIN y COLLECTION (N:M) vía COIN_COLLECTION
- Una moneda puede estar en múltiples colecciones
- Una colección puede contener monedas de otras colecciones

### TRANSACTION y USER (N:M)
- Una transacción involucra un comprador y un vendedor (ambos usuarios)

### TRANSACTION y COIN (N:M) vía COIN_TRANSACTION
- Una transacción puede involucrar varias monedas
- Cada moneda puede estar en varias transacciones

## 🔒 Restricciones

### Integridad Referencial
- Borrado en cascada de colecciones → monedas propias
- Borrado restringido en transacciones
- Verificación de unicidad en emails y usernames

### Reglas de Negocio
1. Una moneda debe tener siempre una colección principal
2. Las transacciones requieren usuarios diferentes como comprador y vendedor
3. No se permite eliminar usuarios con transacciones pendientes

## 🚀 Escalabilidad
- Diseño preparado para crecimiento
- Estructura flexible para futuras características  

...
## 🇬🇧 English

# 📊 AUREUS Database Documentation

## 🎯 Overview
Relational database designed to manage a coin collection system, enabling detailed tracking of coins, collections, users, and transactions.

## 📋 Main Tables

### 1. USERS
Stores system user information.

- `USER_ID` (PK)
- `USERNAME`
- `EMAIL`
- `PASSWORD_HASH`
- `JOIN_DATE`

### 2. USER_DETAIL
Additional user information.

- `USER_ID` (FK → USERS)
- `BIRTHDATE`
- `PHONE`
- `GENDER`
- `NATIONALITY`

### 3. COLLECTION
Manages coin collections.

- `COLLECTION_ID` (PK)
- `USER_ID` (FK → USERS)
- `NAME`
- `DESCRIPTION`

### 4. COIN
Detailed record of each coin.

- `COIN_ID` (PK)
- `COIN_NAME`
- `COIN_YEAR`
- `COIN_MATERIAL`
- `COIN_WEIGHT`
- `COIN_DIAMETER`
- `ESTIMATED_VALUE`
- `ORIGIN_COUNTRY`
- `HISTORICAL_SIGNIFICANCE`
- `COLLECTION_ID` (FK → COLLECTION)

### 5. COIN_COLLECTION
Join table for many-to-many relationship between coins and collections.

- `COIN_ID` (FK → COIN)
- `COLLECTION_ID` (FK → COLLECTION)

### 6. TRANSACTION
Stores all transactions.

- `TRANSACTION_ID` (PK)
- `TRANSACTION_DATE`
- `BUYER_ID`
- `SELLER_ID`

### 7. COIN_TRANSACTION
Join table for many-to-many relationship between coins and transactions, with additional information.

- `COIN_ID` (FK → COIN)
- `TRANSACTION_ID` (FK → TRANSACTION)
- `TRANSACTION_PRICE`
- `CURRENCY`

## 🔄 Relationships

### USER and USER_DETAIL (1:1)
- Each user has associated detail information
- `USER_DETAIL.USER_ID` references `USER.USER_ID`

### USER and COLLECTION (1:N)
- One user can have multiple collections
- Each collection belongs to one user

### COLLECTION and COIN (1:N)
- A collection can contain multiple coins
- Each coin belongs to one main collection

### COIN and COLLECTION (N:M) via COIN_COLLECTION
- A coin can be part of multiple collections
- A collection can contain coins from other collections

### TRANSACTION and USER (N:M)
- A transaction involves a buyer and a seller (both users)

### TRANSACTION and COIN (N:M) via COIN_TRANSACTION
- A transaction can involve multiple coins
- Each coin can appear in multiple transactions

## 🔒 Constraints

### Referential Integrity
- Cascade delete: collections → owned coins
- Restricted delete on transactions
- Unique constraint on emails and usernames

### Business Rules
1. A coin must always have a main collection
2. Transactions must involve different users as buyer and seller
3. Users with pending transactions cannot be deleted

## 🚀 Scalability
- Designed to scale
- Flexible structure for future features