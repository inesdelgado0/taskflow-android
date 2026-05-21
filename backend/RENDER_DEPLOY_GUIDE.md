# Guia para publicar a API TaskFlow no Render

Este guia é para publicar o backend `backend/` no Render, mantendo a base de dados no Supabase.

## 1. Confirmar branch

Confirmar que o backend está na main.

No GitHub, confirmar que este branch existe e tem a pasta:

backend/

## 2. Criar serviço no Render

1. Entrar em https://render.com
2. Clicar em **New +**
3. Escolher **Web Service**
4. Ligar o repositório GitHub:

inesdelgado0/taskflow-android

5. Configurar:

```text
Name: taskflow-api
Branch: simao/semana3-4
Root Directory: backend
Runtime: Node
Build Command: npm install
Start Command: npm start
```

## 3. Variáveis de ambiente no Render

No Render, em **Environment Variables**, adicionar:

```env
PORT=3000
SUPABASE_URL=https://lklawmknhcssiocmvokz.supabase.co
SUPABASE_SERVICE_ROLE_KEY=COLOCAR_A_SERVICE_ROLE_KEY_DO_SUPABASE
JWT_SECRET=COLOCAR_O_JWT_SECRET_DO_BACKEND_ENV_LOCAL
JWT_EXPIRES_IN=7d
```

Notas:

- `SUPABASE_URL` deve ficar sem `/rest/v1/`.
- `SUPABASE_SERVICE_ROLE_KEY` vem do Supabase em **Project Settings > API > service_role**.
- `JWT_SECRET` pode ser o mesmo valor que está no ficheiro local `backend/.env`.
- Nunca colocar `backend/.env` no GitHub.


## 4. Testar API publicada

Depois do deploy terminar, o Render vai gerar um URL parecido com:

```text
https://taskflow-api.onrender.com
```

Testar no browser:

```text
https://taskflow-api.onrender.com/health
```

Resposta esperada:

```json
{"status":"ok"}
```

## 5. Atualizar Android

Quando a API estiver online, alterar em:

```text
app/src/main/java/com/taskflow/app/di/NetworkModule.kt
```

Trocar:

```kotlin
private const val BASE_URL = "http://10.0.2.2:3000/v1/"
```

por:

```kotlin
private const val BASE_URL = "https://taskflow-api.onrender.com/v1/"
```

Substituir `taskflow-api.onrender.com` pelo domínio real gerado pelo Render.
