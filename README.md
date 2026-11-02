# Feeling

Plataforma de apoyo entre pares para estudiantes universitarios.

## Requisitos
- IntelliJ IDEA
- Java 17
- MySQL 8 corriendo en el puerto 3306

## Cómo ejecutar el proyecto

1. **Clonar:** en IntelliJ ir a `File → New → Project from Version Control` y pegar la URL del repositorio.
2. **Java 17:** `File → Project Structure → Project`. En **SDK** elegir un JDK 17 (si no hay, `Download JDK → 17`) y en **Language level** poner 17.
3. **Maven:** `Settings → Build, Execution, Deployment → Build Tools → Maven`. En **Maven home path** elegir **Bundled (Maven 3)**. Luego, en el panel de Maven (la "m" a la derecha), dar clic en 🔄 Reload.
4. **Contraseña de MySQL:** clic derecho sobre `FeelingApplication` → `More Run/Debug → Modify Run Configuration`. En **Environment variables** escribir:
   `DB_PASSWORD=tu_contraseña_de_mysql`
   (Si tu usuario de MySQL no es `root`, agrega también `;DB_USERNAME=tu_usuario`.)
5. **Ejecutar:** botón ▶️ en `FeelingApplication`.
6. Abrir **http://localhost:8080**.

> No hace falta crear la base de datos: si `feeling` no existe, se crea sola al ejecutar.
> Por ahora `localhost:8080` muestra una página **Whitelabel Error 404**. Es normal: la app está corriendo, pero aún no tiene páginas.

⚠️ Nunca escribas tu contraseña dentro de `application.properties`, porque ese archivo se sube a GitHub.