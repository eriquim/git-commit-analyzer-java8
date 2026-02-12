
package br.com.analyzer.domain;

public class ArquivoCommit {

    private Long id;
    private Long commitId;
    private String nomeArquivo;
    private String extensao;
    private String resultadoProcessamento;

    public ArquivoCommit(Long id, Long commitId,
                         String nomeArquivo,
                         String extensao,
                         String resultadoProcessamento) {
        this.id = id;
        this.commitId = commitId;
        this.nomeArquivo = nomeArquivo;
        this.extensao = extensao;
        this.resultadoProcessamento = resultadoProcessamento;
    }

    public Long getId() { return id; }
    public Long getCommitId() { return commitId; }
    public String getNomeArquivo() { return nomeArquivo; }
    public String getExtensao() { return extensao; }
    public String getResultadoProcessamento() { return resultadoProcessamento; }
}
