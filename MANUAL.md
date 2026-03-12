# Manual de Execução e Configuração - Git Commit Analyzer

Este manual descreve como alterar os parâmetros de análise na classe `Main` e como executar o projeto.

## 1. Alteração dos Parâmetros da Classe `Main`

A configuração da análise é feita diretamente no código, alterando os argumentos passados para o método `GitCommitExtractor.extrair(...)` dentro do método `main` da classe `br.com.analyzer.Main`.

Abra o arquivo `src/main/java/br/com/analyzer/Main.java` e localize o seguinte trecho de código:

```java
List<Commit> commits = GitCommitExtractor.extrair(
        "C:/ambiente/ambiente_tjce/spaces/workspace_fluxos/git/PJE", // 1. Caminho do repositório
        "ambientes/projeto-hml-2609",                                // 2. Branch
        "eric.lemos@tjce.jus.br",                                    // 3. Email do autor
        LocalDateTime.of(2026,2,10,0,0),                             // 4. Data Inicial
        LocalDateTime.of(2026,2,28,0,0),                             // 5. Data Final
        Arrays.asList("transition","variable",...)                   // 6. Tags XML
);
```

### Detalhamento dos Parâmetros

1. **Caminho do repositório local:** `String` contendo o caminho absoluto da pasta do repositório Git local que será analisado.
   * *Exemplo:* `"C:/ambiente/ambiente_tjce/spaces/workspace_fluxos/git/PJE"`
2. **Branch de análise:** `String` com o nome da branch onde os commits serão buscados.
   * *Exemplo:* `"ambientes/projeto-hml-2609"` ou `"master"`
3. **Email do autor:** `String` com o email do autor dos commits para filtrar a busca (apenas commits deste autor serão analisados).
   * *Exemplo:* `"eric.lemos@tjce.jus.br"`
4. **Data e Hora Inicial:** Objeto `LocalDateTime` representando o início do período de busca.
   * *Construtor:* `LocalDateTime.of(Ano, Mês, Dia, Hora, Minuto)`
   * *Exemplo:* `LocalDateTime.of(2026, 2, 10, 0, 0)`
5. **Data e Hora Final:** Objeto `LocalDateTime` representando o fim do período de busca.
   * *Construtor:* `LocalDateTime.of(Ano, Mês, Dia, Hora, Minuto)`
   * *Exemplo:* `LocalDateTime.of(2026, 2, 28, 0, 0)`
6. **Lista de Tags XML:** Lista de `String` (`Arrays.asList(...)`) contendo as tags XML específicas que serão contabilizadas durante a análise dos arquivos.
   * *Exemplo:* `Arrays.asList("transition", "variable", "action", "node")`

---

## 2. Como Executar o Projeto

O projeto é baseado em Java 8 e Maven. Existem duas formas principais de executá-lo:

### Opção A: Execução via IDE (Eclipse, IntelliJ, VS Code)
Esta é a maneira mais simples se você já estiver com o código aberto.

1. Na sua IDE, aguarde ou force o Maven a baixar todas as dependências do projeto (como o JGit).
2. Navegue até a classe `src/main/java/br/com/analyzer/Main.java`.
3. Clique com o botão direito no arquivo ou direto na assinatura do método `main` e selecione **"Run As > Java Application"** (a opção pode variar ligeiramente dependendo da IDE, como o ícone de 'Play' no VS Code/IntelliJ).
4. O resultado da análise (Processamento de Fluxos e Scripts) aparecerá de forma estruturada no console/terminal integrado da IDE.

### Opção B: Execução via Linha de Comando (Maven)
O projeto já conta com o `exec-maven-plugin` devidamente configurado no `pom.xml`, o que permite a execução direta pelo terminal usando o Maven.

1. Abra um terminal de sua preferência (Prompt de Comando, PowerShell, Bash).
2. Navegue até o diretório raiz do projeto (onde se encontra o arquivo `pom.xml`):
   ```bash
   cd C:/ambiente/ambiente_tjce/spaces/workspace_legacy/github/git-commit-analyzer-java8
   ```
3. Compile o código atual (necessário sempre que alterar parâmetros na classe `Main`):
   ```bash
   mvn clean compile
   ```
4. Execute o projeto usando o plugin do Maven:
   ```bash
   mvn exec:java
   ```
5. O terminal exibirá a saída completa gerada pela classe `Main`.
