```ascii
 █████╗ ██╗   ██╗██████╗ ███████╗██╗   ██╗███████╗
██╔══██╗██║   ██║██╔══██╗██╔════╝██║   ██║██╔════╝
███████║██║   ██║██████╔╝█████╗  ██║   ██║███████╗
██╔══██║██║   ██║██╔══██╗██╔══╝  ██║   ██║╚════██║
██║  ██║╚██████╔╝██║  ██║███████╗╚██████╔╝███████║
╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═╝╚══════╝ ╚═════╝ ╚══════╝
```

**Idioma / Language:** [🇪🇸 Español](#-español) | [🇬🇧 English](#-english)
---

## 🇪🇸 Español

## 📋 Descripción
Aureus es un proyecto de software modular desarrollado en Java que sigue un enfoque de arquitectura limpia. Su objetivo principal es ofrecer una solución robusta y escalable para la gestión y manipulación de datos almacenados en bases de datos. El proyecto soporta la persistencia de datos mediante dos tecnologías muy utilizadas: JPA (Java Persistence API) y JDBC (Java Database Connectivity). Esta doble opción permite a los desarrolladores elegir la mejor alternativa según el caso de uso o las necesidades de rendimiento.

Diseñado pensando en la extensibilidad y mantenibilidad, Aureus separa las responsabilidades en módulos bien definidos, facilitando la incorporación de nuevas funcionalidades o la modificación de las existentes sin afectar al sistema completo. La arquitectura promueve principios de código limpio y fomenta las mejores prácticas en el diseño de software.

## 📚 Documentación

**[🔗 Ver documentación completa (Javadoc)](https://courageous-baklava-e567dd.netlify.app/)**

## 🏗️ Estructura del Proyecto
El proyecto se organiza en módulos independientes para facilitar el desarrollo y mantenimiento:

- **app**: Módulo principal que orquesta la aplicación.
- **jpa**: Implementación de la persistencia mediante Java Persistence API.
- **jdbc**: Implementación de acceso a base de datos mediante JDBC
- **model**: Definición de las entidades y modelos de datos.
- **utilities**: Funciones y clases utilitarias comunes.
- **repositories**: Capa encargada de la abstracción del acceso a datos.
- **docs**: Documentación relacionada con el proyecto.

## 🛠️ Tecnologías Utilizadas
- Java 21
- Gradle como sistema de construcción y gestión de dependencias

## ⚙️ Requisitos
- JDK 21 o superior instalado
- Gradle (se incluye el wrapper para no requerir instalación global)

## 🚀 Guía Rápida de Inicio

1. Clona el repositorio:
    ```bash
    git clone <url-del-repositorio>
    ```

2. Entra en el directorio del proyecto:
    ```bash
    cd aureus
    ```

3. Construye el proyecto:
    ```bash
    ./gradlew build
    ```

## 📄 Licencia
Este proyecto está bajo la licencia [MIT License](LICENSE).

Puedes usarlo libremente, modificarlo y distribuirlo bajo los términos de la MIT License. Para más detalles, consulta el archivo LICENSE.

## 👥 Cómo Contribuir
Las contribuciones son siempre bienvenidas. Para colaborar sigue estos pasos:

1. Haz un fork del repositorio.
2. Crea una rama para tu nueva funcionalidad:
    ```bash
    git checkout -b feature/NombreDeTuFeature
    ```
3. Realiza los commits con mensajes claros:
    ```bash
    git commit -m "Agrega descripción de tu feature"
    ```
4. Envía tu rama al repositorio remoto:
    ```bash
    git push origin feature/NombreDeTuFeature
    ```
5. Abre un Pull Request para revisión.

## 📬 Contacto
Para cualquier consulta o aporte, puedes contactar a:

- Email: pauetisdev@gmail.com
- O abrir un issue en el repositorio

---
¡Gracias por interesarte en Aureus! 🪙🚀

...
## 🇬🇧 English

## 📋 Description
Aureus is a modular software project developed in Java that follows a clean architecture approach. Its main goal is to provide a robust and scalable solution for managing and manipulating data stored in databases. The project supports data persistence using two widely adopted technologies: JPA (Java Persistence API) and JDBC (Java Database Connectivity). This dual approach allows developers to choose the best option depending on the use case or performance requirements.

Designed with extensibility and maintainability in mind, Aureus separates responsibilities into well-defined modules, making it easier to add new features or modify existing ones without affecting the entire system. The architecture promotes clean code principles and encourages best practices in software design.

## 📚 Documentation

**[🔗 See complete documentation (Javadoc)](https://courageous-baklava-e567dd.netlify.app/)**

## 🏗️ Project Structure
The project is organized into independent modules to facilitate development and maintenance:

- **app**: Main module that orchestrates the application.
- **jpa**: Persistence implementation using the Java Persistence API.
- **jdbc**: Database access implementation using JDBC.
- **model**: Definition of entities and data models.
- **utilities**: Common utility functions and classes.
- **repositories**: Layer responsible for data access abstraction.
- **docs**: Project-related documentation.

## 🛠️ Technologies Used
- Java 21
- Gradle as the build and dependency management system

## ⚙️ Requirements
- JDK 21 or higher installed
- Gradle (wrapper included, so no global installation required)

## 🚀 Quick Start Guide

1. Clone the repository:
    ```bash
    git clone <repository-url>
    ```

2. Enter the project directory:
    ```bash
    cd aureus
    ```

3. Build the project:
    ```bash
    ./gradlew build
    ```

## 📄 License
This project is licensed under the [MIT License](LICENSE).

You are free to use, modify, and distribute it under the terms of the MIT License. For more details, check the LICENSE file.

## 👥 How to Contribute
Contributions are always welcome. To collaborate, follow these steps:

1. Fork the repository.
2. Create a branch for your new feature:
    ```bash
    git checkout -b feature/YourFeatureName
    ```
3. Commit with clear messages:
    ```bash
    git commit -m "Add description of your feature"
    ```
4. Push your branch to the remote repository:
    ```bash
    git push origin feature/YourFeatureName
    ```
5. Open a Pull Request for review.

## 📬 Contact
For any questions or contributions, you can contact:

- Email: pauetisdev@gmail.com
- Or open an issue in the repository

---  
Thanks for your interest in Aureus! 🪙🚀
