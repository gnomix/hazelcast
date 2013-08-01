/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.collection.set.tx;

import com.hazelcast.collection.CollectionProxyId;
import com.hazelcast.collection.CollectionService;
import com.hazelcast.collection.multimap.tx.TransactionalMultiMapProxySupport;
import com.hazelcast.collection.set.ObjectSetProxy;
import com.hazelcast.config.MultiMapConfig;
import com.hazelcast.core.Id;
import com.hazelcast.core.TransactionalSet;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.transaction.impl.TransactionSupport;

/**
 * @author ali 4/16/13
 */
public class TransactionalSetProxy<E> extends TransactionalMultiMapProxySupport<Id> implements TransactionalSet<E> {

    final Data key;

    public TransactionalSetProxy(NodeEngine nodeEngine, CollectionService service, CollectionProxyId proxyId, TransactionSupport tx) {
        super(nodeEngine, service, proxyId, tx, createConfig(proxyId));
        this.key = nodeEngine.toData(getId());
    }

    private static MultiMapConfig createConfig(CollectionProxyId proxyId) {
        return new MultiMapConfig().setName(ObjectSetProxy.COLLECTION_SET_NAME + proxyId.getKeyName())
                .setValueCollectionType(MultiMapConfig.ValueCollectionType.SET);
    }

    public boolean add(E e) {
        checkTransactionState();
        Data value = getNodeEngine().toData(e);
        return putInternal(key, value);
    }

    public boolean remove(E e) {
        checkTransactionState();
        Data value = getNodeEngine().toData(e);
        return removeInternal(key, value);
    }

    public int size() {
        checkTransactionState();
        return valueCountInternal(key);
    }

    public Id getId() {
        return new Id(proxyId.getKeyName(), proxyId.getPartitionKey());
    }
}
