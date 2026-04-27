# MindfulShelf

MindfulShelf es una aplicacion Android desarrollada con Kotlin y Jetpack Compose cuyo objetivo es ofrecer una alternativa saludable al doomscrolling. En lugar de abrir redes sociales sin un objetivo claro, el usuario puede elegir su animo y el tiempo disponible para recibir recomendaciones de lectura profundas usando la API de Google Books.

## Objetivo del proyecto

Este proyecto final busca demostrar una implementacion moderna en Android que combine:

- interfaz declarativa con Jetpack Compose,
- arquitectura MVVM,
- consumo de API real con Retrofit,
- asincronia con Coroutines,
- navegacion con Navigation Compose,
- autenticacion y almacenamiento en Firebase,
- y una experiencia de usuario cuidada y util.

## Funcionalidades principales

- Seleccion guiada por animo y tiempo disponible para generar una busqueda con sentido.
- Consulta real a Google Books API para obtener libros recomendados.
- Pantalla de detalle con portada, autor, sinopsis y enlace de vista previa.
- Estados de interfaz `Loading`, `Error`, `Empty` y `Success`.
- Inicio de sesion y registro con email/contrasena.
- Inicio de sesion con Google.
- Guardado de libros personales en Firebase Firestore.
- Reto de "lectura de hoy" para reforzar el uso consciente de la app.
- Pantalla de configuracion para personalizar la apariencia de la aplicacion.

## Tecnologias utilizadas

- Kotlin
- Jetpack Compose
- Material 3
- MVVM
- Navigation Compose
- Retrofit
- Gson
- Kotlin Coroutines
- Firebase Authentication
- Firebase Firestore
- Coil

## Arquitectura del proyecto

La app esta organizada por capas para mantener responsabilidades separadas y facilitar el mantenimiento:

- `core`: configuracion comun, red y utilidades compartidas.
- `data`: acceso a datos remotos, Firebase y preferencias locales.
- `domain`: modelos, contratos de repositorio y casos de uso.
- `presentation`: pantallas, estados de UI, navegacion y ViewModels.
- `ui/theme`: sistema visual y tema global de la aplicacion.

## Estructura principal

```text
app/src/main/java/com/example/mindfulshelf
|
+-- core
+-- data
|   +-- preferences
|   +-- remote
|   +-- repository
+-- domain
|   +-- model
|   +-- repository
|   +-- usecase
+-- presentation
|   +-- home
|   +-- detail
|   +-- login
|   +-- navigation
|   +-- saved
|   +-- settings
+-- ui/theme
```

## Requisitos

- Android Studio actualizado.
- JDK 11.
- Un emulador Android o dispositivo fisico.
- Una clave de Google Books API.
- Un proyecto de Firebase para autenticacion y Firestore.

## Configuracion local

### 1. API key de Google Books

La clave se lee desde `local.properties`, por lo que no se sube al repositorio.

Anade esta linea en tu archivo `local.properties`:

```properties
google.books.api.key=TU_API_KEY_AQUI
```

### 2. Firebase

El archivo `google-services.json` no se incluye en el repositorio. Para ejecutar la app correctamente debes:

1. Crear o usar un proyecto en Firebase.
2. Registrar la aplicacion Android con el paquete `com.example.mindfulshelf`.
3. Descargar `google-services.json`.
4. Colocarlo dentro de:

```text
app/google-services.json
```

5. Activar en Firebase Authentication:
   - Email/Password
   - Google Sign-In
6. Crear Firestore Database.

### 3. Reglas de Firestore

Para esta version de la app, las reglas usadas durante el desarrollo son:

```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, create, update: if request.auth != null && request.auth.uid == userId;
      allow delete: if false;

      match /dailyReadings/{readingId} {
        allow read, create, update, delete: if request.auth != null && request.auth.uid == userId;
      }

      match /savedBooks/{bookId} {
        allow read, create, update, delete: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

## Ejecucion del proyecto

1. Abre el proyecto en Android Studio.
2. Sincroniza Gradle.
3. Asegurate de tener configurado `local.properties`.
4. Coloca `google-services.json` dentro de `app/`.
5. Ejecuta la aplicacion en un emulador o dispositivo.

## Pruebas

Para ejecutar los tests unitarios:

```powershell
.\gradlew.bat testDebugUnitTest
```

## Estado actual

La aplicacion implementa una primera version funcional centrada en:

- recomendacion guiada de libros,
- autenticacion con Firebase,
- biblioteca personal,
- lectura diaria,
- y personalizacion visual.

No incluye todavia persistencia offline avanzada, favoritos separados de la lectura diaria ni panel administrativo, ya que el objetivo de esta entrega ha sido priorizar claridad, utilidad real y cumplimiento de la rubrica.

## Autor

Proyecto desarrollado por Othmane como proyecto final de Android moderno con Jetpack Compose.
