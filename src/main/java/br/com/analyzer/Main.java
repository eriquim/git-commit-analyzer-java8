
package br.com.analyzer;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import br.com.analyzer.domain.ArquivoCommit;
import br.com.analyzer.domain.Commit;
import br.com.analyzer.service.GitCommitExtractor;

public class Main {

    public static void main(String[] args) throws Exception {

        List<Commit> commits = GitCommitExtractor.extrair(
                "C:/ambiente/ambiente_tjce/spaces/workspace_fluxos/git/PJE",
                "ambientes/projeto-hml-2609",
                "eric.lemos@tjce.jus.br",
                LocalDateTime.of(2026,2,10,0,0),
                LocalDateTime.of(2026,2,28,0,0),
                Arrays.asList("transition","variable","action","node","condition","swimlane","decision","task-node","description","task name")
        );
        
        processamentoFluxos(commits);
        processamentoScripts(commits);
        
    }
    
    private static void processamentoFluxos(List<Commit> commits) {
    	String pattern = "dd-MM-yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern)
        System.out.println("----------------------------------------- Processamento de Fluxos -----------------------------------------------");

        for (Commit c : commits) {
        	System.out.println("Mensagem: "+ c.getMensagem());
            System.out.println("Commit: " + c.getHash() + " - Data do commit: " + simpleDateFormat.format(c.getData()) );
            List<ArquivoCommit> listaFluxos = c.getArquivos().stream().filter(obj ->obj.getExtensao().equals(Propriedades.EXTENSAO_FLUXO)).collect(Collectors.toList());
            if(listaFluxos.isEmpty()) {
            	System.out.println("(Commit sem alteração de Fluxos JBPM)");
            	
            } else {
	            for (ArquivoCommit a : listaFluxos) {
	                System.out.println("  " + a.getNomeArquivo() + " -> " + a.getResultadoProcessamento());
	            }
	            System.out.println("  Total de Itens do Commit: " + c.getTotalItensXml());
            }
            System.out.println("\n\n");
        }
    }
    private static void processamentoScripts(List<Commit> commits) {
    	String pattern = "dd-MM-yyyy";
    	SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    	System.out.println("----------------------------------------- Processamento de Scripts -----------------------------------------------");
    	
    	for (Commit c : commits) {
    		System.out.println("Mensagem: "+ c.getMensagem());
    		System.out.println("Commit: " + c.getHash() + " - Data do commit: " + simpleDateFormat.format(c.getData()) );
    		List<ArquivoCommit> listaScripts = c.getArquivos().stream().filter(obj ->obj.getExtensao().equals(Propriedades.EXTENSAO_SQL)).collect(Collectors.toList());
    		if(listaScripts.isEmpty()) {
    			System.out.println("(Commit sem alteração de scritps SQL)");
    			
    		} else {
	    		for (ArquivoCommit a : listaScripts) {
	    			System.out.println("  " + a.getNomeArquivo() + " -> " + a.getResultadoProcessamento());
	    		}
	    		System.out.println("  Total de Itens do Commit: " + c.getTotalItensXml());
    		}
    		System.out.println("\n\n");
    	}
    }
}
