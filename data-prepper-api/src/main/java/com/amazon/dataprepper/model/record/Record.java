/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  The OpenSearch Contributors require contributions made to
 *  this file be licensed under the Apache-2.0 license or a
 *  compatible open source license.
 *
 *  Modifications Copyright OpenSearch Contributors. See
 *  GitHub history for details.
 */

package com.amazon.dataprepper.model.record;

/**
 * Data Prepper record - represents the fundamental data unit of TI, the idea is to encapsulate different
 * types of data we will be supporting in TI.
 * <p>
 * TODO: The current implementation focuses on proving the bare bones for which this class only need to
 * TODO: support sample test cases.
 */
public class Record<T> {
    private final T data;
    private final RecordMetadata metadata;

    public Record(final T data) {
        this.data = data;
        metadata = RecordMetadata.defaultMetadata();
    }

    public Record(final T data, final RecordMetadata metadata) {
        this.data = data;
        this.metadata = metadata;
    }

    public T getData() {
        return data;
    }

    public RecordMetadata getMetadata() {
        return metadata;
    }
}
