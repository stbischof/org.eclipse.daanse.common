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

import java.sql.SQLException;

import org.eclipse.daanse.common.emf.store.ds.helper.ClassOneHelper;
import org.eclipse.daanse.common.emf.store.ds.helper.TestAnnotations;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.sessions.server.Server;
import org.gecko.emf.osgi.annotation.require.RequireEMF;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import aQute.bnd.annotation.spi.ServiceProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@ServiceProvider(EPackage.class)
@ServiceProvider(EntityManagerFactory.class)
@RequireEMF
class SimpleMappingTest {

    @TestAnnotations.RegisterDynamicModelPackageWithTestClassName
    @TestAnnotations.RegisterH2DataSourceWithMarkerTestClassAnsMethodName
    @TestAnnotations.SetupEntityManagerFactoryWithModelDbAndMapping
    @Test
    void testModelBasicWithoutMapper(@InjectService(timeout = 1000) EntityManagerFactory entityManagerFactory)
            throws SQLException, InterruptedException {

        Server server = JpaHelper.getServerSession(entityManagerFactory);
        ClassDescriptor descriptor = server.getDescriptorForAlias("ClassOne");

        Object id = writeSimpleObject(entityManagerFactory, descriptor);
        readSimpleObject(entityManagerFactory, descriptor, id);
    }

    @TestAnnotations.RegisterDynamicModelPackageWithTestClassName
    @TestAnnotations.RegisterH2DataSourceWithMarkerTestClassAnsMethodName
    @TestAnnotations.SetupEntityManagerFactoryWithModelDbAndMapping
    @Test
    void testModelBasicFieldMapper(@InjectService(timeout = 1000) EntityManagerFactory entityManagerFactory)
            throws SQLException, InterruptedException {

        Server server = JpaHelper.getServerSession(entityManagerFactory);
        ClassDescriptor descriptor = server.getDescriptorForAlias("ClassOne");

        Object id = writeSimpleObject(entityManagerFactory, descriptor);
        readSimpleObject(entityManagerFactory, descriptor, id);
    }

    private void readSimpleObject(EntityManagerFactory entityManagerFactory,
            org.eclipse.persistence.descriptors.ClassDescriptor descriptor, Object id) {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();

        EObject eObject = em.find(descriptor.getJavaClass(), id);

        ClassOneHelper.assertEObject(eObject);

        em.flush();
        em.getTransaction().commit();
        em.clear();
        em.close();
    }



    private Object writeSimpleObject(EntityManagerFactory entityManagerFactory,
            org.eclipse.persistence.descriptors.ClassDescriptor descriptor) {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();

        EObject eObject = (EObject) descriptor.getInstantiationPolicy().buildNewInstance();

        EStructuralFeature attributeOne = eObject.eClass().getEStructuralFeature("attributeOne");
        EStructuralFeature attributeTwo = eObject.eClass().getEStructuralFeature("attributeTwo");
        EStructuralFeature attributeThree = eObject.eClass().getEStructuralFeature("attributeThree");

        EStructuralFeature attribiteId = eObject.eClass().getEStructuralFeature("id");

        eObject.eSet(attributeOne, "foo");
        eObject.eSet(attributeTwo, "bar");
        eObject.eSet(attributeThree, "buzz");

        em.persist(eObject);

        Object id = eObject.eGet(attribiteId);

        em.flush();
        em.getTransaction().commit();
        em.clear();
        em.getEntityManagerFactory().getCache().evictAll();
        em.close();
        return id;
    }
}
