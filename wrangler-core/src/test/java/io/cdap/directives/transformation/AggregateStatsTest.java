/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.directives.transformation;

import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.Row;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AggregateStatsTest {

    @Test
    public void testAggregateStats() throws Exception {
        // Create test data
        List<Row> rows = new ArrayList<>();

        Row row1 = new Row();
        row1.add("data_transfer_size", "10KB");
        row1.add("response_time", "100ms");
        rows.add(row1);

        Row row2 = new Row();
        row2.add("data_transfer_size", "5MB");
        row2.add("response_time", "2.5s");
        rows.add(row2);

        Row row3 = new Row();
        row3.add("data_transfer_size", "1GB");
        row3.add("response_time", "10s");
        rows.add(row3);

        // Define the recipe
        String[] recipe = new String[] {
                "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec"
        };

        // Execute the recipe
        List<Row> results = TestingRig.execute(recipe, rows);

        // Verify results
        Assert.assertEquals(1, results.size());

        // Expected values:
        // 10KB + 5MB + 1GB = 10 * 1024 + 5 * 1024 * 1024 + 1 * 1024 * 1024 * 1024 bytes
        // Convert to MB: (10 * 1024 + 5 * 1024 * 1024 + 1 * 1024 * 1024 * 1024) / (1024
        // * 1024) MB
        double expectedTotalSizeInMB = (10 * 1024 + 5 * 1024 * 1024 + 1 * 1024 * 1024 * 1024) / (1024.0 * 1024.0);

        // 100ms + 2.5s + 10s = 100 * 1_000_000 + 2.5 * 1_000_000_000 + 10 *
        // 1_000_000_000 nanoseconds
        // Convert to seconds: (100 * 1_000_000 + 2.5 * 1_000_000_000 + 10 *
        // 1_000_000_000) / 1_000_000_000 seconds
        double expectedTotalTimeInSeconds = (100 * 1_000_000 + 2.5 * 1_000_000_000 + 10 * 1_000_000_000)
                / 1_000_000_000.0;

        Assert.assertEquals(expectedTotalSizeInMB, (Double) results.get(0).getValue("total_size_mb"), 0.001);
        Assert.assertEquals(expectedTotalTimeInSeconds, (Double) results.get(0).getValue("total_time_sec"), 0.001);
    }
}
