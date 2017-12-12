# couchbase-lite-jpa

JPA provider for CouchbaseLite. Not a full JPA implemention yet, but most annotations working.

Preferable use in conjunction with [jpa-apt](https://github.com/dankito/jpa-apt) to that JPA meta model gets generated at build time and 
no time consuming annotation reading with reflection at application start up is required.


## Setup

Gradle:
```
dependencies {
  compile 'net.dankito.jpa.couchbaselite:couchbase-lite-jpa:1.0-alpha'
}
```

Maven:
```
<dependency>
   <groupId>net.dankito.jpa.couchbaselite</groupId>
   <artifactId>couchbase-lite-jpa</artifactId>
   <version>1.0-alpha</version>
</dependency>
```



# License

    Copyright 2017 dankito

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.