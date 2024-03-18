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
*/
package org.eclipse.daanse.common.emf.store.ds;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.common.emf.model.emfdbmapping.Entity;
import org.eclipse.daanse.common.emf.model.emfdbmapping.EntityMappingsType;
import org.eclipse.daanse.common.emf.store.ds.emf.EmfType;
import org.eclipse.daanse.common.emf.store.ds.emf.JPAWrapperEObject;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.persistence.dynamic.DynamicClassLoader;

public class MappingDynamicInstanceGenerator {
    private EntityMappingsType entityMappingsType;
    private DynamicClassLoader dynamicClassLoader;

    MappingDynamicInstanceGenerator(DynamicClassLoader dynamicClassLoader, EntityMappingsType entityMappingsType) {
        this.dynamicClassLoader = dynamicClassLoader;
        this.entityMappingsType = entityMappingsType;
    }

    List<EmfDynamicEntity> generate(EPackage ePackage) {

        List<EmfDynamicEntity> list = new ArrayList<>();
        for (EClassifier eClassifier : ePackage.getEClassifiers()) {
            EmfDynamicEntity ede = generate(eClassifier);
            list.add(ede);
        }
        return list;
    }

    EmfDynamicEntity generate(EClassifier eClassifier) {
        Class<?> dynamicClass = eClassifier.getInstanceClass();
        if (dynamicClass == null) {
            String fqClassName = eClassifier.getEPackage().getName() + "." + eClassifier.getName();
            dynamicClass = dynamicClassLoader.createDynamicClass(fqClassName);
        }
        EmfDynamicEntity e = new EmfDynamicEntity(eClassifier, getMappingEntity(eClassifier), dynamicClass);
        return e;
    }

    List<EmfType> generateEmfType(EPackage ePackage) {

        List<EmfType> list = new ArrayList<EmfType>();
        for (EClassifier eClassifier : ePackage.getEClassifiers()) {
            EmfType ede = generateEmfType(eClassifier);
            list.add(ede);
        }
        return list;
    }

    EmfType generateEmfType(EClassifier eClassifier) {

        Class<?> javaClass = eClassifier.getInstanceClass();
        if (javaClass == null) {
            String fqClassName = eClassifier.getEPackage().getName() + "." + eClassifier.getName();
            javaClass = dynamicClassLoader.createDynamicClass(fqClassName, JPAWrapperEObject.class);
        }
        return new EmfType(eClassifier, javaClass, getMappingEntity(eClassifier));

    }

    private Optional<Entity> getMappingEntity(EClassifier eClassifier) {
        Optional<Entity> oEntity = entityMappingsType.getEntity().stream().filter(e -> {
            EClassifier ref = e.getClass_();

            System.out.println(ref);
            System.out.println(eClassifier);
            System.out.println(ref.getName());
            System.out.println(eClassifier.getName());
//			boolean is 	=	eClassifier.equals(ref);
//			boolean is =	EcoreUtil.equals(eClassifier, ref);
            boolean is = eClassifier.getName().equals(ref.getName());
            return is;
        }).findFirst();
        return oEntity;
    }
}
