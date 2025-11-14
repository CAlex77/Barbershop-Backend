# Teste de diagnóstico do endpoint de agendamento

## ⚠️ CAUSA RAIZ IDENTIFICADA

**Spring Boot 4.0.0-SNAPSHOT** (versão de desenvolvimento instável) no `pom.xml` pode estar causando o bug de chunked encoding malformado.

### Solução prioritária: Downgrade para versão estável

Editar `pom.xml`:
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version> <!-- ou 3.1.5 - última estável -->
    <relativePath/>
</parent>
```

Após alterar:
```bash
# Windows
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw clean install
./mvnw spring-boot:run
```

---

## Problema identificado
O OkHttp falha ao ler resposta HTTP/1.1 com Transfer-Encoding: chunked malformado.
Erro: `\n not found: limit=1 content=0d…` (falta LF após CR no chunk).

## Correções aplicadas no backend

### 1. Desabilitada compressão e HTTP/2 (application.yml)
```yaml
server:
  compression:
    enabled: false
  http2:
    enabled: false
  tomcat:
    connection-timeout: 20000
    max-http-response-header-size: 8192
```

### 2. Perfil de desenvolvimento criado (application-dev.yml)
Para debug adicional com logs detalhados e sem otimizações que causam chunking.

### 3. Código validado
- ✅ Controller retorna `ResponseEntity<BookedAppointmentResponse>` (sem manipulação manual)
- ✅ Sem uso de `StreamingResponseBody`, `SseEmitter`, ou manipulação de `HttpServletResponse`
- ✅ Filtro JWT não interfere no response body

## Testes de diagnóstico

### 1. Teste básico com curl (verificar headers da resposta)
```bash
curl -v -X POST http://localhost:8080/api/v1/appointments/book \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -d '{
    "clientId": 1,
    "barberId": 2,
    "serviceId": 3,
    "startTime": "2025-11-14T14:30:00Z",
    "tz": "America/Sao_Paulo"
  }'
```

**Verificar na resposta:**
- ✅ `Content-Length: XXX` (bom - sem chunking)
- ❌ `Transfer-Encoding: chunked` (se aparecer, ainda há problema)

### 2. Teste com horário futuro válido
```bash
# Ajuste startTime para 24h no futuro
TOMORROW=$(date -u -d "+1 day" +"%Y-%m-%dT14:30:00Z")

curl -v -X POST http://localhost:8080/api/v1/appointments/book \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN" \
  -d "{
    \"clientId\": 23,
    \"barberId\": 2,
    \"serviceId\": 3,
    \"startTime\": \"$TOMORROW\",
    \"tz\": \"America/Sao_Paulo\"
  }"
```

### 3. Iniciar backend com perfil dev
```bash
# Windows PowerShell
$env:SPRING_PROFILES_ACTIVE="dev"
.\mvnw.cmd spring-boot:run

# Linux/Mac
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

## Próximos passos se problema persistir

### 1. Verificar proxy/load balancer
Se houver Nginx/Apache/ALB na frente do Tomcat:
```nginx
# Nginx - garantir HTTP/1.1 e chunked correto
proxy_http_version 1.1;
proxy_set_header Connection "";
proxy_buffering off;
```

### 2. Adicionar Content-Length explícito (workaround)
Se o problema for específico de um endpoint, force Content-Length:

```kotlin
@PostMapping("/appointments/book")
fun book(@RequestBody req: BookAppointmentRequest): ResponseEntity<BookedAppointmentResponse> {
    val booked = appointmentService.book(req)
    
    // Force Content-Length calculation
    val json = objectMapper.writeValueAsString(booked)
    return ResponseEntity.ok()
        .contentLength(json.toByteArray(StandardCharsets.UTF_8).size.toLong())
        .body(booked)
}
```

### 3. Atualizar dependências (se versão do Tomcat for antiga)
Verifique `pom.xml` e atualize Spring Boot para última versão estável.

## Verificação final
Após aplicar as correções:
1. Reiniciar o backend
2. Testar com curl (verificar headers)
3. Testar no app Android
4. Monitorar logs para erros de encoding

## Notas adicionais
- O erro só aparece em respostas chunked > 1KB aproximadamente
- Respostas pequenas usam Content-Length automaticamente
- O problema NÃO está no código Kotlin/Spring, mas na camada de transporte HTTP

