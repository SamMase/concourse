/*
 * Copyright (c) 2013-2015 Cinchapi Inc.
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
package org.cinchapi.concourse;

import org.cinchapi.concourse.lang.Criteria;
import org.cinchapi.concourse.test.ConcourseIntegrationTest;
import org.cinchapi.concourse.thrift.Operator;
import org.cinchapi.concourse.util.TestData;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Strings;

/**
 * ETE tests for transaction related workflows.
 * 
 * @author Jeff Nelson
 */
public class TransactionWorkflowTest extends ConcourseIntegrationTest {

    /**
     * A second client that is connected to the server on behalf a different,
     * non-admin user.
     */
    private Concourse client2;

    @Override
    protected void beforeEachTest() {
        String username = null;
        while (Strings.isNullOrEmpty(username)) {
            username = TestData.getSimpleString();
        }
        String password = TestData.getString();
        while (Strings.isNullOrEmpty(password) || password.length() < 3) {
            password = TestData.getString();
        }
        grantAccess(username, password);
        client2 = Concourse.connect(SERVER_HOST, SERVER_PORT, username,
                password);
    }

    @Test
    public void testIsolation() {
        String key = TestData.getSimpleString();
        Object value = TestData.getObject();
        long record = TestData.getLong();
        client.stage();
        client2.stage();
        client.add(key, value, record);
        Assert.assertTrue(client.verify(key, value, record));
        Assert.assertFalse(client2.verify(key, value, record));
    }

    @Test
    public void testDurabilityAfterServerRestart() {
        String key = TestData.getSimpleString();
        Object value = TestData.getObject();
        long record = TestData.getLong();
        client.stage();
        client.add(key, value, record);
        client.commit();
        restartServer();
        Assert.assertTrue(client.verify(key, value, record));
    }

    @Test
    public void testConcurrentTransactionsForSameUserAreCorrectlyRouted() {
        client2 = Concourse.connect(SERVER_HOST, SERVER_PORT, "admin", "admin");
        client.stage();
        client2.stage();
        client.add("foo", "bar", 1);
        client2.add("foo", "bar", 2);
        Assert.assertTrue(client.verify("foo", "bar", 1));
        Assert.assertFalse(client.verify("foo", "bar", 2));
        Assert.assertTrue(client2.verify("foo", "bar", 2));
        Assert.assertFalse(client2.verify("foo", "bar", 1));
    }

