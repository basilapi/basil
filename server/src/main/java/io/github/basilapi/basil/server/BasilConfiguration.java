/*
 * Copyright (c) 2022. Enrico Daga and Luca Panziera
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

package io.github.basilapi.basil.server;

public class BasilConfiguration {
    private String namespace;
    private String tdb2location;
    public String getNamespace(){
        return namespace;
    }
    public String getTdb2Location(){
        return tdb2location;
    }

    public void setNamespace(String namespace){
        this.namespace = namespace;
    }
    public void setTdb2Location(String tdb2location){
        this.tdb2location = tdb2location;
    }

}
