/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.api.publish.ivy.internal.publication;

import org.gradle.api.internal.AbstractNamedDomainObjectContainer;
import org.gradle.api.internal.CollectionCallbackActionDecorator;
import org.gradle.api.internal.MutationGuards;
import org.gradle.api.publish.ivy.IvyConfiguration;
import org.gradle.api.publish.ivy.IvyConfigurationContainer;
import org.gradle.internal.reflect.Instantiator;

public class DefaultIvyConfigurationContainer extends AbstractNamedDomainObjectContainer<IvyConfiguration> implements IvyConfigurationContainer {

    public DefaultIvyConfigurationContainer(Instantiator instantiator, CollectionCallbackActionDecorator collectionCallbackActionDecorator) {
        super(IvyConfiguration.class, instantiator, collectionCallbackActionDecorator, MutationGuards.identity());
    }

    @Override
    protected IvyConfiguration doCreate(String name) {
        return getInstantiator().newInstance(DefaultIvyConfiguration.class, name);
    }
}
