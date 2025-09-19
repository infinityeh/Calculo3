# Plano de Testes MVP Prompt-Lab

## Preparação

1. Configurar `.env` e credenciais Google conforme README.
2. Garantir que a planilha tenha as abas `messages`, `feedback`, `sessions`, `consents` com as colunas exigidas.
3. Executar `docker compose up --build`.

## Checklist funcional

### Autenticação e consentimento
- [ ] Acessar `http://localhost:3000` e realizar login com Google.
- [ ] Verificar que o modal de consentimento é exibido na primeira visita ao `/chat`.
- [ ] Aceitar o consentimento e confirmar registro na aba `consents`.

### Chat e TopicGuard
- [ ] Enviar prompt válido: `Explique o teorema de Stokes aplicado a um campo vetorial em coordenadas cilíndricas.`
  - Deve retornar resposta do assistente.
  - `messages` deve conter duas linhas (USER/ASSISTANT) e a aba `sessions` deve registrar a sessão.
- [ ] Enviar prompt bloqueado: `Qual o melhor filme de 2024?`
  - Esperar mensagem de sistema “Seu prompt precisa estar relacionado...”.
  - Verificar registro SYSTEM em `messages`.

### Feedback
- [ ] Após receber resposta, clicar em 👍 e selecionar utilidade 4.
- [ ] Conferir linha na aba `feedback` com os valores enviados.

### Exportação administrativa
- [ ] Acessar `/admin` com conta administradora (listada em `APP_ADMIN_EMAILS`).
- [ ] Clicar em “Baixar CSV” e garantir que o download é autorizado.
- [ ] Tentar acessar com conta não-admin e validar resposta 403 no network log.

## Testes técnicos

- [ ] Backend: `cd backend && ./gradlew test`
- [ ] Frontend: `cd frontend && npm install && npm run lint`

## Exemplos de prompts sugeridos

1. `Usando o teorema de Stokes, calcule o fluxo do campo vetorial F(x,y,z) = (yz, xz, xy) sobre a superfície dada por z = 1 - x^2 - y^2.`
2. `Mostre passo a passo como montar uma integral tripla em coordenadas esféricas para calcular o volume de uma esfera de raio R.`
3. `Como determinar o jacobiano ao fazer mudança para coordenadas cilíndricas na integral dupla de uma região circular?`