    @Test
    public void testConcurrentTransactionsForSameUserCanBeCommitted() {
        client2 = Concourse.connect(SERVER_HOST, SERVER_PORT, "admin", "admin");
        client.stage();
        client2.stage();
        client.add("foo", "bar", 1);
        client2.add("foo", "bar", 2);
        Assert.assertTrue(client.commit());
        Assert.assertTrue(client2.commit());
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionGet() {
        try {
            client.stage();
            client.get("foo", 1);
            client2.set("foo", "baz", 1);
            client.get("foo", 1);
        }
        finally {
            client.abort();
        }
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionRevert() {
        try {
            client.stage();
            client.get("foo", 1);
            Timestamp ts = Timestamp.now();
            client.set("foo", "bar", 1);
            client2.set("foo", "baz", 1);
            client.revert("foo", 1, ts);
        }
        finally {
            client.abort();
        }
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionAdd() {
        try {
            client.stage();
            client.get("foo", 1);
            client2.set("foo", "baz", 1);
            client.add("foo", "grow", 1);
        }
        finally {
            client.abort();
        }
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionAudit() {
        try {
            client.stage();
            client.get("foo", 1);
            client2.set("foo", "baz", 1);
            client.audit("foo", 1);
        }
        finally {
            client.abort();
        }
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionAuditRecord() {
        try {
            client.stage();
            client.get("foo", 1);
            client2.set("foo", "baz", 1);
            client.audit(1);
        }
        finally {
            client.abort();
        }
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionBrowseRecord() {
        try {
            client.stage();
            client.get("foo", 1);
            client2.set("foo", "baz", 1);
            client.select(1);
        }
        finally {
            client.abort();
        }
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionBrowseKey() {
        try {
            client.stage();
            client.get("foo", 1);
            client2.set("foo", "baz", 1);
            client.browse("foo");
        }
        finally {
            client.abort();
        }
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionChronologize() {
        try {
            client.stage();
            client.get("foo", 1);
            client2.set("foo", "baz", 1);
            client.chronologize("foo", 1);
        }
        finally {
            client.abort();
        }
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionClear() {
        try {
            client.stage();
            client.get("foo", 1);
            client2.set("foo", "baz", 1);
            client.clear("foo", 1);
        }
        finally {
            client.abort();
        }
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionClearRecord() {
        try {
            client.stage();
            client.get("foo", 1);
            client2.set("foo", "baz", 1);
            client.clear(1);
        }
        finally {
            client.abort();
        }
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionCommit() {
        try {
            client.stage();
            client.get("foo", 1);
            client2.set("foo", "baz", 1);
            client.commit();
        }
        finally {
            client.abort();
        }
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionDescribeRecord() {
        try {
            client.stage();
            client.get("foo", 1);
            client2.set("foo", "baz", 1);
            client.describe(1);
        }
        finally {
            client.abort();
        }
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionDescribeKeyFetch() {
        try {
            client.stage();
            client.get("foo", 1);
            client2.set("foo", "baz", 1);
            client.select("foo", 1);
        }
        finally {
            client.abort();
        }
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionFind() {
        try {
            client.stage();
            client.get("foo", 1);
            client2.set("foo", "baz", 1);
            client.find("foo", Operator.EQUALS, "bar");
        }
        finally {
            client.abort();
        }
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionFindCriteria() {
        try {
            client.stage();
            client.get("foo", 1);
            client2.set("foo", "baz", 1);
            client.find(Criteria.where().key("foo").operator(Operator.EQUALS)
                    .value("bar"));
        }
        finally {
            client.abort();
        }
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionInsert() {
        try {
            client.stage();
            client.get("foo", 1);
            client2.set("foo", "baz", 1);
            client.insert("{\"foo\": \"bar\"}", 1);
        }
        finally {
            client.abort();
        }
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionInsertNewRecord() {
        try {
            client.stage();
            client.get("foo", 1);
            client2.set("foo", "baz", 1);
            client.insert("{\"foo\": \"bar\"}");
        }
        finally {
            client.abort();
        }
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionPing() {
        try {
            client.stage();
            client.get("foo", 1);
            client2.set("foo", "baz", 1);
            client.ping(1);
        }
        finally {
            client.abort();
        }
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionRemove() {
        try {
            client.stage();
            client.get("foo", 1);
            client2.set("foo", "baz", 1);
            client.remove("foo", "baz", 1);
        }
        finally {
            client.abort();
        }
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionSearch() {
        try {
            client.stage();
            client.get("foo", 1);
            client2.set("foo", "baz", 1);
            client.search("foo", "bar");
        }
        finally {
            client.abort();
        }
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionSet() {
        try {
            client.stage();
            client.get("foo", 1);
            client2.set("foo", "baz", 1);
            client.set("foo", "bar", 1);
        }
        finally {
            client.abort();
        }
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionVerify() {
        try {
            client.stage();
            client.get("foo", 1);
            client2.set("foo", "baz", 1);
            client.verify("foo", "bar", 1);
        }
        finally {
            client.abort();
        }
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionVerifyAndSwap() {
        try {
            client.stage();
            client.get("foo", 1);
            client2.set("foo", "baz", 1);
            client.verifyAndSwap("foo", "bar", 1, "baz");
        }
        finally {
            client.abort();
        }
    }

    @Test(expected = TransactionException.class)
    public void testPreCommitTransactionFailuresAreIndicatedWithExceptionVerifyOrSet() {
        try {
            client.stage();
            client.get("foo", 1);
            client2.set("foo", "baz", 1);
            client.verifyOrSet("foo", "bar", 1);
        }
        finally {
            client.abort();
        }
    }
    
    @Test
    public void testCommitWhenNotInTransactionReturnsFalse(){
        Assert.assertFalse(client.commit());
    }

}
