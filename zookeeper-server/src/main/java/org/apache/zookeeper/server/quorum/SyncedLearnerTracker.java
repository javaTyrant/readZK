/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.zookeeper.server.quorum;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.zookeeper.server.quorum.flexible.QuorumVerifier;

//这种实体类其实相当于数据库的表
public class SyncedLearnerTracker {

    protected final ArrayList<QuorumVerifierAcksetPair> qvAcksetPairs = new ArrayList<>();

    //QuorumVerifierAcksetPair的集合
    //QuorumVerifierAcksetPair,qv对应的ack信息
    //看看QuorumVerifier的赋值情况
    //QuorumVerifier的赋值情况
    //QuorumPeer self;self.getQuorumVerifier()
    //然后查看self是如何赋值的.self是通过FastLeaderElection(QuorumPeer self, QuorumCnxManager manager)
    //FastLeaderElection fle = new FastLeaderElection(this, qcm);
    //QuorumPeer中调用FLT的构造器,那么QuorumPeer显然是在createElectionAlgorithm这个方法里生成的
    //构造器里进行赋值QuorumPeer()
    // if (quorumConfig == null) {
    //            quorumConfig = new QuorumMaj(quorumPeers);
    //        }
    //        setQuorumVerifier(quorumConfig, false);
    //最好从入口再重新观察下各个重要参数的赋值情况
    public void addQuorumVerifier(QuorumVerifier qv) {
        qvAcksetPairs.add(new QuorumVerifierAcksetPair(qv, new HashSet<>(qv.getVotingMembers().size())));
    }

    //实际上这个返回值是没有被用到的,只是单纯的修改了qvAckset
    public boolean addAck(Long sid) {
        boolean change = false;
        for (QuorumVerifierAcksetPair qvAckset : qvAcksetPairs) {
            //如果投票的sid包含目标sid
            if (qvAckset.getQuorumVerifier().getVotingMembers().containsKey(sid)) {
                //
                qvAckset.getAckset().add(sid);
                change = true;
            }
        }
        return change;
    }

    public boolean hasSid(long sid) {
        for (QuorumVerifierAcksetPair qvAckset : qvAcksetPairs) {
            if (!qvAckset.getQuorumVerifier().getVotingMembers().containsKey(sid)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasAllQuorums() {
        for (QuorumVerifierAcksetPair qvAckset : qvAcksetPairs) {
            //
            if (!qvAckset.getQuorumVerifier().containsQuorum(qvAckset.getAckset())) {
                return false;
            }
        }
        return true;
    }

    public String ackSetsToString() {
        StringBuilder sb = new StringBuilder();

        for (QuorumVerifierAcksetPair qvAckset : qvAcksetPairs) {
            sb.append(qvAckset.getAckset().toString()).append(",");
        }

        return sb.substring(0, sb.length() - 1);
    }

    public static class QuorumVerifierAcksetPair {

        //QuorumVerifier有哪些信息呢,看看这个实现类QuorumMaj
        private final QuorumVerifier qv;
        //ack集合,看下从哪里赋值的:qv.getVotingMembers().size()
        private final HashSet<Long> ackset;

        public QuorumVerifierAcksetPair(QuorumVerifier qv, HashSet<Long> ackset) {
            this.qv = qv;
            this.ackset = ackset;
        }

        public QuorumVerifier getQuorumVerifier() {
            return this.qv;
        }

        public HashSet<Long> getAckset() {
            return this.ackset;
        }
    }

}
