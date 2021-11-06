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

package org.apache.zookeeper.server.watch;

import java.io.PrintWriter;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;

/**
 * 观察管理器
 * 主要看下两个实现类,有一个是优化了的,
 * 为什么需要优化,如何优化的
 */
public interface IWatchManager {

    /**
     * Add watch to specific path.
     * 为路径添加一个观察者
     *
     * @param path    znode path
     * @param watcher watcher object reference
     * @return true if the watcher added is not already present
     */
    boolean addWatch(String path, Watcher watcher);

    /**
     * 检查某个路径是否有观察者
     * Checks the specified watcher exists for the given path.
     *
     * @param path    znode path
     * @param watcher watcher object reference
     * @return true if the watcher exists, false otherwise
     */
    boolean containsWatcher(String path, Watcher watcher);

    /**
     * Removes the specified watcher for the given path.
     * 删除观察者
     *
     * @param path    znode path
     * @param watcher watcher object reference
     * @return true if the watcher successfully removed, false otherwise
     */
    boolean removeWatcher(String path, Watcher watcher);

    /**
     * The entry to remove the watcher when the cnxn is closed.
     * cnxn被关闭的时候,要移除观察者
     *
     * @param watcher watcher object reference
     */
    void removeWatcher(Watcher watcher);

    /**
     * Distribute the watch event for the given path.
     *
     * @param path znode path
     * @param type the watch event type
     * @return the watchers have been notified
     */
    WatcherOrBitSet triggerWatch(String path, EventType type);

    /**
     * Distribute the watch event for the given path, but ignore those
     * suppressed ones.
     *
     * @param path     znode path
     * @param type     the watch event type
     * @param suppress the suppressed watcher set
     * @return the watchers have been notified
     */
    WatcherOrBitSet triggerWatch(String path, EventType type, WatcherOrBitSet suppress);

    /**
     * Get the size of watchers.
     *
     * @return the watchers number managed in this class.
     */
    int size();

    /**
     * Clean up the watch manager.
     */
    void shutdown();

    /**
     * Returns a watch summary.
     *
     * @return watch summary
     * @see WatchesSummary
     */
    WatchesSummary getWatchesSummary();

    /**
     * Returns a watch report.
     *
     * @return watch report
     * @see WatchesReport
     */
    WatchesReport getWatches();

    /**
     * Returns a watch report by path.
     *
     * @return watch report
     * @see WatchesPathReport
     */
    WatchesPathReport getWatchesByPath();

    /**
     * String representation of watches. Warning, may be large!
     * 警告:可能会很大
     *
     * @param pwriter the writer to dump the watches
     * @param byPath  iff true output watches by paths, otw output
     *                watches by connection
     */
    void dumpWatches(PrintWriter pwriter, boolean byPath);

}
