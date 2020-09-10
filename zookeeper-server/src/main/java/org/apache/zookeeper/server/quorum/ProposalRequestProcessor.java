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

import org.apache.zookeeper.server.Request;
import org.apache.zookeeper.server.RequestProcessor;
import org.apache.zookeeper.server.SyncRequestProcessor;
import org.apache.zookeeper.server.quorum.Leader.XidRolloverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This RequestProcessor simply forwards requests to an AckRequestProcessor and
 * SyncRequestProcessor.
 * 这个处理器简单把请求转发到AckRequestProcessor和SyncRequestProcessor
 */
public class ProposalRequestProcessor implements RequestProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ProposalRequestProcessor.class);

    LeaderZooKeeperServer zks;
    //下一个处理器,LeaderZooKeeperServer中设置了CommitProcessor
    RequestProcessor nextProcessor;
    //同步处理器
    SyncRequestProcessor syncProcessor;

    public ProposalRequestProcessor(LeaderZooKeeperServer zks, RequestProcessor nextProcessor) {
        this.zks = zks;
        this.nextProcessor = nextProcessor;
        AckRequestProcessor ackProcessor = new AckRequestProcessor(zks.getLeader());
        //SyncRequestProcessor需要AckRequestProcessor,也就是同步之后要ack
        syncProcessor = new SyncRequestProcessor(zks, ackProcessor);
    }

    /**
     * initialize this processor
     */
    public void initialize() {
        //同步处理器开始工作
        syncProcessor.start();
    }

    public void processRequest(Request request) throws RequestProcessorException {
        // LOG.warn("Ack>>> cxid = " + request.cxid + " type = " +
        // request.type + " id = " + request.sessionId);
        // request.addRQRec(">prop");


        /* In the following IF-THEN-ELSE block, we process syncs on the leader.
         * If the sync is coming from a follower, then the follower
         * handler adds it to syncHandler. Otherwise, if it is a client of
         * the leader that issued the sync command, then syncHandler won't
         * contain the handler. In this case, we add it to syncHandler, and
         * call processRequest on the next processor.
         */
        //如果是LearnerSyncRequest,getLeader处理同步
        if (request instanceof LearnerSyncRequest) {
            zks.getLeader().processSync((LearnerSyncRequest) request);
        } else {
            //CommitProcessor处理这个同步
            nextProcessor.processRequest(request);
            if (request.getHdr() != null) {
                // We need to sync and get consensus on any transactions
                // 我们需要同步,并且对任何的事务获取一致性
                try {
                    //要把这个过程搞懂,
                    zks.getLeader().propose(request);
                } catch (XidRolloverException e) {
                    throw new RequestProcessorException(e.getMessage(), e);
                }
                //交给sync处理器
                syncProcessor.processRequest(request);
            }
        }
    }

    public void shutdown() {
        LOG.info("Shutting down");
        nextProcessor.shutdown();
        syncProcessor.shutdown();
    }

}
