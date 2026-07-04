# Kegel & Posture Reminder App

Estrutura inicial de um aplicativo Android nativo desenvolvido do zero com **Kotlin**, **Jetpack Compose**, **WorkManager** e padrão de arquitetura **MVVM**.

## 📱 Sobre o App

Este aplicativo foi projetado para auxiliar no bem-estar físico e mental através de lembretes periódicos e controle de hábitos em segundo plano. Ele foca em duas práticas essenciais:
1. **Descostar os dentes:** Evitar a tensão crônica na mandíbula que causa DTM (Disfunção Temporomandibular) e bruxismo.
2. **Alinhamento de Postura:** Lembrete para ajustar os ombros e coluna.

---

## 🛠️ Arquitetura e Estrutura do Projeto

O app segue os padrões oficiais do Android recomendados pelo Google:
*   **MVVM (Model-View-ViewModel):** Separação de responsabilidades bem definida.
*   **Jetpack Compose:** Interface declarativa premium com animações e temas personalizados de alto padrão.
*   **WorkManager:** Gerenciamento robusto de tarefas em segundo plano persistentes para agendamento dos lembretes.
*   **Gradle Version Catalog (`libs.versions.toml`):** Gerenciamento centralizado e moderno de dependências.

### Estrutura de Pastas

```text
Kegel/
├── .github/
│   └── workflows/
│       └── android.yml            # CI para build automático no GitHub Actions
├── app/
│   ├── build.gradle.kts           # Configuração de dependências do módulo do app
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml # Declaração de permissões de notificação e boot
│           ├── java/com/kegel/app/
│           │   ├── KegelApplication.kt # Inicialização do canal de notificações
│           │   ├── MainActivity.kt     # Ponto de partida e controle de permissões
│           │   ├── worker/
│           │   │   └── ReminderWorker.kt # Job em segundo plano para notificações
│           │   └── ui/
│           │       ├── screens/
│           │       │   └── MainScreen.kt   # Dashboard interativo com design premium
│           │       ├── theme/
│           │       │   ├── Color.kt        # Paleta HSL moderna (Slate-900 / Sky-400)
│           │       │   ├── Theme.kt        # Tema base do Material 3
│           │       │   └── Type.kt         # Tipografia personalizada
│           │       └── viewmodel/
│           │           └── MainViewModel.kt # Orquestração do WorkManager e estados de tela
│           └── res/
│               ├── drawable/       # Ícones e backgrounds vetoriais do launcher
│               ├── values/         # strings.xml e themes.xml
│               └── xml/            # Configurações de backup e restauração
├── gradle/
│   └── libs.versions.toml          # Catálogo de versões das bibliotecas (BOM)
├── build.gradle.kts                # Configuração do Gradle nível de projeto
├── settings.gradle.kts             # Definição dos repositórios e módulos
└── gradle.properties               # Configurações do compilador do Gradle
```

---

## 🚀 Como Executar o Projeto Localmente

1. **Abra o projeto no Android Studio:**
   Selecione a pasta `Kegel` e abra-a no Android Studio (versão Hedgehog ou superior recomendada).
2. **Geração automática do Gradle Wrapper:**
   Como as ferramentas e IDEs Android geram o binário do Gradle Wrapper (`gradle-wrapper.jar`) localmente de forma segura, o Android Studio identificará que ele está ausente e perguntará se deseja criá-lo. Clique em **OK / Recreate**.
   *Caso prefira via linha de comando (com Gradle instalado no seu path):*
   ```bash
   gradle wrapper
   ```
3. **Execute o app:**
   Selecione o seu dispositivo ou emulador e clique no botão **Run** (ícone de play verde).

---

## ☁️ GitHub Actions & CI Workflow

O projeto já inclui um workflow de integração contínua (CI) em `.github/workflows/android.yml`. Ao subir a pasta para o seu repositório do GitHub:
1. O GitHub Actions detectará automaticamente o arquivo YML.
2. Ele fará o download e cache do JDK 17 e do Gradle correspondente.
3. Executará o comando `gradle assembleDebug` para certificar que o build do projeto está correto e livre de erros. O workflow foi configurado para baixar o Gradle 8.7 globalmente no runner da Action, eliminando a necessidade de arquivos de script locais (`gradlew`) ou do binário (`gradle-wrapper.jar`) no repositório.

---

## 🔒 Permissões Declaradas

*   `android.permission.POST_NOTIFICATIONS`: Necessária no Android 13+ (API 33) para enviar lembretes. O app possui rotina nativa na `MainActivity.kt` para solicitar essa permissão dinamicamente com as melhores práticas de UX.
*   `android.permission.RECEIVE_BOOT_COMPLETED`: Garante que os lembretes persistirão mesmo se o dispositivo do usuário for reiniciado.
