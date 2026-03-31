
package br.com.analyzer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
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

        PrintStream consoleOut = System.out;
        PrintStream consoleErr = System.err;
        FileOutputStream fileOut = new FileOutputStream(new File("resultado_analise.txt"));

        System.setOut(new PrintStream(new TeeOutputStream(consoleOut, fileOut), true, "UTF-8"));
        System.setErr(new PrintStream(new TeeOutputStream(consoleErr, fileOut), true, "UTF-8"));

        List<Commit> commits = GitCommitExtractor.extrair(
                "C:/ambiente/ambiente_tjce/spaces/workspace_fluxos/git/PJE",
                "ambientes/projeto-hml-2609",
                "eric.lemos@tjce.jus.br",
                LocalDateTime.of(2026,3,01,0,0),
                LocalDateTime.of(2026,3,31,0,0),
                Arrays.asList("transition","variable","action","node","condition","swimlane","decision","task-node","description","task name")
        );
        
        processamentoFluxos(commits);
        processamentoScripts(commits);
        
    }
    
    private static void processamentoFluxos(List<Commit> commits) {
    	String pattern = "dd-MM-yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
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
    
    private static class TeeOutputStream extends OutputStream {
        private final OutputStream out1;
        private final OutputStream out2;

        public TeeOutputStream(OutputStream out1, OutputStream out2) {
            this.out1 = out1;
            this.out2 = out2;
        }

        @Override
        public void write(int b) throws IOException {
            out1.write(b);
            out2.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            out1.write(b);
            out2.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out1.write(b, off, len);
            out2.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            out1.flush();
            out2.flush();
        }

        @Override
        public void close() throws IOException {
            out1.close();
            out2.close();
        }
    }
}
