/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kodebeagle.gitreposummary

import java.io.File

import com.kodebeagle.configuration.KodeBeagleConfig
import com.kodebeagle.model.{GithubRepo, MockedGithubRepo}
import org.apache.commons.io.FileUtils
import org.apache.hadoop.conf.Configuration
import org.scalatest.{BeforeAndAfterAll, FunSuite}


class GitRepoSummarySuite extends FunSuite with BeforeAndAfterAll with GitHubRepoMockSupport {

  var repo: Option[GithubRepo] = None

  override def beforeAll {
    repo = mockRepo
  }

  test("Repository Summary") {
    val files = repo.get.files
    val timeStampCheck = "1466071223"

    val gitRepoSummaryObj: GitRepoSummary = GitRepoStatistics.getRepoSummary(repo.get.repository,
      "It scraps all the topics present in any google group link", "himukr",
      "https://github.com/himukr/google-grp-scraper.git")

    val details: List[CommitDetails] = gitRepoSummaryObj.commitDetails
    assert((details.head.timeStamp) == timeStampCheck)
  }
}

trait GitHubRepoMockSupport {
  def mockRepo: Option[GithubRepo] = {
    import sys.process._
    FileUtils.copyFileToDirectory(
      new File(Thread.currentThread.
        getContextClassLoader.getResource("GitRepoTest-git.tar.gz").getPath),
      new File(s"${KodeBeagleConfig.repoCloneDir}/himukr/google-grp-scraper"))

    s"""tar -xvf ${KodeBeagleConfig.repoCloneDir}/himukr/google-grp-scraper/GitRepoTest-git.tar.gz
        |-C ${KodeBeagleConfig.repoCloneDir}/himukr/google-grp-scraper""".stripMargin.!!

    Option(new MockedGithubRepo().init(new Configuration, "himukr/google-grp-scraper"))
  }

}
