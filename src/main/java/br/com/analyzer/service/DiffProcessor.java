
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

    public static String processar(String diff, String extensao, List<String> xmlTags, Commit commit) {

        StringBuilder sb = new StringBuilder();
        String[] lines = diff.split("\n");

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

        Map<String,Integer> count = new HashMap<String,Integer>();

        int total = 0;
        for (String line : lines) {
            if (line.startsWith("+ ") || line.startsWith("- ")) {
	            String l = line.substring(1).toLowerCase();
	            l = l.trim();
	            if (l.startsWith("-")) continue; 
	            if (l.startsWith("</")) continue; 
	            if (l.startsWith("<!")) continue; 
	            if (!l.startsWith("<")) continue; 
	            
	            int idx = l.indexOf(" ");
	            String tagLinha = (idx == -1) ? l : l.substring(0, idx);
	            
	            for (String t : tags) {
	                if (tagLinha.contains("<" + t.toLowerCase()) ) {
	                	if(count.get(tagLinha) == null) {
	                		count.put(tagLinha, 1);
	                	} else {
	                		count.put(tagLinha, count.get(tagLinha) + 1);
	                	}
	                	total++;
	                }
	            }
            }
        }
        sb.append("\n    XML ").append(count);
        sb.append("\n    TOTAL: ").append(total);
        return total;
    }
}
