/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.processors.cache;

import org.apache.ignite.*;
import org.apache.ignite.internal.util.typedef.internal.*;

import javax.cache.processor.*;

/**
 * Implementation of {@link MutableEntry} passed to the {@link EntryProcessor#process(MutableEntry, Object...)}.
 */
public class CacheInvokeEntry<K, V> extends CacheLazyEntry<K, V> implements MutableEntry<K, V> {
    /** */
    private final boolean hadVal;

    /** */
    private Operation op = Operation.NONE;

    /**
     * @param cctx   Cache context.
     * @param keyObj Key cache object.
     * @param valObj Cache object value.
     */
    public CacheInvokeEntry(GridCacheContext cctx, KeyCacheObject keyObj, CacheObject valObj) {
        super(cctx, keyObj, valObj);

        this.hadVal = getValue() != null;
    }

    /** {@inheritDoc} */
    @Override public boolean exists() {
        return getValue() != null;
    }

    /** {@inheritDoc} */
    @Override public void remove() {
        val = null;
        valObj = null;

        if (op == Operation.CREATE)
            op = Operation.NONE;
        else
            op = Operation.REMOVE;
    }

    /** {@inheritDoc} */
    @Override public void setValue(V val) {
        if (val == null)
            throw new NullPointerException();

        this.val = val;

        op = hadVal ? Operation.UPDATE : Operation.CREATE;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override public <T> T unwrap(Class<T> cls) {
        if (cls.isAssignableFrom(Ignite.class))
            return (T)cctx.kernalContext().grid();
        else if (cls.isAssignableFrom(getClass()))
            return cls.cast(this);

        throw new IllegalArgumentException("Unwrapping to class is not supported: " + cls);
    }

    /**
     * @return {@code True} if {@link #setValue} or {@link #remove was called}.
     */
    public boolean modified() {
        return op != Operation.NONE;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(CacheInvokeEntry.class, this);
    }

    /**
     *
     */
    private static enum Operation {
        /** */
        NONE,

        /** */
        CREATE,

        /** */
        UPDATE,

        /** */
        REMOVE
    }
}
