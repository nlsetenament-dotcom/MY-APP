# NLS – Chat con IA Personalizada 💕

App Android de chat con compañero/a virtual impulsado por Google Gemini.
Kotlin · MVVM · Material Design 3 · Room · Retrofit

---

## ⚡ Pasos para compilar el APK

### 1. Abrir en Android Studio
- File → Open → selecciona la carpeta `NLS`
- Espera la sincronización de Gradle (~2 min primera vez)

### 2. API Key (ya incluida en local.properties)
El archivo `local.properties` ya contiene:
```
GEMINI_API_KEY=AIzaSyA8q5SOQKGxP_zsf8Uct475LbGnukG2uk0
```
Gradle la inyecta en `BuildConfig.GEMINI_API_KEY` automáticamente.
⚠️ NO subas `local.properties` a Git (ya está en .gitignore).

### 3. Compilar APK
- Build → Build Bundle(s) / APK(s) → Build APK(s)
- O desde terminal: `./gradlew assembleDebug`
- APK en: `app/build/outputs/apk/debug/app-debug.apk`

### 4. Requisitos
- Android Studio Hedgehog o superior
- JDK 17
- Android SDK 26+
- Conexión a internet en el dispositivo

---

## 📁 Estructura del proyecto

```
NLS/
├── local.properties              ← API Key (NO commitear)
├── build.gradle                  ← Root Gradle
├── settings.gradle
├── gradle.properties
└── app/
    ├── build.gradle              ← Dependencias + inyección de API Key
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/nls/chat/
        │   ├── NLSApplication.kt        ← App class, aplica tema global
        │   ├── model/
        │   │   ├── CompanionConfig.kt   ← Config del compañero + buildSystemPrompt()
        │   │   └── Message.kt           ← Entidad Room del mensaje
        │   ├── data/
        │   │   ├── local/
        │   │   │   ├── AppDatabase.kt   ← Base de datos Room
        │   │   │   ├── MessageDao.kt    ← Queries SQL para mensajes
        │   │   │   ├── Converters.kt    ← Enum ↔ String para Room
        │   │   │   └── PreferencesManager.kt ← SharedPreferences wrapper
        │   │   └── repository/
        │   │       └── ChatRepository.kt ← Une Room + GeminiService
        │   ├── ai/
        │   │   ├── GeminiApi.kt         ← Interface Retrofit
        │   │   ├── GeminiModels.kt      ← DTOs request/response
        │   │   ├── GeminiService.kt     ← Lógica IA + historial + prompts
        │   │   └── RetrofitClient.kt    ← OkHttp + Retrofit singleton
        │   ├── ui/
        │   │   ├── onboarding/
        │   │   │   └── OnboardingActivity.kt ← Wizard 5 pasos
        │   │   ├── home/
        │   │   │   └── HomeActivity.kt       ← Pantalla principal
        │   │   ├── chat/
        │   │   │   ├── ChatActivity.kt       ← Chat con RecyclerView
        │   │   │   ├── ChatViewModel.kt      ← MVVM ViewModel
        │   │   │   └── MessageAdapter.kt     ← Adapter burbujas
        │   │   └── settings/
        │   │       └── SettingsActivity.kt   ← Tema + avatar + nombre
        │   └── utils/
        │       ├── Constants.kt         ← Claves y constantes
        │       ├── ThemeManager.kt      ← Cambio de tema claro/oscuro/b&w
        │       └── Extensions.kt        ← Helpers de View/Context
        └── res/
            ├── layout/
            │   ├── activity_onboarding.xml  ← Wizard completo 5 pasos
            │   ├── activity_home.xml         ← Perfil + botones
            │   ├── activity_chat.xml         ← Chat WhatsApp style
            │   ├── activity_settings.xml     ← Config tema/avatar/nombre
            │   ├── item_message_sent.xml     ← Burbuja usuario (derecha)
            │   └── item_message_received.xml ← Burbuja IA (izquierda)
            ├── drawable/
            │   ├── bubble_sent.xml           ← Burbuja rosa degradada
            │   ├── bubble_received.xml       ← Burbuja blanca
            │   ├── circle_online.xml         ← Punto verde "en línea"
            │   ├── ic_avatar_default.xml     ← Avatar SVG por defecto
            │   ├── ic_send.xml               ← Ícono enviar
            │   ├── ic_arrow_back.xml         ← Ícono volver
            │   ├── ic_more_vert.xml          ← Ícono menú
            │   └── ic_check_double.xml       ← Doble check mensaje
            ├── values/
            │   ├── colors.xml        ← Paleta de colores
            │   ├── strings.xml       ← Textos de la app
            │   └── themes.xml        ← Tema claro Material3
            ├── values-night/
            │   └── themes.xml        ← Tema oscuro Material3
            ├── anim/
            │   └── typing_dots.xml   ← Animación "escribiendo..."
            ├── menu/
            │   └── menu_chat.xml     ← Menú opciones chat
            ├── xml/
            │   └── file_paths.xml    ← Rutas FileProvider para imágenes
            └── mipmap-*/
                └── ic_launcher*.png  ← Íconos en 5 densidades
```

---

## 🧠 Flujo de la app

```
OnboardingActivity  →  HomeActivity  →  ChatActivity
     (1ª vez)            (inicio)         (chat)
                              ↓
                      SettingsActivity
                       (tema/avatar)
```

---

## 🔒 Seguridad de la API Key

La API Key NUNCA está hardcodeada en el código fuente.
Flujo seguro:
```
local.properties  →  Gradle (build time)  →  BuildConfig.GEMINI_API_KEY  →  GeminiService
```
