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

package org.apache.zookeeper.server.quorum.flexible;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.zookeeper.server.quorum.QuorumPeer.LearnerType;
import org.apache.zookeeper.server.quorum.QuorumPeer.QuorumServer;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig.ConfigException;

/**
 * This class implements a validator for majority quorums. The implementation is
 * straightforward.
 */
public class QuorumMaj implements QuorumVerifier {
    //所有的成员
    private Map<Long, QuorumServer> allMembers = new HashMap<>();
    //投票的成员
    private Map<Long, QuorumServer> votingMembers = new HashMap<>();
    //观察的成员
    private Map<Long, QuorumServer> observingMembers = new HashMap<>();
    //版本
    private long version = 0;
    //过半机制
    private int half;

    public int hashCode() {
        assert false : "hashCode not designed";
        return 42; // any arbitrary constant will do
    }

    public boolean equals(Object o) {
        if (!(o instanceof QuorumMaj)) {
            return false;
        }
        QuorumMaj qm = (QuorumMaj) o;
        if (qm.getVersion() == version) {
            return true;
        }
        if (allMembers.size() != qm.getAllMembers().size()) {
            return false;
        }
        for (QuorumServer qs : allMembers.values()) {
            QuorumServer qso = qm.getAllMembers().get(qs.id);
            if (qso == null || !qs.equals(qso)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Defines a majority to avoid computing it every time.
     * 定义一个多数派,以避免每次都要计算
     * 这个allMembers肯定是从配置文件里获取到的
     */
    public QuorumMaj(Map<Long, QuorumServer> allMembers) {
        this.allMembers = allMembers;
        for (QuorumServer qs : allMembers.values()) {
            //两种身份,参与者还是观察者
            if (qs.type == LearnerType.PARTICIPANT) {
                votingMembers.put(Long.valueOf(qs.id), qs);
            } else {
                observingMembers.put(Long.valueOf(qs.id), qs);
            }
        }
        //投票者的一半
        half = votingMembers.size() / 2;
    }

    public QuorumMaj(Properties props) throws ConfigException {
        for (Entry<Object, Object> entry : props.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();

            if (key.startsWith("server.")) {
                int dot = key.indexOf('.');
                long sid = Long.parseLong(key.substring(dot + 1));
                QuorumServer qs = new QuorumServer(sid, value);
                allMembers.put(Long.valueOf(sid), qs);
                if (qs.type == LearnerType.PARTICIPANT) {
                    votingMembers.put(Long.valueOf(sid), qs);
                } else {
                    observingMembers.put(Long.valueOf(sid), qs);
                }
            } else if (key.equals("version")) {
                version = Long.parseLong(value, 16);
            }
        }
        half = votingMembers.size() / 2;
    }

    /**
     * Returns weight of 1 by default.
     *
     * @param id
     */
    public long getWeight(long id) {
        return 1;
    }

    public String toString() {
        StringBuilder sw = new StringBuilder();

        for (QuorumServer member : getAllMembers().values()) {
            String key = "server." + member.id;
            String value = member.toString();
            sw.append(key);
            sw.append('=');
            sw.append(value);
            sw.append('\n');
        }
        String hexVersion = Long.toHexString(version);
        sw.append("version=");
        sw.append(hexVersion);
        return sw.toString();
    }

    /**
     * Verifies if a set is a majority. Assumes that ackSet contains acks only
     * from votingMembers
     */
    public boolean containsQuorum(Set<Long> ackSet) {
        return (ackSet.size() > half);
    }

    public Map<Long, QuorumServer> getAllMembers() {
        return allMembers;
    }

    public Map<Long, QuorumServer> getVotingMembers() {
        return votingMembers;
    }

    public Map<Long, QuorumServer> getObservingMembers() {
        return observingMembers;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long ver) {
        version = ver;
    }

}
