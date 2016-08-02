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
import java.util.TimeZone

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import com.gitblit.models.Metric
import com.gitblit.utils.MetricUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.{Ref, Repository}
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import com.kodebeagle.util.SparkIndexJobHelper._

import scala.util.control.Breaks

object GitRepoStatistics {

  def getRepoSummary(repository: Repository, description: String,
                     owner: String, repoURL: String): String = {
    val commitDetailsList = ListBuffer[CommitDetails]()
    val git = new Git(repository)
    var noOfCommits = 0
    try {
      val branches: List[org.eclipse.jgit.lib.Ref] = git.branchList().call().toList
      for (branch: org.eclipse.jgit.lib.Ref <- branches) {
        val branchName = branch.getName
        val commits: java.lang.Iterable[RevCommit] = git.log().add(repository.resolve(branchName))
          .call()
        val loop = new Breaks
        loop.breakable {
          for (commit: org.eclipse.jgit.revwalk.RevCommit <- commits) {
            if (noOfCommits==4){
              loop.break()
            }
            commitDetailsList +=
              CommitDetails(commit.getAuthorIdent.getName,commit.getFullMessage,
                commit.getCommitTime.toString,commit.toString)
            noOfCommits += 1
          }
        }


      }
    } catch {
      case e: Exception => e.printStackTrace()
    }
    val metrics= getDateMetrics(repository)
    var stats = noOfCommits + " commits " + repository.getTags.size + " tags"
    print(stats)

    print(toJson(GitRepoSummary(
      description, owner, "Last Changed", stats, repoURL,commitDetailsList.toList, metrics)))

    print(toIndexTypeJson("java","statistics",GitRepoSummary(
      description, owner, "Last Changed", stats, repoURL,commitDetailsList.toList, metrics),true))

    toJson(GitRepoSummary(
      description, owner, "Last Changed", stats, repoURL,commitDetailsList.toList, metrics))
  }

  def getDateMetrics(repository: Repository): List[MetricScala] =  {
    val metrics = MetricUtils.getDateMetrics(repository,
      "refs/heads/master", true, "", TimeZone.getDefault)
    metrics.remove(0)
    metrics.map(metric => MetricScala(metric.name, metric.count)).toList
  }
}

case class MetricScala(name: String, count: Double)

case class GitRepoSummary(description: String, owner: String, lastChange: String,
                          stats: String, rpeositoryURl: String,
                          commitDetails: List[CommitDetails], metricsForChart: List[MetricScala])
case class CommitDetails(authorName: String, commitMsg: String, timeStamp: String, commitId: String)

