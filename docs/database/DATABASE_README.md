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
- `CREATION_DATE`
- `LAST_LOGIN`

### 2. COLLECTIONS
Gestiona las colecciones de monedas.

- `COLLECTION_ID` (PK)
- `USER_ID` (FK → USERS)
- `NAME`
- `DESCRIPTION`
- `CREATION_DATE`
- `VISIBILITY` (PUBLIC/PRIVATE)

### 3. COINS
Registro detallado de cada moneda.

- `COIN_ID` (PK)
- `COLLECTION_ID` (FK → COLLECTIONS)
- `NAME`
- `YEAR`
- `COUNTRY`
- `MATERIAL`
- `DENOMINATION`
- `CONDITION`
- `DESCRIPTION`
- `REGISTRATION_DATE`

### 4. COIN_COLLECTION
Tabla puente para relación muchos a muchos entre monedas y colecciones.

- `COIN_ID` (FK → COINS)
- `COLLECTION_ID` (FK → COLLECTIONS)
- `ADDITION_DATE`

### 5. TRANSACTIONS
Registra todas las transacciones de monedas.

- `TRANSACTION_ID` (PK)
- `SELLER_ID` (FK → USERS)
- `BUYER_ID` (FK → USERS)
- `COIN_ID` (FK → COINS)
- `DATE`
- `PRICE`
- `STATUS`

## 🔄 Relaciones

### Usuarios y Colecciones (1:N)
- Un usuario puede tener múltiples colecciones
- Cada colección pertenece a un único usuario
- Relación mediante `USER_ID` en tabla `COLLECTIONS`

### Colecciones y Monedas
1. **Relación Principal (1:N)**
    - Una colección puede contener múltiples monedas
    - Cada moneda tiene una colección principal
    - Mediante `COLLECTION_ID` en tabla `COINS`

2. **Relación Secundaria (N:M)**
    - Una moneda puede aparecer en múltiples colecciones
    - Una colección puede contener monedas de otras colecciones
    - Mediante tabla puente `COIN_COLLECTION`

### Transacciones (N:M con USERS)
- Conecta dos usuarios (comprador y vendedor)
- Vincula con la moneda específica
- Registra detalles de la transacción

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
