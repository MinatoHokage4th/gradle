/*
 * Copyright 2024 the original author or authors.
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

package org.gradle.internal.component.resolution.failure.type;

import org.gradle.api.Describable;
import org.gradle.api.internal.attributes.AttributeContainerInternal;
import org.gradle.api.internal.catalog.problems.ResolutionFailureProblemId;
import org.gradle.internal.component.resolution.failure.ResolutionCandidateAssessor;

import java.util.List;

/**
 * A {@link ResolutionFailure} that specializes {@link IncompatibleResolutionFailure} to represent a
 * failure to select a variant for a component when building a dependency resolution graph.
 */
public final class IncompatibleGraphVariantFailure extends IncompatibleResolutionFailure {
    public IncompatibleGraphVariantFailure(Describable requested, AttributeContainerInternal requestedAttributes, List<ResolutionCandidateAssessor.AssessedCandidate> candidates) {
        super(ResolutionFailureProblemId.INCOMPATIBLE_GRAPH_VARIANT, requested, requestedAttributes, candidates);
    }

    public boolean noCandidatesHaveAttributes() {
        return getCandidates().stream().allMatch(ResolutionCandidateAssessor.AssessedCandidate::hasNoAttributes);
    }
}
