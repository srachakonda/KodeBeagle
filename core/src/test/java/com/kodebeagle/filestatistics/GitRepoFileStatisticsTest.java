package com.kodebeagle.filestatistics;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Map;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.Test;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

public class GitRepoFileStatisticsTest {

	String filePath = "README.md";

	@Test
	public void testFilesInCommit() throws Exception {

        File archive = new File("resources/git.tar.gz");
        File destination = new File("/tmp");

        Archiver archiver = ArchiverFactory.createArchiver("tar", "gz");
        archiver.extract(archive, destination);


        FileRepositoryBuilder builder = new FileRepositoryBuilder();
		Repository repository = builder.setGitDir(new File("/tmp/git/")).readEnvironment().findGitDir().build();
		Map<String, Integer> results = GitRepoFileStatistics.getNRelatedFiles(repository, filePath, 2);
		assertEquals(2, results.size());
	}
}
