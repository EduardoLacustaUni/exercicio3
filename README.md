# Kegel & Posture Reminder App

Estrutura inicial de um aplicativo Android nativo desenvolvido em **Kotlin**, **Jetpack Compose**, **WorkManager** e padrão de arquitetura **MVVM**.

Este aplicativo foi projetado para auxiliar no bem-estar físico e mental através de lembretes periódicos e controle de hábitos em segundo plano, com foco em sessões de **Kegel** e **Meditação**.

---

## 🛠️ Arquitetura e Estrutura do Projeto

O app segue os padrões oficiais do Android recomendados pelo Google:

*   **MVVM (Model-View-ViewModel):** Separação clara de responsabilidades entre UI, lógica de negócios e persistência.
*   **Jetpack Compose:** Interface declarativa premium com animações e temas personalizados baseados em cores HSL modernas (Slate-900 / Sky-400).
*   **WorkManager:** Gerenciamento robusto de tarefas em segundo plano persistentes para agendamento preciso dos lembretes.
*   **Gradle Version Catalog (`libs.versions.toml`):** Gerenciamento centralizado de dependências do projeto.
*   **Gson & SharedPreferences:** Serialização leve de dados locais para manter as configurações do usuário e histórico de sessões.

### Estrutura de Pastas

```text
Kegel/
├── .agents/                        # Configurações personalizadas de agentes de IA
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
│           │   ├── MainActivity.kt     # Ponto de partida, consumo de intents e permissões
│           │   ├── data/
│           │   │   ├── local/
│           │   │   │   └── PreferencesManager.kt # Armazenamento SharedPreferences + Gson
│           │   │   └── model/
│           │   │       └── Event.kt              # Definições de modelos (ScheduledEvent, EventType, EventStatus)
│           │   ├── worker/
│           │   │   ├── ReminderWorker.kt       # Job principal de agendamento de notificações
│           │   │   └── MissedReminderWorker.kt # Job auxiliar para registrar sessões perdidas
│           │   ├── util/
│           │   │   └── EventSchedulerHelper.kt # Auxiliar para distribuição e randomização dos horários
│           │   └── ui/
│           │       ├── screens/
│           │       │   └── MainScreen.kt   # Dashboard interativo com design premium
│           │       ├── theme/
│           │       │   ├── Color.kt        # Cores customizadas (Slate, Sky, Teal, Red)
│           │       │   ├── Theme.kt        # Tema base do Material 3
│           │       │   └── Type.kt         # Tipografia personalizada
│           │       └── viewmodel/
│           │           └── MainViewModel.kt # Orquestração do WorkManager, persistência e estados de tela
│           └── res/
│               ├── drawable/       # Ícones e assets do launcher
│               ├── values/         # strings.xml (Língua e strings locais)
│               └── xml/            # Configurações do sistema do app
├── gradle/
│   └── libs.versions.toml          # Catálogo de versões das bibliotecas (BOM)
├── build.gradle.kts                # Configuração do Gradle nível de projeto
├── settings.gradle.kts             # Definição dos repositórios e módulos
├── gradle.properties               # Configurações do compilador do Gradle
├── AGENTS.md                       # Diretrizes técnicas para agentes de IA (Nova!)
└── README.md                       # Documentação técnica básica (Este arquivo)
```

---

## 🚀 Como Executar o Projeto Localmente

### Pré-requisitos
*   **Android Studio** Hedgehog (2023.1.1) ou superior recomendado.
*   **JDK 17** instalado e configurado na IDE.

### Passos
1.  **Abra o projeto no Android Studio:**
    Selecione a pasta `Kegel` e abra-a.
2.  **Geração do Gradle Wrapper:**
    Caso o wrapper não esteja no repositório, o Android Studio perguntará se deseja criá-lo. Confirme clicando em **OK / Recreate**. 
    Alternativamente, execute o seguinte comando no terminal se tiver o Gradle instalado globalmente:
    ```bash
    gradle wrapper
    ```
3.  **Compilar e Rodar:**
    Conecte um dispositivo Android físico com depuração USB ativa ou utilize um emulador, e clique em **Run** (ícone de play verde na barra superior).

---

## 🔒 Permissões Requeridas

*   `android.permission.POST_NOTIFICATIONS`: Requerida em dispositivos com Android 13+ (API 33) para exibir os lembretes de sessão. A rotina de solicitação dinâmica está implementada no fluxo da `MainActivity.kt`.
*   `android.permission.RECEIVE_BOOT_COMPLETED`: Registrada para reagendar tarefas e manter os lembretes ativos se o celular for reiniciado.

---

## ☁️ GitHub Actions (CI)

O workflow `.github/workflows/android.yml` realiza o build automático a cada push ou pull request na branch principal. Ele configura a JDK 17, faz o cache automático do Gradle e executa a tarefa `assembleDebug` para garantir que o código compile livre de erros.

---

## 🤖 Diretrizes para Agentes de IA

Para agentes de IA auxiliando no desenvolvimento do projeto, consulte o arquivo [AGENTS.md](file:///C:/Users/eduar/Downloads/Kegel/AGENTS.md) na raiz do projeto para detalhes sobre o estilo de código (Kotlin, Jetpack Compose), boas práticas de arquitetura (MVVM) e restrições técnicas.
