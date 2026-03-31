
package br.com.analyzer.service;

import java.util.*;
import java.util.regex.*;

import br.com.analyzer.Propriedades;
import br.com.analyzer.domain.Commit;

public class DiffProcessor {

    private static final Pattern INSERT_PATTERN =
            Pattern.compile("insert\\s+into\\s+([a-zA-Z0-9_\\.]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern UPDATE_PATTERN =
            Pattern.compile("update\\s+([a-zA-Z0-9_\\.]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern DELETE_PATTERN =
            Pattern.compile("delete\\s+from\\s+([a-zA-Z0-9_\\.]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern SELECT_PATTERN =
            Pattern.compile("select\\s+(?:.*?\\s+from\\s+)?([a-zA-Z0-9_\\.]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern TB_PARAMETRO_NM_PATTERN =
            Pattern.compile("nm_parametro\\s*=\\s*'([^']*)'", Pattern.CASE_INSENSITIVE);

    public static String processar(String diff, String extensao, List<String> xmlTags, Commit commit, String path) {

        StringBuilder sb = new StringBuilder();
        String[] lines = diff.split("\n");

        if (Propriedades.EXTENSAO_FLUXO.equals(extensao) && path != null && path.contains("Estat")) {
            System.out.println("========== DEBBUG O ARQUIVO: " + path + " ==========");
            for(String line : lines) {
                System.out.println("LINE: " + line);
            }
            System.out.println("======================================================");
        }

        if (Propriedades.EXTENSAO_SQL.equals(extensao)) {
        	commit.addTotalItensXml(processSql(lines, sb));
        }

        if (Propriedades.EXTENSAO_FLUXO.equals(extensao)) {
        	commit.addTotalItensXml(processXml(lines, sb, xmlTags));
        }

        return sb.toString();
    }

    private static int processSql(String[] lines, StringBuilder sb) {
        
        StringBuilder sqlFullText = new StringBuilder();
        for (String line : lines) {
            if (line.startsWith("+++") || line.startsWith("---") || line.startsWith("@@")) continue;
            if (line.startsWith("+") || line.startsWith(" ")) {
                sqlFullText.append(line.substring(1)).append(" \n");
            }
        }
        
        String[] statements = sqlFullText.toString().split(";");
        
        Set<String> casosDeUso = new LinkedHashSet<String>();
        
        for (String stmt : statements) {
            if (stmt.trim().isEmpty()) continue;
            
            Matcher mInsert = INSERT_PATTERN.matcher(stmt);
            Matcher mUpdate = UPDATE_PATTERN.matcher(stmt);
            Matcher mDelete = DELETE_PATTERN.matcher(stmt);
            Matcher mSelect = SELECT_PATTERN.matcher(stmt);
            
            if (mInsert.find()) {
            	adicionarCasoDeUso(casosDeUso, mInsert.group(1), "criar", stmt);
            } else if (mUpdate.find()) {
            	adicionarCasoDeUso(casosDeUso, mUpdate.group(1), "alterar", stmt);
            } else if (mDelete.find()) {
            	adicionarCasoDeUso(casosDeUso, mDelete.group(1), "excluir", stmt);
            } else if (mSelect.find()) {
            	adicionarCasoDeUso(casosDeUso, mSelect.group(1), "consultar", stmt);
            }
        }

        sb.append("\n  Casos de Uso Identificados:");
        if (casosDeUso.isEmpty()) {
            sb.append("\n    (Nenhum caso de uso SQL mapeado)");
        } else {
            for (String caso : casosDeUso) {
                sb.append("\n    - ").append(caso);
            }
        }
        
        return casosDeUso.size();
    }
    
    private static void adicionarCasoDeUso(Set<String> casosDeUso, String tabela, String operacao, String stmt) {
        tabela = tabela.toLowerCase();
        if ("tb_parametro".equals(tabela)) {
            String parametro = null;
            Matcher mNm = TB_PARAMETRO_NM_PATTERN.matcher(stmt);
            if (mNm.find()) {
                parametro = mNm.group(1);
            } else {
                Matcher mFallback = Pattern.compile("'([A-Z0-9_]+)'").matcher(stmt);
                if (mFallback.find()) {
                    parametro = mFallback.group(1);
                } else {
                    Matcher mStr = Pattern.compile("'([^']+)'").matcher(stmt);
                    if (mStr.find()) parametro = mStr.group(1);
                }
            }
            if (parametro != null) {
                casosDeUso.add(operacao + " o parâmetro '" + parametro + "'");
            } else {
                casosDeUso.add(operacao + " a entidade tb_parametro");
            }
        } else {
            casosDeUso.add(operacao + " a entidade " + tabela);
        }
    }

    private static int processXml(String[] lines, StringBuilder sb, List<String> tags) {

        Map<String,Integer> finalCount = new HashMap<String,Integer>();
        Map<String,Integer> hunkAdded = new HashMap<String,Integer>();
        Map<String,Integer> hunkRemoved = new HashMap<String,Integer>();
        
        int total = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            boolean isAdded = line.startsWith("+") && (!line.startsWith("+++"));
            boolean isRemoved = line.startsWith("-") && (!line.startsWith("---"));
            
            if (!isAdded && !isRemoved) {
                // Break of contiguous change block
                if (!hunkAdded.isEmpty() || !hunkRemoved.isEmpty()) {
                    Set<String> allTags = new HashSet<String>(hunkAdded.keySet());
                    allTags.addAll(hunkRemoved.keySet());
                    for (String tag : allTags) {
                        int added = hunkAdded.getOrDefault(tag, 0);
                        int removed = hunkRemoved.getOrDefault(tag, 0);
                        int operations = Math.max(added, removed);
                        if (operations > 0) {
                            finalCount.put(tag, finalCount.getOrDefault(tag, 0) + operations);
                            total += operations;
                        }
                    }
                    hunkAdded.clear();
                    hunkRemoved.clear();
                }
                continue;
            }
            
            String l = line.substring(1).toLowerCase().trim();
            if (l.startsWith("</") || l.startsWith("<!") || !l.startsWith("<")) continue;
            
            for (String t : tags) {
                String prefix = "<" + t.toLowerCase();
                if (l.startsWith(prefix)) {
                    boolean match = false;
                    if (l.length() == prefix.length()) {
                        match = true;
                    } else {
                        char next = l.charAt(prefix.length());
                        match = (next == ' ' || next == '>' || next == '/' || next == '=' || next == '\t');
                    }
                    
                    if (match) {
                        if (isAdded) {
                            hunkAdded.put(prefix, hunkAdded.getOrDefault(prefix, 0) + 1);
                        } else {
                            hunkRemoved.put(prefix, hunkRemoved.getOrDefault(prefix, 0) + 1);
                        }
                        break;
                    }
                }
            }
        }
        
        // Final flush if diff ends with an active block
        if (!hunkAdded.isEmpty() || !hunkRemoved.isEmpty()) {
            Set<String> allTags = new HashSet<String>(hunkAdded.keySet());
            allTags.addAll(hunkRemoved.keySet());
            for (String tag : allTags) {
                int added = hunkAdded.getOrDefault(tag, 0);
                int removed = hunkRemoved.getOrDefault(tag, 0);
                int operations = Math.max(added, removed);
                if (operations > 0) {
                    finalCount.put(tag, finalCount.getOrDefault(tag, 0) + operations);
                    total += operations;
                }
            }
        }

        sb.append("\n    XML ").append(finalCount);
        sb.append("\n    TOTAL: ").append(total);
        return total;
    }
}
