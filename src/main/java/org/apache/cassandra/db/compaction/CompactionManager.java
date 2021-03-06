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
package org.apache.cassandra.db.compaction;

import java.lang.management.ManagementFactory;
import java.util.*;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.TabularData;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.cassandra.metrics.CompactionMetrics;

import com.cloudius.urchin.api.APIClient;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * A singleton which manages a private executor of ongoing compactions.
 * <p/>
 * Scheduling for compaction is accomplished by swapping sstables to be
 * compacted into a set via DataTracker. New scheduling attempts will ignore
 * currently compacting sstables.
 */

/*
 * Copyright 2015 Cloudius Systems
 * 
 * Modified by Cloudius Systems
 */
public class CompactionManager implements CompactionManagerMBean {
    public static final String MBEAN_OBJECT_NAME = "org.apache.cassandra.db:type=CompactionManager";
    private static final java.util.logging.Logger logger = java.util.logging.Logger
            .getLogger(CompactionManager.class.getName());
    public static final CompactionManager instance;
    private APIClient c = new APIClient();
    CompactionMetrics metrics = new CompactionMetrics();

    public void log(String str) {
        logger.info(str);
    }

    static {
        instance = new CompactionManager();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            mbs.registerMBean(instance, new ObjectName(MBEAN_OBJECT_NAME));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static CompactionManager getInstance() {
        return instance;
    }

    /** List of running compaction objects. */
    public List<Map<String, String>> getCompactions() {
        log(" getCompactions()");
        return c.getListMapStrValue("compaction_manager/compactions");
    }

    /** List of running compaction summary strings. */
    public List<String> getCompactionSummary() {
        log(" getCompactionSummary()");
        return c.getListStrValue("compaction_manager/compaction_summary");
    }

    /** compaction history **/
    public TabularData getCompactionHistory() {
        log(" getCompactionHistory()");
        return c.getCQLResult("SELECT * from system.compaction_history");
    }

    /**
     * @see org.apache.cassandra.metrics.CompactionMetrics#pendingTasks
     * @return estimated number of compactions remaining to perform
     */
    @Deprecated
    public int getPendingTasks() {
        log(" getPendingTasks()");
        return c.getIntValue("");
    }

    /**
     * @see org.apache.cassandra.metrics.CompactionMetrics#completedTasks
     * @return number of completed compactions since server [re]start
     */
    @Deprecated
    public long getCompletedTasks() {
        log(" getCompletedTasks()");
        return c.getLongValue("");
    }

    /**
     * @see org.apache.cassandra.metrics.CompactionMetrics#bytesCompacted
     * @return total number of bytes compacted since server [re]start
     */
    @Deprecated
    public long getTotalBytesCompacted() {
        log(" getTotalBytesCompacted()");
        return c.getLongValue("");
    }

    /**
     * @see org.apache.cassandra.metrics.CompactionMetrics#totalCompactionsCompleted
     * @return total number of compactions since server [re]start
     */
    @Deprecated
    public long getTotalCompactionsCompleted() {
        log(" getTotalCompactionsCompleted()");
        return c.getLongValue("");
    }

    /**
     * Triggers the compaction of user specified sstables. You can specify files
     * from various keyspaces and columnfamilies. If you do so, user defined
     * compaction is performed several times to the groups of files in the same
     * keyspace/columnfamily.
     *
     * @param dataFiles
     *            a comma separated list of sstable file to compact. must
     *            contain keyspace and columnfamily name in path(for 2.1+) or
     *            file name itself.
     */
    public void forceUserDefinedCompaction(String dataFiles) {
        log(" forceUserDefinedCompaction(String dataFiles)");
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("dataFiles", dataFiles);
        c.post("compaction_manager/compaction_manager/force_user_defined_compaction",
                queryParams);
    }

    /**
     * Stop all running compaction-like tasks having the provided {@code type}.
     * 
     * @param type
     *            the type of compaction to stop. Can be one of: - COMPACTION -
     *            VALIDATION - CLEANUP - SCRUB - INDEX_BUILD
     */
    public void stopCompaction(String type) {
        log(" stopCompaction(String type)");
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("type", type);
        c.post("compaction_manager/compaction_manager/stop_compaction",
                queryParams);
    }

    /**
     * Returns core size of compaction thread pool
     */
    public int getCoreCompactorThreads() {
        log(" getCoreCompactorThreads()");
        return c.getIntValue("");
    }

    /**
     * Allows user to resize maximum size of the compaction thread pool.
     * 
     * @param number
     *            New maximum of compaction threads
     */
    public void setCoreCompactorThreads(int number) {
        log(" setCoreCompactorThreads(int number)");
    }

    /**
     * Returns maximum size of compaction thread pool
     */
    public int getMaximumCompactorThreads() {
        log(" getMaximumCompactorThreads()");
        return c.getIntValue("");
    }

    /**
     * Allows user to resize maximum size of the compaction thread pool.
     * 
     * @param number
     *            New maximum of compaction threads
     */
    public void setMaximumCompactorThreads(int number) {
        log(" setMaximumCompactorThreads(int number)");
    }

    /**
     * Returns core size of validation thread pool
     */
    public int getCoreValidationThreads() {
        log(" getCoreValidationThreads()");
        return c.getIntValue("");
    }

    /**
     * Allows user to resize maximum size of the compaction thread pool.
     * 
     * @param number
     *            New maximum of compaction threads
     */
    public void setCoreValidationThreads(int number) {
        log(" setCoreValidationThreads(int number)");
    }

    /**
     * Returns size of validator thread pool
     */
    public int getMaximumValidatorThreads() {
        log(" getMaximumValidatorThreads()");
        return c.getIntValue("");
    }

    /**
     * Allows user to resize maximum size of the validator thread pool.
     * 
     * @param number
     *            New maximum of validator threads
     */
    public void setMaximumValidatorThreads(int number) {
        log(" setMaximumValidatorThreads(int number)");
    }

}
