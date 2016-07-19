package com.kodebeagle.filestatistics;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import com.gitblit.models.PathModel.PathChangeModel;
import com.gitblit.utils.JGitUtils;

public class GitRepoFileStatistics {

	/**
	 * This method walks over all the commits and returns us Top N related files
	 * checked in along with it
	 *
	 * @param repository
	 * @param filePath
	 * @param nFiles
	 * @return
	 * @throws GitAPIException
	 * @throws IOException
	 */
	public static Map<String, Integer> getNRelatedFiles(Repository repository, String filePath, int nFiles)
			throws GitAPIException, IOException {

		Map<String, Integer> topNFiles = walkAllCommits(repository, filePath, nFiles);

		return topNFiles;
	}

	/**
	 * This method uses RevWalk to quickly iterate over all available commits
	 *
	 * @param repo
	 * @param fileName
	 * @param nFiles
	 * @return
	 * @throws GitAPIException
	 * @throws IOException
	 */
	public static Map<String, Integer> walkAllCommits(Repository repo, String fileName, int nFiles)
			throws GitAPIException, IOException {

		Git git = new Git(repo);
		RevWalk walk = new RevWalk(repo);
		List<Ref> branches = git.branchList().call();
		Map<String, Integer> fileRankings = new HashMap<String, Integer>();

		for (Ref branch : branches) {
			String branchName = branch.getName();

			Iterable<RevCommit> commits = git.log().all().call();

			for (RevCommit commit : commits) {
				boolean foundInThisBranch = false;

				RevCommit targetCommit = walk.parseCommit(repo.resolve(commit.getName()));
				for (Map.Entry<String, Ref> e : repo.getAllRefs().entrySet()) {
					if (e.getKey().startsWith("refs/heads/")) {
						if (walk.isMergedInto(targetCommit, walk.parseCommit(e.getValue().getObjectId()))) {
							String foundInBranch = e.getValue().getName();
							if (branchName.equals(foundInBranch)) {
								foundInThisBranch = true;
								break;
							}
						}
					}
				}
				if (foundInThisBranch) {
					List<PathChangeModel> filesList = JGitUtils.getFilesInCommit(repo, commit, true);
					for (int i = 0; i < filesList.size(); i++) {
						if (filesList.get(i).name.equals(fileName)) {
							rankFilesCommitLevel(filesList, fileRankings, fileName);
						} else {
							// ignore as of now placeholder
						}
					}
				}
			}
		}
		git.close();
		fileRankings = SortRankings.sortRankingsMap(fileRankings, nFiles);
		return fileRankings;

	}

	/**
	 * Ranks all files based on the availability in previous commits
	 * 
	 * @param filesList
	 * @param fileRankings
	 */
	public static void rankFilesCommitLevel(List<PathChangeModel> filesList, Map<String, Integer> fileRankings,
			String fileName) {
		for (int i = 0; i < filesList.size(); i++) {
			String key = filesList.get(i).name;
			int count = fileRankings.containsKey(key) ? fileRankings.get(key) : 0;
			fileRankings.put(key, count + 1);
		}
		fileRankings.remove(fileName);
	}

}
