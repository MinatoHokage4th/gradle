/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.api.internal;

import groovy.lang.Closure;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.Namer;
import org.gradle.api.internal.collections.CollectionFilter;
import org.gradle.api.specs.Spec;
import org.gradle.api.specs.Specs;
import org.gradle.internal.Cast;
import org.gradle.internal.reflect.Instantiator;

public class FactoryNamedDomainObjectContainer<T> extends AbstractNamedDomainObjectContainer<T> {

    private final NamedDomainObjectFactory<T> factory;

    /**
     * <p>Creates a container that instantiates using the given factory.<p>
     *
     * @param type The concrete type of element in the container
     * @param instantiator The instantiator to use to create any other collections based on this one
     * @param namer The naming strategy to use
     * @param factory The factory responsible for creating new instances on demand
     */
    public FactoryNamedDomainObjectContainer(Class<T> type, Instantiator instantiator, Namer<? super T> namer, NamedDomainObjectFactory<T> factory, MutationGuard parentMutationGuard, CollectionCallbackActionDecorator collectionCallbackActionDecorator) {
        super(type, instantiator, namer, collectionCallbackActionDecorator, parentMutationGuard);
        this.factory = factory;
    }

    /**
     * <p>Creates a container that instantiates using the given factory.<p>
     *
     * @param type The concrete type of element in the container
     * @param instantiator The instantiator to use to create any other collections based on this one
     * @param namer The naming strategy to use
     * @param factoryClosure The factory responsible for creating new instances on demand
     */
    public FactoryNamedDomainObjectContainer(Class<T> type, Instantiator instantiator, Namer<? super T> namer, final Closure<?> factoryClosure, MutationGuard parentMutationGuard, CollectionCallbackActionDecorator collectionCallbackActionDecorator) {
        this(type, instantiator, namer, new ClosureObjectFactory<>(type, factoryClosure), parentMutationGuard, collectionCallbackActionDecorator);
    }

    @Override
    protected <S extends T> DefaultNamedDomainObjectSet<S> filtered(CollectionFilter<S> filter) {
        return filtered(Specs.satisfyAll(), filter);
    }

    @Override
    protected <S extends T> DefaultNamedDomainObjectSet<S> filtered(Spec<String> nameFilter, CollectionFilter<S> elementFilter) {
        return Cast.uncheckedNonnullCast(getInstantiator().newInstance(DefaultNamedDomainObjectSet.class, this, nameFilter, elementFilter, getInstantiator(), getNamer(), getParentMutationGuard()));
    }

    @Override
    protected T doCreate(String name) {
        return factory.create(name);
    }

    private static class ClosureObjectFactory<T> implements NamedDomainObjectFactory<T> {
        private final Class<T> type;
        private final Closure<?> factoryClosure;

        public ClosureObjectFactory(Class<T> type, Closure<?> factoryClosure) {
            this.type = type;
            this.factoryClosure = factoryClosure;
        }

        @Override
        public T create(String name) {
            return type.cast(factoryClosure.call(name));
        }
    }
}
