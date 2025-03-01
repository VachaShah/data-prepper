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

package com.amazon.dataprepper.plugins.source;

import com.amazon.dataprepper.model.PluginType;
import com.amazon.dataprepper.model.annotations.DataPrepperPlugin;
import com.amazon.dataprepper.model.buffer.Buffer;
import com.amazon.dataprepper.model.configuration.PluginSetting;
import com.amazon.dataprepper.model.record.Record;
import com.amazon.dataprepper.model.source.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * A simple source which reads data from console each line at a time. It exits when it reads case insensitive "exit"
 * from console or if Pipeline notifies to stop.
 */
@DataPrepperPlugin(name = "stdin", type = PluginType.SOURCE)
public class StdInSource implements Source<Record<String>> {
    private static final Logger LOG = LoggerFactory.getLogger(StdInSource.class);
    private static final String ATTRIBUTE_TIMEOUT = "write_timeout";
    private static final int WRITE_TIMEOUT = 5_000;
    private final Scanner reader;
    private final int writeTimeout;
    private final String pipelineName;
    private boolean isStopRequested;

    /**
     * Mandatory constructor for Data Prepper Component - This constructor is used by Data Prepper
     * runtime engine to construct an instance of {@link StdInSource} using an instance of {@link PluginSetting} which
     * has access to pluginSetting metadata from pipeline
     * pluginSetting file.
     *
     * @param pluginSetting instance with metadata information from pipeline pluginSetting file.
     */
    public StdInSource(final PluginSetting pluginSetting) {
        this(checkNotNull(pluginSetting, "PluginSetting cannot be null")
                        .getIntegerOrDefault(ATTRIBUTE_TIMEOUT, WRITE_TIMEOUT),
                pluginSetting.getPipelineName());
    }

    public StdInSource(final int writeTimeout, final String pipelineName) {
        this.writeTimeout = writeTimeout;
        this.pipelineName = checkNotNull(pipelineName, "Pipeline name cannot be null");
        this.reader = new Scanner(System.in);
        isStopRequested = false;
    }

    @Override
    public void start(final Buffer<Record<String>> buffer) {
        checkNotNull(buffer, format("Pipeline [%s] - buffer cannot be null for source to start", pipelineName));
        String line = reader.nextLine();
        while (!"exit".equalsIgnoreCase(line) && !isStopRequested) {
            final Record<String> record = new Record<>(line);
            try {
                buffer.write(record, writeTimeout);
            } catch (TimeoutException ex) {
                LOG.error("Pipeline [{}] - Timed out writing to buffer; Will exit without further processing",
                        pipelineName, ex);
                throw new RuntimeException(format("Pipeline [%s] - Timed out writing to buffer", pipelineName), ex);
            }
            line = reader.nextLine();
        }
    }

    @Override
    public void stop() {
        isStopRequested = true;
    }
}