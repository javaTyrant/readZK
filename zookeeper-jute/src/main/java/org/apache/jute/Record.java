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

package org.apache.jute;

import java.io.IOException;

import org.apache.yetus.audience.InterfaceAudience;

/**
 * 序列化反序列化节点
 * Interface that is implemented by generated classes.
 */
@InterfaceAudience.Public
public interface Record {
    /**
     *
     * @param archive out
     * @param tag tag?
     * @throws IOException
     */
    void serialize(OutputArchive archive, String tag) throws IOException;

    /**
     *
     * @param archive in
     * @param tag 看看用到的地方,这个tag标签是做什么用的
     * @throws IOException
     */
    void deserialize(InputArchive archive, String tag) throws IOException;
}
