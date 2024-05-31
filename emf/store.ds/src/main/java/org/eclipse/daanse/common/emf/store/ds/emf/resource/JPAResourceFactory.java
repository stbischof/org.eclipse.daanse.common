/*
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   SmartCity Jena - initial
*   Stefan Bischof (bipolis.org) - initial
*   Data In Motion - initial API and implementation
*/
package org.eclipse.daanse.common.emf.store.ds.emf.resource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.daanse.common.emf.store.ds.api.JpaEmFConstants;
import org.eclipse.daanse.common.emf.store.ds.emf.resource.JPAResourceFactory.Config;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Factory;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.gecko.emf.osgi.constants.EMFNamespaces;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import jakarta.persistence.EntityManagerFactory;

@Designate(factory = true, ocd = Config.class)
@Component(configurationPid = JpaEmFConstants.PID_JPA_RESOURCE_FACTORY, service = Factory.class, property = {
        EMFNamespaces.EMF_CONFIGURATOR_NAME + "=JPARF", EMFNamespaces.EMF_MODEL_PROTOCOL + "=jpa",
        EMFNamespaces.EMF_MODEL_VERSION + "=3.0", EMFNamespaces.EMF_MODEL_FEATURE + "=EMFJPA" })
public class JPAResourceFactory extends ResourceFactoryImpl {

    public JPAResourceFactory() {
        System.out.println(111111);
    }

    @ObjectClassDefinition
    @interface Config {

    }

    @Reference(name = JpaEmFConstants.JPA_RESOURCE_FACTORY_PROPERTY_EMF)
    EntityManagerFactory entityManagerFactory;

    /**
     * Cache for Entity manager factories to the persistence unit names
     */
    private Map<String, JPAEngine> engineMap = new ConcurrentHashMap<>();

    @Override
    public Resource createResource(URI uri) {
        String puName = uri.authority();
        JPAEngine engine = engineMap.computeIfAbsent(puName, p -> createEngine(uri, entityManagerFactory));
        return new JPAResourceImpl(engine);
    }

    private JPAEngine createEngine(URI uri, EntityManagerFactory entityManagerFactory) {
        JPAEngine engine = new JPAEngine(uri, entityManagerFactory);
        return engine;
    }

}
