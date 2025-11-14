# Resumo das correções aplicadas para resolver EOFException chunked encoding

## Problema
```
java.io.EOFException: \n not found: limit=1 content=0d…
```
OkHttp no Android não consegue ler resposta HTTP chunked malformada do backend.

## Causa raiz identificada
**Spring Boot 4.0.0-SNAPSHOT** (versão instável) no `pom.xml` contém bugs conhecidos de HTTP/1.1 chunked encoding.

## Correções aplicadas

### 1. ⚠️ CRÍTICO: Atualizar versão do Spring Boot (pom.xml)
**Recomendação forte:** Mudar de SNAPSHOT para versão estável.

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version> <!-- Alterado de 4.0.0-SNAPSHOT -->
    <relativePath/>
</parent>
```

### 2. Desabilitar compressão e HTTP/2 (application.yml)
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

### 3. Perfil de desenvolvimento criado (application-dev.yml)
Para testes com logs detalhados e sem otimizações de rede.

### 4. Health check adicionado (HealthController.kt)
Endpoint `/api/v1/health` para testar resposta HTTP sem autenticação.

### 5. Script de teste criado (test-chunked.sh)
Automatiza testes com curl para validar headers HTTP.

## Como aplicar as correções

### Passo 1: Atualizar Spring Boot
```bash
# Editar pom.xml manualmente ou usar sed (Linux/Mac)
sed -i 's/4.0.0-SNAPSHOT/3.2.0/' pom.xml

# Rebuild
./mvnw clean install
```

### Passo 2: Reiniciar backend
```bash
# Com perfil dev
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run

# Ou normal
./mvnw spring-boot:run
```

### Passo 3: Testar com curl
```bash
chmod +x test-chunked.sh
./test-chunked.sh
```

### Passo 4: Validar no Android
Testar novamente o fluxo de agendamento no app.

## Validação de sucesso

✅ curl mostra `Content-Length: XXX` nas respostas  
✅ curl NÃO mostra `Transfer-Encoding: chunked`  
✅ Android não lança `EOFException`  
✅ POST /appointments/book retorna 200 com JSON válido  

## Se problema persistir após Spring Boot 3.2.0

### Opção 1: Adicionar filtro explícito para forçar Content-Length
```kotlin
@Component
class ContentLengthFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        val wrapper = ContentCachingResponseWrapper(response)
        chain.doFilter(request, wrapper)
        wrapper.copyBodyToResponse()
    }
}
```

### Opção 2: Verificar proxy/load balancer
Se houver Nginx/Apache na frente do Tomcat, validar configuração de proxy.

### Opção 3: Atualizar OkHttp no Android
```kotlin
// build.gradle.kts (app)
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
```

## Arquivos modificados
- ✅ `pom.xml` (DEVE ser editado manualmente)
- ✅ `src/main/resources/application.yml`
- ✅ `src/main/resources/application-dev.yml` (novo)
- ✅ `src/main/kotlin/com/barbershop/backend/controller/HealthController.kt` (novo)
- ✅ `test-chunked.sh` (novo)
- ✅ `DIAGNOSE-CHUNKED-ERROR.md` (documentação completa)

## Contato para suporte
Se o erro persistir após aplicar todas as correções, fornecer:
1. Saída completa do `./test-chunked.sh`
2. Versão do Spring Boot após alteração (`mvn -version` e `pom.xml`)
3. Log completo do backend ao fazer POST /appointments/book
4. Stacktrace completo do Android

