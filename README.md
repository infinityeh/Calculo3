# Prompt-Lab Engenharia de Prompt (Cálculo 3)

Este repositório contém um MVP completo para um laboratório de prompts usado em uma pesquisa universitária sobre engenharia de prompts em Cálculo 3. O projeto está dividido em duas aplicações:

- `frontend/`: Next.js 14 com NextAuth e interface de chat.
- `backend/`: Spring Boot 3.3 (Java 21) com APIs REST protegidas via Google ID Token, integração com Gemini e Google Sheets como datastore.

Um `docker-compose.yml` orquestra os serviços e um Postgres opcional (não utilizado por padrão).

## Arquitetura resumida

1. **Autenticação**: estudantes fazem login com Google (OAuth) no frontend. O id_token é armazenado na sessão NextAuth.
2. **Chat**: o frontend envia prompts para a rota interna `/api/chat-proxy` que encaminha para `POST /api/chat` do backend incluindo o `Authorization: Bearer <id_token>`.
3. **Backend**: valida o JWT com o emissor `https://accounts.google.com`, aplica o TopicGuard com palavras-chave de Cálculo 3, calcula heurísticas de rubrica, grava mensagens/feedback/consentimentos em planilhas Google e encaminha o prompt para Gemini.
4. **Persistência**: o MVP usa Google Sheets como armazenamento. Cada aba (`messages`, `feedback`, `sessions`, `consents`) é preenchida via API com um service account.
5. **Admin**: apenas e-mails na variável `APP_ADMIN_EMAILS` podem baixar o CSV em `/api/export`.

## Pré-requisitos

- Docker e Docker Compose
- Conta Google Cloud com permissões para criar OAuth Client e Service Account
- Planilha Google Sheets compartilhada com o service account (como Editor)

## Configuração de credenciais Google

### 1. Google Sheets API

1. Crie um projeto no [Google Cloud Console](https://console.cloud.google.com/).
2. Habilite a **Google Sheets API**.
3. Crie um **Service Account** e faça o download do JSON da chave.
4. Compartilhe sua planilha (Google Sheets) com o e-mail do service account como Editor.
5. Salve o JSON dentro de `backend/credentials/service-account.json` (não comite!).

A planilha deve conter as abas e colunas:

| Aba       | Colunas                                                                 |
|-----------|--------------------------------------------------------------------------|
| messages  | timestamp, userEmail, sessionId, messageId, role, text, rubricJson, latencyMs, tokens, assignmentTag |
| feedback  | timestamp, userEmail, messageId, usefulness, thumbsUp, comment           |
| sessions  | timestamp, userEmail, sessionId, assignmentTag                           |
| consents  | timestamp, userEmail, version, accepted                                  |

### 2. OAuth Web Client (Google)

1. Ainda no Google Cloud Console, acesse **APIs & Services > Credentials**.
2. Crie um **OAuth Client ID** do tipo *Web application*.
3. Configure o redirect URI: `http://localhost:3000/api/auth/callback/google`.
4. Guarde o `client_id` e `client_secret`.

## Variáveis de ambiente

Crie um arquivo `.env` na raiz (ou exporte no shell) com os valores necessários:

```
APP_ADMIN_EMAILS=professor@example.com
APP_SHEETS_SPREADSHEET_ID=1xxxxxxxxxxxxxxxxxxxx
GEMINI_API_KEY=your-gemini-api-key
NEXTAUTH_SECRET=complex-random-string
GOOGLE_CLIENT_ID=client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=client-secret
```

Outras variáveis utilizadas:

- `GEMINI_MODEL` (opcional, padrão `gemini-pro`)
- `NEXTAUTH_URL` (default `http://localhost:3000`)

Consulte `backend/.env.example` e `frontend/.env.example` para a lista completa.

## Executando com Docker Compose

1. Coloque o JSON do service account em `backend/credentials/service-account.json`.
2. Garanta que o `.env` (ou variáveis exportadas) contenha os valores mencionados.
3. Execute:

```bash
docker compose up --build
```

4. Acesse `http://localhost:3000`, faça login com Google, aceite o termo de consentimento e envie um prompt relacionado a Cálculo 3.
5. Verifique na planilha os registros nas abas correspondentes.

### Testando fluxo completo

1. Login com conta Google aprovada.
2. Na página `/chat`, enviar um prompt válido (ex.: “Calcule o fluxo usando o teorema de Stokes…”).
3. Verificar resposta do assistente e avaliar com 👍/👎 e nota de 0 a 5.
4. Tentar enviar prompt fora do escopo (ex.: “Conte uma piada”) e confirmar bloqueio.
5. Em `/admin`, tentar baixar CSV (apenas e-mails administradores).

## Desenvolvimento local (sem Docker)

### Backend

```bash
cd backend
./gradlew bootRun
```

Certifique-se de exportar as mesmas variáveis de ambiente usadas no Docker.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

## Estratégia de testes

- **Backend**: execute `./gradlew test` (mocka Gemini e Google Sheets).
- **Frontend**: use `npm run lint` e testes manuais descritos em `TEST_PLAN.md`.

## Privacidade e consentimento

- Somente o e-mail Google é persistido; nenhuma outra informação pessoal é coletada.
- O consentimento é obrigatório no primeiro acesso ao chat e fica registrado na aba `consents`.
- Para migração futura para Postgres, consulte a seção abaixo.

## Migração para Postgres (futuro)

1. Crie tabelas equivalentes a cada aba (messages, feedback, sessions, consents).
2. Substitua `SheetsClient` por repositórios Spring Data ou `JdbcTemplate` conectados a Postgres.
3. Atualize `docker-compose.yml` para definir variáveis de conexão e habilitar o serviço `postgres` (já incluído).
4. Importe dados exportando a planilha em CSV e realizando `COPY` para Postgres.

## Estrutura do repositório

```
backend/
  ├── src/main/java/... (API REST Spring Boot)
  ├── credentials/.gitkeep (coloque o JSON do service account aqui)
  ├── Dockerfile
frontend/
  ├── app/ (App Router Next.js)
  ├── Dockerfile
TEST_PLAN.md (checklist manual)
```

## Licença

Projeto acadêmico interno – ajustar conforme política institucional.
