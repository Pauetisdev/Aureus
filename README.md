```text
 █████╗   ██╗   ██╗  ██████╗   ███████╗  ██╗   ██╗  ███████╗
██╔══██╗  ██║   ██║  ██╔══██╗  ██╔════╝  ██║   ██║  ██╔════╝
███████║  ██║   ██║  ██████╔╝  █████╗    ██║   ██║  ███████╗
██╔══██║  ██║   ██║  ██╔══██╗  ██╔══╝    ██║   ██║  ╚════██║
██║  ██║  ╚██████╔╝  ██║  ██║  ███████╗  ╚██████╔╝  ███████║
╚═╝  ╚═╝   ╚═════╝   ╚═╝  ╚═╝  ╚══════╝   ╚═════╝   ╚══════╝
```
---

# Aureus — Gestión avanzada de colecciones de monedas

---

## Índice

- Descripción
- Arquitectura del proyecto
- Módulos principales
- Historial de versiones (Cronología)
- Requisitos previos
- Configuración de seguridad
- Instalación rápida y ejecución
- Resumen de la interfaz HTTP
- Configuración de la base de datos (MySQL)
- Ejemplos prácticos (curl)
- Contribuir
- Licencia y contacto

---

## Descripción

Aureus es una plataforma modular orientada a la gestión, compraventa y conservación de monedas antiguas y colecciones numismáticas. Está pensada tanto para coleccionistas como para comerciantes y casas de subastas que necesitan un sistema profesional para catalogar piezas, publicar anuncios de venta, gestionar ofertas y mantener el historial de procedencia y tasación.

Funcionalidades y casos de uso principales:
- Catálogo y gestión de inventario: registrar monedas con metadatos (año, material, peso, diámetro, estado de conservación, valor estimado, fotografías, etc.).
- Publicación de anuncios y gestión de ventas: crear listados con precio, opción de aceptar ofertas y registrar el historial de transacciones.
- Ofertas y subastas: recibir y procesar ofertas de compradores, soporte para procesos de subasta o venta directa.
- Valoración y autenticidad: almacenar valoraciones, funciones de hash para integridad y trazabilidad de la procedencia (provenance).
- Gestión de usuarios y permisos: perfiles para coleccionistas, vendedores y administradores; control de acceso a operaciones sensibles.
- Seguridad y firma: uso de keystores y cifrado para proteger credenciales, firmas y operaciones críticas.
- Integraciones y extensibilidad: API HTTP para integraciones externas, cliente de consola (CLI) para usos automatizados y soporte para distintas implementaciones de persistencia (JPA / JDBC).

Aureus busca ofrecer una solución completa y profesional para el ciclo de vida de una moneda en el mercado (catalogación, tasación, venta, historial y custodia), con atención especial en la seguridad y la integridad de los datos.

---

## Arquitectura del proyecto

Arquitectura general (simplificada):

Servidor (HTTP)  <--->  Servicios / Controladores  <--->  Repositorios  <--->  Persistencia (JPA / JDBC)  <--->  Base de datos

Diagrama de texto:

```
+----------------+      +--------------------------+      +-------------------------+      +-------------------------------+
| Clientes (CLI) | <-->  | Servidor / Controladores | <-->  | Repositorios (API)     | <-->  | Persistencia (JPA)           |
|                |      |  (enrutamiento de req.)  |      |  (interfaces)          |      |  o implementación JDBC        |
+----------------+      +--------------------------+      +-------------------------+      +-------------------------------+
                                                             |
                                                             v
                                                     +-------------------------+
                                                     | Base de datos (MySQL)   |
                                                     +-------------------------+
```

Patrones y responsabilidades:
- Enrutamiento de peticiones: parsea peticiones HTTP y mapea rutas a controladores.
- Controladores: validación, normalización (por ejemplo, convertir collectionId a referencia interna) y construcción de respuestas.
- Servicios: reglas de negocio y orquestación de repositorios.
- Repositorios: interfaz única que oculta la implementación de persistencia (JPA / JDBC).

---

## Módulos principales

- `app` — Orquestación y utilidades compartidas.
- `server` — Implementación del servidor HTTP y controladores.
- `clients/console` — Cliente de consola (CLI) para uso local.
- `model` — Entidades del dominio (Coin, Collection, etc.).
- `repositories` — Interfaces y contratos de acceso a datos.
- `jpa` — Implementación de persistencia usando JPA / Hibernate.
- `jdbc` — Implementación alternativa usando JDBC directo.
- `utilities` — Funciones auxiliares, logs, utilidades de parsing.

---

## Historial de versiones (Cronología)

Este proyecto ha evolucionado a través de varias versiones; a continuación el historial (etiquetas exactas):

- **v1.0**: Versión inicial con la estructura básica de módulos.
- **v2.0**: Implementación básica de la arquitectura cliente-servidor.
- **v2.1.0**: Introducción de concurrencia mediante el uso de threads en la comunicación.
- **v2.1.1**: Implementación de integridad de datos mediante funciones de hash.
- **v2.1.2**: Implementación de seguridad avanzada con encriptación asimétrica (claves pública/privada).

---

## Requisitos previos

- Java 17 o superior (JDK 17+)
- MySQL (o compatible) para persistencia relacional
- Gradle (se recomienda usar el Gradle Wrapper incluido)

Nota: el proyecto incluye un Gradle Wrapper para evitar dependencias del entorno global.

