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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.sessions.server.Server;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class JPAEngine {

    private JPAResourceImpl resource;

    private URI uri;

    private EntityManagerFactory entityManagerFactory;

    public JPAEngine(URI uri, EntityManagerFactory entityManagerFactory) {
        this.uri = uri;
        this.entityManagerFactory = entityManagerFactory;
    }

    public void setResource(JPAResourceImpl resource) {
        this.resource = resource;

    }

    private void doSave(EObject object) {
        // save jpa here
    }

    public void load(Map<?, ?> loadOptions) {
        int segmentCount = uri.segmentCount();
        String idString = uri.lastSegment();
        String classAlias = uri.segment(segmentCount - 2);

        Server server = JpaHelper.getServerSession(entityManagerFactory);
        ClassDescriptor descriptor = server.getDescriptorForAlias(classAlias);

        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();

        DatabaseField databaseFieldPK = descriptor.getPrimaryKeyFields().get(0);
        Class<?> pkType = databaseFieldPK.getType();
        Object id = idString;

        if (pkType == Integer.class) {
            id = Integer.valueOf(idString);
        } else if (pkType == Long.class) {
            id = Long.valueOf(idString);
        } else if (pkType == UUID.class) {
            id = UUID.fromString(idString);

        }

        EObject eObject = em.find(descriptor.getJavaClass(), id);

        em.flush();
        em.getTransaction().commit();
        em.clear();
        em.close();

        resource.getContents().add(eObject);
    }

    public void save(Map<?, ?> saveptions) {
        List<EObject> content = resource.getContents();
        content.forEach(this::doSave);
    }

    public URI getUri() {
        return uri;
    }
}
