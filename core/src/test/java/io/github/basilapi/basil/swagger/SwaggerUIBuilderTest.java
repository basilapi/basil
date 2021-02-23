/*
 * Copyright (c) 2021. Enrico Daga and Luca Panziera
 *
 * MLicensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.basilapi.basil.swagger;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class SwaggerUIBuilderTest {
   @Test
    public void test() throws URISyntaxException {
        String response = SwaggerUIBuilder.build(new URI("http://127.0.0.1:8080/basil/5134r243t/api-docs"));
        Assert.assertTrue(response.contains("\"/basil/5134r243t/api-docs\""));
//        System.err.println(response);
    }

}
