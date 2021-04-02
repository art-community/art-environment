import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

/*
 * ART
 *
 * Copyright 2020 ART
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

open class ProjectConfiguration @Inject constructor(objectFactory: ObjectFactory, val name: String) {
    var url: String? = null
        private set
    var version: String? = null
        private set

    fun from(url: String, version: String) {
        this.url = url
        this.version = version
    }

    fun url(url: String) {
        this.url = url
    }

    fun version(version: String) {
        this.version = version
    }
}
