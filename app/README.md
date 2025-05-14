
# Despliegue de la aplicación Minimal Travel

## Requisitos previos

- Java 17 o superior (para el backend)
- Android Studio (para el frontend)
- MySQL 8 instalado y corriendo en local
- Git
- (Opcional) Postman o similar para pruebas de la API

---

## 1. Clonar los repositorios

Clona ambos repositorios en tu máquina local:

```
git clone https://github.com/antoniolopez7217/MinimalTravel_BackEnd.git
git clone https://github.com/antoniolopez7217/MinimalTravel_FrontEnd.git
```

---

## 2. Configurar la base de datos MySQL

1. Crea una base de datos en tu servidor MySQL local. Por ejemplo:

   ```
   CREATE DATABASE minimaltravel CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. Crea un usuario y otorga permisos, o usa el usuario root.

3. Anota la IP del ordenador donde se ejecuta MySQL (usualmente `127.0.0.1` si es local, o la IP de tu red si accedes desde otro equipo).

---

## 3. Configuración del backend

1. Entra en la carpeta del backend:

   ```
   cd MinimalTravel_BackEnd
   ```

2. Edita el archivo `src/main/resources/application.properties` y configura la conexión a la base de datos. Sustituye `localhost` por la IP de tu máquina si es necesario:

   ```
   spring.datasource.url=jdbc:mysql://localhost:3306/minimal_travel
   spring.datasource.username=
   spring.datasource.password=
   ```

3. Ejecuta la aplicación Spring Boot:

   ```
   ./mvnw spring-boot:run
   ```

   o desde tu IDE favorito.

---

## 4. Configuración del frontend (Android)

1. Abre la carpeta `MinimalTravel_FrontEnd` en Android Studio.

2. En el archivo donde se configura la URL base de la API (`ApiClient.java`), asegúrate de que la IP apunta a la máquina donde corre el backend. Si ejecutas el emulador de Android, la IP de tu PC suele ser `10.0.2.2` (para emulador) o la IP de tu red local para dispositivos físicos:

   ```
   private static final String BASE_URL = "http://:8080/api/";
   ```

3. Compila y ejecuta la aplicación en el emulador o en un dispositivo físico.

---

## 5. Notas adicionales

- Si cambias la IP de la base de datos o del backend, recuerda actualizar la configuración tanto en el backend como en el frontend.
- Asegúrate de que el firewall de tu sistema permite conexiones a los puertos 3306 (MySQL) y 8080 (backend).
- Puedes utilizar herramientas como Postman para probar los endpoints del backend antes de conectar el frontend.

---

## 6. Despliegue en producción

Para un despliegue en producción, se recomienda:

- Usar una base de datos MySQL en la nube (AWS RDS, Railway, Render, etc.).
- Configurar variables de entorno seguras para las credenciales.
- Desplegar el backend en un servidor seguro (por ejemplo, AWS EC2, Heroku, etc.).
- Publicar la app Android en Google Play si es necesario.
