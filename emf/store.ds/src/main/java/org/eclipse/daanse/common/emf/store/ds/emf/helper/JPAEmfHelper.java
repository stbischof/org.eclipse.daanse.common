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
package org.eclipse.daanse.common.emf.store.ds.emf.helper;

import java.util.List;

import org.eclipse.daanse.common.emf.store.ds.emf.EmfType;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.jpa.JpaHelper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 *
 * @author dclarke
 * @since EclipseLink 1.2
 */
public class JPAEmfHelper extends EmfHelper {

    public JPAEmfHelper(EntityManagerFactory emf) {
        super(JpaHelper.getServerSession(emf));
    }

    public JPAEmfHelper(EntityManager em) {
        super(JpaHelper.getEntityManager(em).getDatabaseSession());
    }

    /**
     * Add one or more EntityType instances to a session and optionally generate
     * needed tables with or without FK constraints.
     */
    @Override
    public void addTypes(boolean createMissingTables, boolean generateFKConstraints, List<EmfType> emfTypes) {

        super.addTypes(createMissingTables, generateFKConstraints, emfTypes);
        for (EmfType emfType : emfTypes) {
            ClassDescriptor descriptor = emfType.getDescriptor();
            descriptor.getQueryManager().checkDatabaseForDoesExist();
        }
    }

}