---

## Configuración de seguridad

Aureus utiliza keystores y mecanismos criptográficos en algunas operaciones (firma / cifrado). Para que la gestión de keystores funcione correctamente debes definir la contraseña mediante la variable de entorno:

- `AUREUS_KS_PASSWORD` — contraseña usada para abrir los keystores del proyecto.

Ejemplo (Linux / macOS):

```bash
export AUREUS_KS_PASSWORD="mi-contraseña-secreta"
```

Ejemplo (Windows cmd.exe):

```bat
set AUREUS_KS_PASSWORD=mi-contraseña-secreta
```

Asegúrate de mantener esta variable segura en tus entornos de CI / despliegue.

---

## Instalación rápida y ejecución

Construir todo el proyecto (usar Gradle Wrapper):

Linux / macOS:

```bash
./gradlew build
```

Windows (cmd.exe):

```bat
gradlew.bat build
```

Ejecutar el servidor (por defecto escucha en el puerto configurado en el módulo `server`):

Linux / macOS:

```bash
./gradlew :server:run
```

Windows (cmd.exe):

```bat
gradlew.bat :server:run
```

Ejecutar el cliente de consola (ejemplo con argumentos host y puerto):

Linux / macOS:

```bash
./gradlew :clients:console:run --args="localhost 5000"
```

Windows (cmd.exe):

```bat
gradlew.bat :clients:console:run --args="localhost 5000"
```

También puedes generar y lanzar los JARs producidos por los módulos:

```bash
java -jar server/build/libs/server.jar
java -jar clients/console/build/libs/console.jar
```

---

## Resumen de la interfaz HTTP

Puntos finales principales (convención de estilo REST):

- GET /coins — lista todas las monedas
- GET /coins/{id} — obtiene una moneda por id
- POST /coins — crea una moneda (usar `collectionId` en el body)
- PUT /coins/{id} — actualiza una moneda (usar `collectionId` en el body)
- DELETE /coins/{id} — elimina una moneda

- GET /collections — lista colecciones
- GET /collections/{id} — obtiene una colección por id

Contratos y recomendaciones:
- Para evitar errores de persistencia (PropertyValueException), envía `collectionId` como entero en el JSON de POST/PUT en lugar de un objeto anidado `collection`.
- Las respuestas usan códigos HTTP estándar (200, 201, 204, 400, 404, 500).

---

## Configuración de la base de datos (MySQL)

Ejemplo de `datasource.properties` mínimo para MySQL:

```
jdbc.url=jdbc:mysql://localhost:3306/aureus_db?useSSL=false&serverTimezone=UTC
jdbc.username=aureus_user
jdbc.password=tu_password
jdbc.driver=com.mysql.cj.jdbc.Driver
```

- Crea la base de datos y el usuario antes de iniciar el servidor.
- Ajusta las propiedades en los archivos de configuración de `jpa` o `jdbc` según el módulo que uses.

---

## Ejemplos prácticos (curl)

Crear una moneda (POST):

```bash
curl -v -X POST http://localhost:5000/coins \
  -H "Content-Type: application/json" \
  -d '{
    "coinName": "Aureus",
    "coinYear": 125,
    "coinMaterial": "Oro",
    "coinWeight": 7.5,
    "coinDiameter": 18.0,
    "estimatedValue": 1000.0,
    "originCountry": "Rumanía",
    "historicalSignificance": "Moneda imperial",
    "description": "Moneda antigua de oro",
    "collectionId": 4
  }'
```

Obtener moneda por id (GET):

```bash
curl -v http://localhost:5000/coins/123
```

Actualizar una moneda (PUT):

```bash
curl -v -X PUT http://localhost:5000/coins/123 \
  -H "Content-Type: application/json" \
  -d '{ "coinName": "Aureus Actualizada", "estimatedValue": 1200.0, "collectionId": 5 }'
```

Eliminar una moneda (DELETE):

```bash
curl -v -X DELETE http://localhost:5000/coins/123
```

---

## Pruebas y desarrollo

Ejecutar tests con Gradle Wrapper:

```bash
./gradlew test
```

(Windows)

```bat
gradlew.bat test
```

Para desarrollar el cliente de consola editar `clients/console/src/main/java` y ejecutar sólo ese módulo con `:clients:console:run`.

---

## Contribuir

Si deseas contribuir, sigue el flujo habitual:

1. Haz fork del repositorio.
2. Crea una rama: `git checkout -b feature/mi-feature`.
3. Añade pruebas y documentación cuando cambies comportamiento público.
4. Envía un Pull Request describiendo el cambio y su justificación.

Por favor respeta las convenciones de código y añade tests para cambios en `server` y `clients/console`.

---

## Licencia y contacto

- Licencia: MIT — ver el fichero `LICENSE`.
- Contacto: pauetisdev@gmail.com

---


## Guía para desarrolladores: checklist para PRs y cambios

Antes de abrir un Pull Request considera:
- Añadir tests que cubran el comportamiento nuevo o corregido (módulos `server` y `clients/console`).
- Verificar que `clients/console` no envía objetos anidados a menos que la interfaz lo requiera.
- Actualizar la documentación (`README.md`) con cualquier cambio en los puntos finales o en el contrato JSON.
- Ejecutar `./gradlew build` y `./gradlew test` antes de subir la PR.

---
