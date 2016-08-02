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

class GitRepoSummarySuite extends FunSuite with BeforeAndAfterAll with GitHubRepoMockSupport{

  var repo: Option[GithubRepo] = None

  override def beforeAll {
    repo = mockRepo
  }

  test("Repository Summary") {
    val files = repo.get.files
    val jsonStringTest  = "{ \"index\" : { \"_index\" : " +
      "\"gitreposummary\", \"_type\" : \"typegitreposummary\" }" +
      " }\n{\"description\":\"It scraps all the topics " +
      "present in any google group link\",\"owner\":" +
      "\"himukr\",\"lastChange\":\"Last Changed\"," +
      "\"stats\":\"4 commits 0 tags\",\"rpeositoryURl\":" +
      "\"https://github.com/himukr/google-grp-scraper.git\"," +
      "\"commitDetails\":[{\"authorName\":\"himanshu " +
      "khantwal\",\"commitMsg\":\"updated\"," +
      "\"timeStamp\":\"1466071223\",\"commitId\":" +
      "\"commit 2f728a4485432ec8e3f77e8aa45ba16d1bd373d8 " +
      "1466071223 ----sp\"},{\"authorName\":\"himanshu" +
      " khantwal\",\"commitMsg\":\"changed the scroll class " +
      "to the correct class\",\"timeStamp\":\"1453962955\"," +
      "\"commitId\":\"commit df8ec70e7ef79cbc2f74861b1ed7b8ac93d341ca " +
      "1453962955 ----sp\"},{\"authorName\":\"himukr\",\"commitMsg\":" +
      "\"added ReadMe to the project\\n\",\"timeStamp\":" +
      "\"1430141760\",\"commitId\":\"commit " +
      "b709aa1df44a97c2bef77312f3b2e940ef9f270f " +
      "1430141760 ----sp\"},{\"authorName\":\"himukr\"," +
      "\"commitMsg\":\"file name should be less than 50-- fix\\n\"," +
      "\"timeStamp\":\"1430141385\",\"commitId\":\"commit " +
      "c4b4cab53a8e91412ebddf072f2f13945495d508 1430141385 " +
      "----sp\"}],\"metricsForChart\":[{\"name\":\"2015-04\"," +
      "\"count\":3.0},{\"name\":\"2016-01\",\"count\":1.0},{\"name\":\"2016-06\",\"count\":1.0}]}"




    val resultJSONString = GitRepoStatistics.getRepoSummary(repo.get.repository,
      "It scraps all the topics present in any google group link","himukr",
      "https://github.com/himukr/google-grp-scraper.git")
    assert(jsonStringTest.sameElements(resultJSONString))
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
