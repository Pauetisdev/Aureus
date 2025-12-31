```text
 █████╗   ██╗   ██╗  ██████╗   ███████╗  ██╗   ██╗  ███████╗
██╔══██╗  ██║   ██║  ██╔══██╗  ██╔════╝  ██║   ██║  ██╔════╝
███████║  ██║   ██║  ██████╔╝  █████╗    ██║   ██║  ███████╗
██╔══██║  ██║   ██║  ██╔══██╗  ██╔══╝    ██║   ██║  ╚════██║
██║  ██║  ╚██████╔╝  ██║  ██║  ███████╗  ╚██████╔╝  ███████║
╚═╝  ╚═╝   ╚═════╝   ╚═╝  ╚═╝  ╚══════╝   ╚═════╝   ╚══════╝
```

Aureus es un proyecto modular en Java para la gestión de colecciones de monedas (coins). Está diseñado con una arquitectura cliente‑servidor y módulos separados para persistencia (JPA y JDBC), modelo, repositorios y utilidades.

---

Índice (ES / EN)

| Español | English |
|---|---|
| [Resumen](#es-resumen) | [Summary](#en-summary) |
| [Arquitectura y módulos](#es-arquitectura) | [Architecture and modules](#en-architecture) |
| [Modelo de datos (entidades principales)](#es-modelo-datos) | [Data model (main entities)](#en-data-model) |
| [API HTTP (endpoints más relevantes)](#es-api-http) | [HTTP API (key endpoints)](#en-http-api) |
| [Cliente de consola: comportamiento y UX](#es-cliente-ux) | [Console client: behavior and UX](#en-console-ux) |
| [Cómo construir y ejecutar (servidor y cliente)](#es-como-construir) | [Build & run](#en-build-run) |
| [Configuración de la base de datos](#es-config-db) | [Examples (curl)](#en-examples) |
| [Ejemplos prácticos (curl)](#es-ejemplos-curl) | [Testing and development](#en-testing) |
| [Pruebas y desarrollo](#es-pruebas) | [Troubleshooting](#en-troubleshooting) |
| [Errores comunes y solución rápida](#es-errores) | [Contributing](#en-contributing) |
| [Cómo contribuir](#es-como-contribuir) | [License / Contact](#en-license-contact) |
| [Licencia y contacto](#es-licencia-contacto) |  |

---

## 🇪🇸 Español

<a id="es-resumen"></a>
### Resumen
Aureus es una aplicación Java modular que proporciona:
- Un servidor HTTP simple que expone una API REST‑like para gestionar monedas y colecciones.
- Un cliente de consola que interactúa con el servidor (menu interactivo) para realizar operaciones CRUD.

El proyecto está organizado para facilitar el cambio entre implementaciones de persistencia (JPA o JDBC) y para favorecer la separación de responsabilidades.

<a id="es-arquitectura"></a>
### Arquitectura y módulos
Breve visión de los módulos más importantes:
- `server`: Implementa el servidor HTTP y la lógica de negocio expuesta vía endpoints.
- `clients/console`: Cliente de consola (CLI) para interactuar con la API.
- `model`: Clases que definen las entidades del dominio (Coin, Collection, etc.).
- `repositories`: Interfaces y adaptadores para acceso a datos.
- `jpa` / `jdbc`: Implementaciones de persistencia.
- `utilities` / `app`: Código común, utilidades y orquestación.

Diagrama conceptual (simplificado):

Client (console) <--HTTP--> Server <--repositories--> Persistence (JPA / JDBC) <---> Database

<a id="es-modelo-datos"></a>
### Modelo de datos (entidades principales)
A continuación se indican los campos principales que maneja la API para `Coin` y `Collection`. Pueden variar según la implementación, pero sirven como guía.

Coin (ejemplo de campos):
- id (Integer)
- coinName (String)
- coinYear (Integer)
- coinMaterial (String)
- coinWeight (Double)
- coinDiameter (Double)
- estimatedValue (Double)
- originCountry (String)
- historicalSignificance (String)
- description (String)
- collectionId (Integer) ó collection (objeto anidado con id y name)

Collection (ejemplo de campos):
- id (Integer)
- collectionName (String) ó name (String)
- description (String)

> Nota: el cliente de consola está diseñado para enviar `collectionId` en POST/PUT en lugar del objeto anidado `collection` para evitar errores en el servidor (PropertyValueException).

<a id="es-api-http"></a>
### API HTTP (endpoints más relevantes)
Estos endpoints son los que utiliza el cliente de consola y los ejemplos curl abajo:
- GET /coins — lista todas las monedas (array JSON)
- GET /coins/{id} — obtiene una moneda por id (objeto JSON)
- POST /coins — crea una moneda (payload JSON con campos permitidos)
- PUT /coins/{id} — actualiza una moneda (payload JSON con campos permitidos)
- DELETE /coins/{id} — elimina una moneda

- GET /collections — lista colecciones
- GET /collections/{id} — obtiene una colección por id

Cada respuesta sigue un patrón HTTP estándar: códigos 2xx para éxito, 4xx/5xx para errores. En particular, DELETE suele responder 204 No Content; el cliente captura la información previa y la muestra al usuario para confirmación.

<a id="es-cliente-ux"></a>
### Cliente de consola: comportamiento y UX
El cliente (`clients/console`) ofrece un menú interactivo con opciones para listar, obtener por id, crear, actualizar y eliminar monedas.
Puntos clave de la UX del cliente:

| Funcionalidad | Detalle |
|---|---|
| Antes de pedir un ID para operaciones GET/UPDATE/DELETE | El cliente muestra una tabla compacta con los IDs y nombres disponibles (para que el usuario pueda escoger más fácilmente). |
| Manejo de ID no existente | Si el usuario introduce un ID que no existe, el cliente muestra un mensaje de error conciso y no vuelve a imprimir la lista completa. |
| Selección de Collection ID en create/update | En `create` y `update`: cuando se pide `Collection ID`, el cliente muestra la tabla de colecciones (id / name) — ¡solo la tabla de colecciones! — (antes había confusión entre tablas). |
| Normalización de nombre | Al crear/actualizar, el campo de nombre (`coinName`) se normaliza a Title Case (primera letra en mayúscula de cada palabra). |
| Flujo de actualización (update) | En `update`, el cliente obtiene la entidad existente, permite editar campos (dejando en blanco para mantener el valor) y después de un PUT exitoso realiza un GET para mostrar la moneda actualizada. |
| Confirmación en delete | En `delete`, antes de eliminar, el cliente solicita confirmación (en inglés). Si la eliminación es exitosa, muestra la tabla con la información del registro eliminado y un mensaje de confirmación. |

<a id="es-como-construir"></a>
### Cómo construir y ejecutar
Requisitos: JDK 21+ y Gradle wrapper están incluidos.

Desde la raíz del repositorio:

Linux / macOS:

```bash
./gradlew build
```

Windows (cmd.exe):

```bat
gradlew.bat build
```

Ejecutar el servidor (por defecto escucha en el puerto configurado en el código del servidor):

```bash
# Linux / macOS
./gradlew :server:run

# Windows (cmd.exe)
gradlew.bat :server:run
```

Ejecutar el cliente de consola (por defecto usa host=localhost, port=5000; también acepta variables de entorno o args):

```bash
# Con args
./gradlew :clients:console:run --args="localhost 5000"

# Windows (cmd.exe)
gradlew.bat :clients:console:run --args="localhost 5000"
```

O ejecutar los JAR generados:

```bash
java -jar server/build/libs/server.jar
java -jar clients/console/build/libs/console.jar
```

Variables de entorno que reconoce el cliente:
- `AUREUS_HOST` — host del servidor (default `localhost`)
- `AUREUS_PORT` — puerto del servidor (default `5000`)

<a id="es-config-db"></a>
### Configuración de la base de datos
El proyecto contiene dos implementaciones de persistencia (JPA y JDBC). Revisa `jpa` y `jdbc` para ver cómo se configuran las `datasource.properties` (hay ejemplos en `app/bin/test` y `app/bin/main` o en `docs/database`).

Si usas JPA, asegúrate de que las propiedades de `persistence.xml` o la configuración del EntityManager apunten a la base de datos correcta (URL, usuario, contraseña). Para pruebas locales puedes usar una base de datos en memoria (H2) si está configurada.

<a id="es-ejemplos-curl"></a>
### Ejemplos prácticos (curl)
Crear una moneda (POST):

```bash
curl -v -X POST http://localhost:5000/coins \
  -H "Content-Type: application/json" \
  -d '{
    "coinName": "Aureus",
    "coinYear": 125,
    "coinMaterial": "Gold",
    "coinWeight": 7.5,
    "coinDiameter": 18.0,
    "estimatedValue": 1000.0,
    "originCountry": "Romania",
    "historicalSignificance": "Imperial coin",
    "description": "Ancient gold coin",
    "collectionId": 4
  }'
```

Actualizar una moneda (PUT):

```bash
curl -v -X PUT http://localhost:5000/coins/123 \
  -H "Content-Type: application/json" \
  -d '{ "coinName": "Aureus Updated", "estimatedValue": 1200.0, "collectionId": 5 }'
```

Obtener moneda por id (GET):

```bash
curl -v http://localhost:5000/coins/123
```

Listar colecciones (GET):

```bash
curl -v http://localhost:5000/collections
```

Eliminar una moneda (DELETE):

```bash
curl -v -X DELETE http://localhost:5000/coins/123
```

<a id="es-pruebas"></a>
### Pruebas y desarrollo
- Ejecutar pruebas unitarias y de integración con Gradle:

```bash
# Ejecuta tests de todos los módulos
./gradlew test

# Windows (cmd.exe)
gradlew.bat test
```

- Para desarrollar en el cliente, edita `clients/console/src/main/java/cat/uvic/teknos/dam/aureus/Client.java` y ejecuta solo el módulo de consola:

```bash
./gradlew :clients:console:run --args="localhost 5000"
```

<a id="es-errores"></a>
### Errores comunes y solución rápida
- PropertyValueException al editar (500): suele ocurrir si se envía un objeto `collection` anidado en lugar de `collectionId` en el JSON del PUT/POST. Solución: enviar solo `collectionId` o campos primitivos (el cliente ya evita esto).
- HTTP 204 No Content en delete: el servidor puede devolver 204; el cliente ahora muestra la información previa al DELETE para que tengas una confirmación visual.
- ID no existente: si introduces un ID inválido en el cliente, verás un mensaje de error expresivo (por ejemplo: "Error: Coin not found with id 1212 (HTTP 404)").
- Problemas de JNA/pty en Windows durante compilación: si ves errores nativos en la salida del build en el IDE, intenta compilar desde cmd.exe con `gradlew.bat`.

<a id="es-como-contribuir"></a>
### Cómo contribuir
1. Haz fork del repositorio.
2. Crea una rama para tu feature: `git checkout -b feature/mi-feature`.
3. Haz cambios y commits claros.
4. Envía un Pull Request para revisión.

<a id="es-licencia-contacto"></a>
### Licencia
MIT — consulta el archivo `LICENSE`.

### Contacto
- Email: pauetisdev@gmail.com

---

## 🇬🇧 English

<a id="en-summary"></a>
### Summary
Aureus is a modular Java project to manage coin collections. It provides a simple HTTP server (REST‑like API) and a console client (CLI) to perform CRUD operations on coins and collections.

<a id="en-architecture"></a>
### Architecture and modules
- `server`: HTTP server exposing endpoints (GET/POST/PUT/DELETE for coins and collections).
- `clients/console`: Interactive console client that communicates with the server.
- `model`: Domain entities (Coin, Collection).
- `repositories`: Data access interfaces and adapters.
- `jpa` / `jdbc`: Persistence implementations.

<a id="en-data-model"></a>
### Data model (main entities)
Coin fields (example):
- id (Integer)
- coinName (String)
- coinYear (Integer)
- coinMaterial (String)
- coinWeight (Double)
- coinDiameter (Double)
- estimatedValue (Double)
- originCountry (String)
- historicalSignificance (String)
- description (String)
- collectionId (Integer) or collection (nested object)

Collection fields (example):
- id (Integer)
- collectionName or name (String)
- description (String)

Note: The console client prefers `collectionId` to avoid nested-object payloads on POST/PUT.

<a id="en-http-api"></a>
### HTTP API (key endpoints)
- GET /coins
- GET /coins/{id}
- POST /coins
- PUT /coins/{id}
- DELETE /coins/{id}
- GET /collections
- GET /collections/{id}

Responses use standard HTTP codes. DELETE may return 204 No Content; the client shows the previously fetched record for confirmation.

<a id="en-console-ux"></a>
### Console client: behavior and UX

| Feature | Detail |
|---|---|
| Interactive menu | Shows an interactive menu with options to list, get by id, create, update and delete coins. |
| Shows ids before selection | Shows lists of available coin IDs before asking for user selection. |
| Handles missing ids gracefully | If the user inputs an ID that does not exist, the client shows a concise error message and does not reprint the full list. |
| Collection ID selection in create/update | Shows collections table before asking for `Collection ID` in create/update flows. |
| coinName normalization | Normalizes `coinName` to Title Case automatically when creating/updating. |
| Update flow | After successful PUT, performs a GET and shows the updated coin. |
| Delete confirmation | On DELETE, asks for confirmation and then shows the deleted coin info along with a success message. |

<a id="en-build-run"></a>
### Build & run
Requirements: JDK 21+, Gradle wrapper.

Build all modules:

```bash
./gradlew build
```

Run server:

```bash
./gradlew :server:run
```

Run console client (passing server host and port as args):

```bash
./gradlew :clients:console:run --args="localhost 5000"
```

Or run produced JARs:

```bash
java -jar server/build/libs/server.jar
java -jar clients/console/build/libs/console.jar
```

Client env variables:
- `AUREUS_HOST` (default: localhost)
- `AUREUS_PORT` (default: 5000)

<a id="en-examples"></a>
### Examples (curl)
See the Spanish section for curl examples to create, update, get and delete coins.

<a id="en-testing"></a>
### Testing and development
Run tests:

```bash
./gradlew test
```

Run only the console module for development:

```bash
./gradlew :clients:console:run --args="localhost 5000"
```

<a id="en-troubleshooting"></a>
### Troubleshooting
- PropertyValueException on update: send `collectionId` instead of nested `collection` object.
- 204 No Content on delete: client will present deleted info (fetched before delete) for visual confirmation.
- Gradle JNA errors on Windows: use `gradlew.bat` from cmd.exe.

<a id="en-contributing"></a>
### Contributing
- Fork, branch, commit, PR.

<a id="en-license-contact"></a>
### License

MIT — see the `LICENSE` file.

### Contact
- Email: pauetisdev@gmail.com

---

Gracias por usar y contribuir a Aureus. Si quieres que añada una sección de API detallada (con todos los campos y ejemplos de respuesta) o un conjunto de scripts para lanzar entorno local (servidor + cliente), dímelo y lo incluyo.

---

## Detalles técnicos adicionales

### Internals: flujo de petición en el servidor (detallado)

Cuando el servidor recibe una petición HTTP, el flujo interno es:

1. `Server` acepta la conexión TCP y delega a `RequestRouter.handleRequest` pasando los streams.
2. `RequestRouter` parsea la petición con `HttpRequest.parse`, luego busca la ruta que coincida (method + path).
3. El handler registrado invoca al `CoinController` o `CollectionController`.
4. El `Controller` normaliza el JSON (por ejemplo, convierte `collectionId` -> `collection:{id:...}`), valida campos y delega al `CoinService`.
5. `CoinService` aplica reglas de negocio y llama al repositorio correspondiente (JPA o JDBC) para persistir/consultar.
6. El repositorio interactúa con la base de datos y retorna el resultado.
7. El `Controller` construye la respuesta JSON y el `RequestRouter` la envía al `Server`, que la escribe por el socket.

Clases clave y contratos (resumen):
- `RequestRouter`: mapea rutas a handlers. Maneja errores y convierte excepciones a respuestas HTTP (ej. `EntityNotFoundException` -> 404).
- `CoinController`: expone `getAllCoins()`, `getCoin(int)`, `createCoin(String body)`, `updateCoin(int,String body)`, `deleteCoin(int)`; normaliza diferentes formas de pasar `collectionId`.
- `CoinService`: interfaz con métodos `findAll()`, `findById(id)`, `create(coin)`, `create(coin, collectionId)`, `update(coin)`, `delete(id)`.

### Debugging: problemas comunes y cómo rastrearlos (más detallado)

- PropertyValueException en edición (500): esto suele producirse cuando el JSON enviado contiene un objeto `collection` complejo (por ejemplo, un mapa con referencias JPA) que el ORM intenta persistir. Solución:
  1. Asegúrate de enviar `collectionId` en el body (numérico) o que `collection` sea un objeto simple con `{ "id": <num> }`.
  2. El cliente `Client.java` ha sido actualizado para preferir `collectionId` y evitar enviar objetos anidados cuando sea posible.

- DELETE responde 204 No Content: si el servidor devuelve 204, el cliente ahora muestra la información de la entidad eliminada que fue recuperada antes de enviar el DELETE, así tendrás confirmación visual.

- Errores 4xx por JSON malformado: `RequestRouter` captura `JsonSyntaxException` y responde 400 Bad Request. Usa `curl -v` para comprobar body y encabezados `Content-Type: application/json`.

- Problemas en Windows con JNA/pty en la salida de Gradle: si ves `UnsatisfiedLinkError` relacionado con `jnidispatch.dll`, intenta correr Gradle desde `cmd.exe` usando `gradlew.bat` o instala/actualiza las utilidades nativas necesarias.

---

### API contract (campos esperados)

Coin (request para POST/PUT — campos permitidos / esperados):
- coinName: String (requerido)
- coinYear: Integer (requerido)
- coinMaterial: String (requerido)
- coinWeight: Double (requerido)
- coinDiameter: Double (requerido)
- estimatedValue: Double (requerido)
- originCountry: String (requerido)
- historicalSignificance: String
- description: String (opcional)
- collectionId: Integer (recomendado) o collection: { id: Integer }

Responses: el servidor devuelve objetos JSON que contienen los mismos campos y el `id` generado en creación.

---

## Developer guide: checklist para PRs y cambios

Antes de abrir un Pull Request considera:
- Añadir tests que cubran el comportamiento nuevo o corregido (módulos `server` y `clients/console`).
- Verificar que `clients/console` no envía objetos anidados a menos que la API lo requiera.
- Actualizar la documentación (`README.md`) con cualquier cambio en los endpoints o en el contrato JSON.
- Ejecutar `./gradlew build` y `./gradlew test` antes de subir la PR.

---
