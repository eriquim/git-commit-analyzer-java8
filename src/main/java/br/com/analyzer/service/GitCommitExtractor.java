
package br.com.analyzer.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import br.com.analyzer.domain.ArquivoCommit;
import br.com.analyzer.domain.Commit;

public class GitCommitExtractor {

    public static List<Commit> extrair(
            String repoPath,
            String branch,
            String autorFiltro,
            LocalDateTime inicio,
            LocalDateTime fim,
            List<String> xmlTags
    ) throws Exception {

        List<Commit> lista = new ArrayList<Commit>();
        long commitSeq = 1;
        long arqSeq = 1;

        Repository repo = new FileRepositoryBuilder()
                .setGitDir(new File(repoPath + "/.git"))
                .build();

        Git git = new Git(repo);
        RevWalk revWalk = new RevWalk(repo);
        ObjectReader reader = repo.newObjectReader();

        try {

            ObjectId branchId = repo.resolve(branch);
            Iterable<RevCommit> commits = git.log().add(branchId).call();

            Instant start = inicio.atZone(ZoneId.systemDefault()).toInstant();
            Instant end = fim.atZone(ZoneId.systemDefault()).toInstant();

            for (RevCommit rc : commits) {

                String autor = rc.getAuthorIdent().getEmailAddress();
                Instant dataCommit = rc.getAuthorIdent().getWhen().toInstant();

                if (autorFiltro != null && !autor.contains(autorFiltro)) continue;
                if (dataCommit.isBefore(start) || dataCommit.isAfter(end)) continue;
                if (rc.getParentCount() == 0) continue;

                Commit commit = new Commit(
                        commitSeq++,
                        rc.getName(),
                        autor,
                        rc.getAuthorIdent().getWhen(),
                        rc.getShortMessage(),
                        0
                );

                RevCommit parent = revWalk.parseCommit(rc.getParent(0));

                CanonicalTreeParser oldTree = new CanonicalTreeParser();
                oldTree.reset(reader, parent.getTree());

                CanonicalTreeParser newTree = new CanonicalTreeParser();
                newTree.reset(reader, rc.getTree());

                List<DiffEntry> diffs = git.diff()
                        .setOldTree(oldTree)
                        .setNewTree(newTree)
                        .call();

                for (DiffEntry d : diffs) {

                    String path = d.getNewPath();
                    String ext = getExt(path);

                    if (!"sql".equals(ext) && !"xml".equals(ext)) continue;

                    ByteArrayOutputStream out = new ByteArrayOutputStream();

                    DiffFormatter formatter = new DiffFormatter(out);
                    formatter.setRepository(repo);
                    formatter.format(d);

                    String diffText = out.toString("ISO-8859-1");
                    formatter.close();
                    
                    String resultado = DiffProcessor.processar(diffText, ext, xmlTags, commit);

                    ArquivoCommit arq = new ArquivoCommit(
                            arqSeq++,
                            commit.getId(),
                            path,
                            ext,
                            resultado
                    );

                    commit.addArquivo(arq);
                }

                lista.add(commit);
            }

        } finally {
            reader.close();
            revWalk.close();
            git.close();
            repo.close();
        }

        return lista;
    }

    private static String getExt(String nome) {
        int i = nome.lastIndexOf('.');
        if (i < 0) return "";
        return nome.substring(i + 1).toLowerCase();
    }
}
