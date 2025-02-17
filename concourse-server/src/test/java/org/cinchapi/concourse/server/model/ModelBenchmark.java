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
package org.cinchapi.concourse.server.model;

import org.cinchapi.concourse.util.TestData;
import org.junit.Test;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;

/**
 * 
 * 
 * @author Jeff Nelson
 */
public class ModelBenchmark extends AbstractBenchmark {

    @Test
    @BenchmarkOptions(benchmarkRounds = 1000, warmupRounds = 0)
    public void benchmarkValue() {
        Value.wrap(TestData.getTObject());
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 1000, warmupRounds = 0)
    public void benchmarkPrimaryKey() {
        PrimaryKey.wrap(TestData.getLong());
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 1000, warmupRounds = 0)
    public void benchmarkText() {
        Text.wrap(TestData.getString());
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 1000, warmupRounds = 0)
    public void benchmarkPosition() {
        Position.wrap(TestData.getPrimaryKey(), Math.abs(TestData.getInt()));
    }

}
