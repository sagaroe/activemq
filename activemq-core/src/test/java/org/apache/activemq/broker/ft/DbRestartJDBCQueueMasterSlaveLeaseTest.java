/**
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
package org.apache.activemq.broker.ft;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.store.jdbc.JDBCPersistenceAdapter;
import org.apache.activemq.store.jdbc.LeaseDatabaseLocker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbRestartJDBCQueueMasterSlaveLeaseTest extends DbRestartJDBCQueueMasterSlaveTest {
    private static final transient Logger LOG = LoggerFactory.getLogger(DbRestartJDBCQueueMasterSlaveLeaseTest.class);

    @Override
    protected void configureJdbcPersistenceAdapter(JDBCPersistenceAdapter persistenceAdapter) throws IOException {
        super.configureJdbcPersistenceAdapter(persistenceAdapter);
        persistenceAdapter.setLockAcquireSleepInterval(getLockAcquireSleepInterval());
        persistenceAdapter.setLockKeepAlivePeriod(getLockKeepAlivePeriod());
        persistenceAdapter.setDatabaseLocker(new LeaseDatabaseLocker());
    }

    private long getLockKeepAlivePeriod() {
        return 500;
    }

    private long getLockAcquireSleepInterval() {
        return 2000;
    }

    @Override
    protected void delayTillRestartRequired() {

        LOG.info("restart db after lease has expired. While Db is offline, master should stay alive, them lease up for grabs");
        try {
            TimeUnit.MILLISECONDS.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void verifyExpectedBroker(int inflightMessageCount) {
        if (inflightMessageCount == 0) {
            assertEquals("connected to master", master.getBrokerName(), ((ActiveMQConnection)sendConnection).getBrokerName());
        }
        // the lock is up for grabs after the expiry
    }

}
