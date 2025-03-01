/* Copyright 2021 predic8 GmbH, www.predic8.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */
package com.predic8.membrane.examples.tests;

import com.predic8.membrane.examples.DistributionExtractingTestcase;
import com.predic8.membrane.examples.Process2;
import com.predic8.membrane.examples.util.BufferLogger;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.predic8.membrane.test.AssertUtils.postAndAssert;

public class Xml2JsonTest extends DistributionExtractingTestcase {

    @Test
    public void test() throws IOException, InterruptedException {
        File baseDir = getExampleDir("xml-2-json");
        BufferLogger b = new BufferLogger();
        Process2 sl = new Process2.Builder().in(baseDir).script("service-proxy").waitForMembrane().withWatcher(b).start();
        try {
            String body = new String(Files.readAllBytes(Paths.get(baseDir + FileSystems.getDefault().getSeparator()
                    + "jobs.xml")));
            String[] headers = {"Content-Type", "application/xml"};
            Thread.sleep(2000);
            String response = postAndAssert(200,"http://localhost:2000/", headers, body);
            Thread.sleep(1000);

            Assert.assertTrue(b.toString().contains("{\"jobs\":{\"job\":"));

        } finally {
            sl.killScript();
        }
    }


}