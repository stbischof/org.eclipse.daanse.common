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

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Map;

import org.eclipse.daanse.common.emf.store.ds.helper.ClassOneHelper;
import org.eclipse.daanse.common.emf.store.ds.helper.TestAnnotations;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.gecko.emf.osgi.annotation.require.RequireEMF;
import org.gecko.emf.osgi.constants.EMFNamespaces;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import aQute.bnd.annotation.spi.ServiceProvider;
import jakarta.persistence.EntityManagerFactory;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@ServiceProvider(EPackage.class)
@ServiceProvider(EntityManagerFactory.class)
@RequireEMF
class EmfLoadTestClassOne {

    @TestAnnotations.RegisterH2DataSourceWithMarkerTestClassAnsMethodName
    @TestAnnotations.RegisterDynamicModelPackageWithTestClassName
    @TestAnnotations.SetupEntityManagerFactoryWithModelDbAndMapping
    @TestAnnotations.SaveCsvInDb
    @TestAnnotations.SetupJpaResouceFactory
    @Test
    void testOtherNameOfAttribute(@InjectService(timeout = 2000) EntityManagerFactory entityManagerFactory,
            @InjectService(timeout = 2000, filter = "(" + EMFNamespaces.EMF_CONFIGURATOR_NAME
                    + "=JPARF)") ServiceAware<ResourceSet> rsAware)
            throws SQLException, InterruptedException, IOException {

        ResourceSet resourceSet = rsAware.getService();
        Resource resource = resourceSet.createResource(URI.createURI("jpa://myPersistenceUnit/ClassOne/1"));
        resource.load(InputStream.nullInputStream(), Map.of());

        EObject eObject = resource.getContents().get(0);

        ClassOneHelper.assertEObject(eObject);

    }

}
