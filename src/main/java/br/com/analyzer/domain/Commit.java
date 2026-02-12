
package br.com.analyzer.domain;

import java.util.*;

public class Commit {

    private Long id;
    private String hash;
    private String autor;
    private Date data;
    private String mensagem;
    private List<ArquivoCommit> arquivos = new ArrayList<ArquivoCommit>();
    private int totalItensXml;


	public Commit(Long id, String hash, String autor, Date data, String mensagem, int totalItensXml) {
        this.id = id;
        this.hash = hash;
        this.autor = autor;
        this.data = data;
        this.mensagem = mensagem;
        this.totalItensXml = totalItensXml;
    }

    public void addArquivo(ArquivoCommit arq) {
        arquivos.add(arq);
    }
    
    public void addTotalItensXml(int totalItensArquivo) {
    	totalItensXml+=totalItensArquivo;
    }

    public Long getId() { return id; }
    public String getHash() { return hash; }
    public String getAutor() { return autor; }
    public Date getData() { return data; }
    public String getMensagem() { return mensagem; }
    public List<ArquivoCommit> getArquivos() { return arquivos; }
    public int getTotalItensXml() {
    	return totalItensXml;
    }
}
