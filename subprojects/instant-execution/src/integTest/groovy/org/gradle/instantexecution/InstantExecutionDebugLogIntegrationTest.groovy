/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.instantexecution

import org.gradle.api.DefaultTask
import org.gradle.execution.plan.LocalTaskNode

import static org.gradle.instantexecution.fingerprint.InstantExecutionCacheFingerprint.GradleEnvironment

class InstantExecutionDebugLogIntegrationTest extends AbstractInstantExecutionIntegrationTest {

    def "logs categorized open/close frame events for state and fingerprint files"() {
        given:
        settingsFile << """
            include 'sub'
        """
        buildFile << """
            allprojects {
                task ok { doLast { println('ok!') } }
            }
        """

        when:
        withDebugLogging()
        instantRun 'ok'

        then: "fingerprint frame events are logged"
        def events = collectOutputEvents()
        events.contains([category: "fingerprint", type: "O", "frame": GradleEnvironment.name])
        events.contains([category: "fingerprint", type: "C", "frame": GradleEnvironment.name])

        and: "state frame events are logged"
        events.contains([category: "state", type: "O", frame: ":ok"])
        events.contains([category: "state", type: "C", frame: ":ok"])
        events.contains([category: "state", type: "O", frame: ":sub:ok"])
        events.contains([category: "state", type: "C", frame: ":sub:ok"])

        and: "task type frame follows task path frame follows LocalTaskNode frame"
        def firstTaskNodeIndex = events.findIndexOf { it.frame == LocalTaskNode.name }
        firstTaskNodeIndex > 0
        events[firstTaskNodeIndex] == [category: "state", type: "O", frame: LocalTaskNode.name]
        events[firstTaskNodeIndex + 1] == [category: "state", type: "O", frame: ":ok"]
        events[firstTaskNodeIndex + 2] == [category: "state", type: "O", frame: DefaultTask.name]
    }

    private Collection<Map<String, Object>> collectOutputEvents() {
        def pattern = /[0-9:T.\-]+ \[DEBUG\] \[org.gradle.instantexecution.DefaultInstantExecution\] \[configuration cache (state|fingerprint)\] \{"type":"(O|C)","frame":"(.*?)","at":\d+\}/
        (output =~ pattern)
            .findAll()
            .collect { matchResult ->
                //noinspection GroovyUnusedAssignment
                def (ignored, category, type, frame) = matchResult
                [category: category, type: type, frame: frame]
            }
    }
}
