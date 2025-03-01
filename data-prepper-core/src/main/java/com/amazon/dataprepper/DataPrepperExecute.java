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

package com.amazon.dataprepper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execute entry into Data Prepper.
 */
public class DataPrepperExecute {
    private static final Logger LOG = LoggerFactory.getLogger(DataPrepperExecute.class);

    public static void main(String[] args) {
        java.security.Security.setProperty("networkaddress.cache.ttl", "60");

        if(args.length > 1) {
            DataPrepper.configure(args[1]);
        }
        final DataPrepper dataPrepper = DataPrepper.getInstance();
        if (args.length > 0) {
            dataPrepper.execute(args[0]);
        } else {
            LOG.error("Configuration file is required");
            System.exit(1);
        }
    }
}