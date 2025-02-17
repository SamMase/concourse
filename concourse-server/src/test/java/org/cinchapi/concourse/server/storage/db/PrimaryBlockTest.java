/*
 * Copyright (c) 2013-2015 Cinchapi Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cinchapi.concourse.server.storage.db;

import org.cinchapi.concourse.server.model.PrimaryKey;
import org.cinchapi.concourse.server.model.Text;
import org.cinchapi.concourse.server.model.Value;
import org.cinchapi.concourse.server.storage.Action;
import org.cinchapi.concourse.server.storage.db.Block;
import org.cinchapi.concourse.server.storage.db.PrimaryBlock;
import org.cinchapi.concourse.time.Time;
import org.cinchapi.concourse.util.Convert;
import org.cinchapi.concourse.util.TestData;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * 
 * @author Jeff Nelson
 */
public class PrimaryBlockTest extends BlockTest<PrimaryKey, Text, Value> {
    
    @Test
    public void testInsertLocatorWithTrailingWhiteSpace() {
        PrimaryKey locator = PrimaryKey.wrap(1);
        Text key = Text.wrap("Youtube Embed Link ");
        Value value = Value.wrap(Convert.javaToThrift("http://youtube.com"));
        block.insert(locator, key, value, Time.now(), Action.ADD);
        Record<PrimaryKey, Text, Value> record = Record.createPrimaryRecordPartial(locator, key);
        block.sync();
        block.seek(locator, key, record);
        Assert.assertTrue(record.get(key).contains(value));
    }

    @Override
    protected PrimaryKey getLocator() {
        return TestData.getPrimaryKey();
    }

    @Override
    protected Text getKey() {
        return TestData.getText();
    }

    @Override
    protected Value getValue() {
        return TestData.getValue();
    }

    @Override
    protected PrimaryBlock getMutableBlock(String directory) {
        return Block.createPrimaryBlock(Long.toString(Time.now()), directory);
    }

}
