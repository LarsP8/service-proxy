/* Copyright 2012 predic8 GmbH, www.predic8.com

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

import static com.predic8.membrane.test.AssertUtils.assertContains;
import static com.predic8.membrane.test.AssertUtils.postAndAssert200;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.predic8.membrane.examples.DistributionExtractingTestcase;
import com.predic8.membrane.examples.Process2;

public class CBRTest extends DistributionExtractingTestcase {
	@Test
	public void test() throws IOException, InterruptedException {
		File baseDir = getExampleDir("cbr");
		Process2 sl = new Process2.Builder().in(baseDir).script("service-proxy").waitForMembrane().start();
		try {
			Thread.sleep(2000);
			String result = postAndAssert200("http://localhost:2000/shop", FileUtils.readFileToString(new File(baseDir, "order.xml")));
			assertContains("Normal order received.", result);

			result = postAndAssert200("http://localhost:2000/shop", FileUtils.readFileToString(new File(baseDir, "express.xml")));
			assertContains("Express order received.", result);

			result = postAndAssert200("http://localhost:2000/shop", FileUtils.readFileToString(new File(baseDir, "import.xml")));
			assertContains("Order contains import items.", result);
		} finally {
			sl.killScript();
		}
	}


}
