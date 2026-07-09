# 📱 KMP App (Android & iOS)

Aplicativo móvel multiplataforma (Android e iOS) construído com **Kotlin Multiplatform (KMP)** e **Compose Multiplatform**. O projeto adota a arquitetura **MVVM (Model-View-ViewModel)** e um modelo **Offline-First**, garantindo navegação fluida sem internet e robustez no gerenciamento de estado da aplicação.

### 🚀 Principais Características
* **Autenticação e Rede:** Consumo da API `better-auth` utilizando **Ktorfit** (sintaxe idêntica ao Retrofit) para chamadas HTTP tipadas.
* **Arquitetura (MVVM):** Organização clara de responsabilidades utilizando o padrão Jetpack ViewModel oficial para KMP e `StateFlow` para reatividade.
* **Persistência Offline:** Banco de dados local reativo utilizando **Room Multiplatform** como única fonte de verdade (Single Source of Truth).
* **Segurança:** Armazenamento nativo criptografado para tokens de sessão utilizando `Multiplatform Settings`.
* **Testes Automatizados:** Testes unitários de regras de negócio nativas e automação de fluxos visuais E2E com o `Maestro`.
* **Infraestrutura e CI/CD:** Ambientes isolados (Staging e Production) com builds cruzados automatizados via Woodpecker CI.

---

## 🗺️ Roadmap do Projeto

### ✅ Concluído
- [x] **Definição da Stack:** Transição do modelo conceitual (React Native) para o ecossistema Kotlin Multiplatform.
- [x] **Mapeamento de Ferramentas:** Adoção de Room (BD), Ktorfit (Rede) e Koin (Injeção de Dependências).
- [x] **Desenho Arquitetural:** Estabelecimento do padrão MVVM em substituição ao FSD.
- [x] **Setup Inicial:** Inicialização do projeto base via KMP Wizard com Compose Multiplatform.
- [x] **Gestão de Dependências:** Configuração do `libs.versions.toml` com as bibliotecas core.
- [x] **Injeção de Dependência:** Setup do Koin no `commonMain` para gerenciar Repositórios e ViewModels.
- [x] **Navegação:** Implementação do motor de navegação para a estrutura de Tabs e Stacks.

### ⏳ Próximos Passos
- [ ] **Persistência Local:** Configuração do banco de dados Room Multiplatform e esquemas das entidades.
- [ ] **Camada de Rede:** Configuração do Ktorfit e mapeamento das rotas da API (`/sign-in`, etc).
- [ ] **Repositórios Híbridos:** Implementação da lógica de prioridade: buscar localmente (Room) ou buscar na rede (Ktorfit) e salvar localmente.
- [ ] **Armazenamento Seguro:** Configuração do Keychain (iOS) e EncryptedSharedPreferences (Android) para os tokens do `better-auth`.
- [ ] **Design System:** Criação dos componentes visuais reutilizáveis em Compose.
- [ ] **Gestão de Estado:** Construção dos ViewModels baseados em `StateFlow`.
- [ ] **Autenticação UI:** Desenvolvimento das telas de login e mapeamento de erros.
- [ ] **Integração Nativa:** Implementação de biometria via `expect/actual` (Face ID / Touch ID).
- [ ] **Migração do CI:** Adaptação das rotinas no Woodpecker CI para executar `./gradlew assemble` e `xcodebuild`.
- [ ] **Testes E2E:** Criação dos fluxos interativos de teste usando scripts do Maestro.
- [ ] **Flavors:** Configuração das variáveis de ambiente (Staging/Prod) diretamente nas Build Variants do Gradle.