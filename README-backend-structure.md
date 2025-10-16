Nova package raiz: `com.barbershop.backend`

Estrutura inicial criada para o backend Kotlin/Spring Boot (simples):

- src/main/kotlin/com/barbershop/backend/
  - config/
  - controller/api/v1/
  - dto/request/
  - dto/response/
  - entity/
  - mapper/
  - repository/
  - service/
  - exception/
  - util/

- src/test/kotlin/com/barbershop/backend/

Ponto de entrada da aplicação:
- `src/main/kotlin/com/barbershop/backend/BarbershopBackendApplication.kt` (classe `BarbershopBackendApplication`)

Como rodar o projeto (Windows, cmd.exe)

1) Verificar o wrapper do Maven:

    mvnw.cmd -v

2) Rodar a aplicação em modo desenvolvimento (executa o Spring Boot):

    mvnw.cmd spring-boot:run

3) Empacotar (build) a aplicação:

    mvnw.cmd -DskipTests package

   Isso irá gerar o JAR em `target/` (por exemplo `target/<artifact>-<version>.jar`).

4) Rodar o JAR gerado:

    java -jar target\<artifact>-<version>.jar

5) Executar testes:

    mvnw.cmd test

Perfis e configurações
- Arquivos de configuração estão em `src/main/resources` (ex.: `application.yml` ou `application.properties`).
- Para rodar com um profile específico (ex.: `dev`):

    mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev

Notas
- IDE run configuration foi atualizada para apontar para o novo main class. Se você tiver run configurations locais no IDE, pode ser necessário atualizá-las/refresh.

Próximos passos recomendados (opcionais):
- Criar esqueletos iniciais de controllers, services e repositories.
- Adicionar `application.yml` de exemplo com datasource H2 para desenvolvimento.
