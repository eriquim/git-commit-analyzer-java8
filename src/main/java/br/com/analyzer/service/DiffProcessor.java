
package br.com.analyzer.service;

import java.util.*;
import java.util.regex.*;

import br.com.analyzer.Propriedades;
import br.com.analyzer.domain.Commit;

public class DiffProcessor {

    private static final Pattern INSERT_PATTERN =
            Pattern.compile("insert\\s+into\\s+([a-zA-Z0-9_\\.]+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern UPDATE_PATTERN =
            Pattern.compile("update\\s+([a-zA-Z0-9_\\.]+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern IF_PATTERN =
            Pattern.compile("if\\s*\\((.*?)\\)", Pattern.CASE_INSENSITIVE);

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

        Set<String> inserts = new HashSet<String>();
        Set<String> updates = new HashSet<String>();
        Set<String> ifs = new HashSet<String>();

        for (String line : lines) {
            if (!line.startsWith("+") || line.startsWith("+++")) continue;

            String l = line.substring(1);

            Matcher mi = INSERT_PATTERN.matcher(l);
            if (mi.find()) inserts.add(mi.group(1));

            Matcher mu = UPDATE_PATTERN.matcher(l);
            if (mu.find()) updates.add(mu.group(1));

            Matcher mif = IF_PATTERN.matcher(l);
            if (mif.find()) ifs.add(mif.group(1));
        }

        sb.append("SQL { INSERT=").append(inserts)
          .append(" UPDATE=").append(updates)
          .append(" IF=").append(ifs)
          .append(" }");
        
        return 0;
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
