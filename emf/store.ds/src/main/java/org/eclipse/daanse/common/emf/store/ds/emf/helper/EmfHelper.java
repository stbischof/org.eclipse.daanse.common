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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.daanse.common.emf.store.ds.emf.EmfType;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicClassLoader;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.dynamic.DynamicTypeBuilder;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.internal.dynamic.DynamicEntityImpl;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.eclipse.persistence.queries.ReadObjectQuery;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.tools.schemaframework.DynamicSchemaManager;

/**
 * A DynamicHelper provides some utility methods to simplify application
 * development with dynamic types. Since the application does not have static
 * references to the dynamic types it must use entity names. This helper
 * provides simplified access to methods that would typically require the static
 * classes.
 *
 * @author dclarke, mnorman
 * @since EclipseLink 1.2
 */
public class EmfHelper {

    protected DatabaseSession session;
    protected Map<String, ClassDescriptor> fqClassnameToDescriptor = new HashMap<>();

    public EmfHelper(DatabaseSession session) {
        this.session = session;
        Collection<ClassDescriptor> descriptors = session.getDescriptors().values();
        for (ClassDescriptor desc : descriptors) {
            if (desc.getJavaClassName() != null) {
                fqClassnameToDescriptor.put(desc.getJavaClassName(), desc);
            }
        }
    }

    public DatabaseSession getSession() {
        return this.session;
    }

    /**
     * Lookup the dynamic type for an alias. This is required to get the type for
     * factory creation but can also be used to provide the application with access
     * to the meta model (type and properties) allowing for dynamic use as well as
     * optimized data value retrieval from an entity.
     */
    public DynamicType getType(String typeName) {
        ClassDescriptor cd = fqClassnameToDescriptor.get(typeName);
        if (cd == null) {
            cd = getSession().getClassDescriptorForAlias(typeName);
        }

        if (cd == null) {
            return null;
        }
        return getType(cd);
    }

    public static DynamicType getType(ClassDescriptor descriptor) {
        return (DynamicType) descriptor.getProperty(DynamicType.DESCRIPTOR_PROPERTY);
    }

    /**
     * Provide access to the entity's type.
     *
     * @throws ClassCastException if entity is not an instance of
     *                            {@link DynamicEntityImpl}
     */
    public static DynamicType getType(DynamicEntity entity) throws ClassCastException {
        return ((DynamicEntityImpl) entity).getType();
    }

    /**
     * Remove a dynamic type from the system.
     *
     * This implementation assumes that the dynamic type has no relationships to it
     * and that it is not involved in an inheritance relationship. If there are
     * concurrent processes using this type when it is removed some exceptions may
     * occur.
     *
     */
    public void removeType(String typeName) {
        DynamicType type = getType(typeName);

        if (type != null) {
            getSession().getIdentityMapAccessor().initializeIdentityMap(type.getJavaClass());

            ClassDescriptor descriptor = type.getDescriptor();
            fqClassnameToDescriptor.remove(descriptor.getJavaClassName());
            getSession().getProject().getOrderedDescriptors().remove(descriptor);
            getSession().getProject().getDescriptors().remove(type.getJavaClass());
            // bug 430318 - clear the parsed cache as queries in that cache could be using
            // this descriptor
            getSession().getProject().getJPQLParseCache().clear();
            ((AbstractSession) getSession()).getCommitManager().getCommitOrder().remove(type.getJavaClass());
        }
    }

    /**
     *
     */
    public DynamicEntity newDynamicEntity(String typeName) {
        DynamicType type = getType(typeName);

        if (type == null) {
            throw new IllegalArgumentException("DynamicHelper.createQuery: Dynamic type not found: " + typeName);
        }

        return type.newDynamicEntity();
    }

    /**
     * Helper method to simplify creating a native ReadAllQuery using the entity
     * type name (descriptor alias)
     */
    public ReadAllQuery newReadAllQuery(String typeName) {
        DynamicType type = getType(typeName);

        if (type == null) {
            throw new IllegalArgumentException("DynamicHelper.createQuery: Dynamic type not found: " + typeName);
        }

        return new ReadAllQuery(type.getJavaClass());
    }

    /**
     * Helper method to simplify creating a native ReadObjectQuery using the entity
     * type name (descriptor alias)
     */
    public ReadObjectQuery newReadObjectQuery(String typeName) {
        DynamicType type = getType(typeName);

        if (type == null) {
            throw new IllegalArgumentException("DynamicHelper.createQuery: Dynamic type not found: " + typeName);
        }

        return new ReadObjectQuery(type.getJavaClass());
    }

    /**
     * Helper method to simplify creating a native ReportQuery using the entity type
     * name (descriptor alias)
     */
    public ReportQuery newReportQuery(String typeName, ExpressionBuilder builder) {
        DynamicType type = getType(typeName);

        if (type == null) {
            throw new IllegalArgumentException("DynamicHelper.createQuery: Dynamic type not found: " + typeName);
        }

        return new ReportQuery(type.getJavaClass(), builder);
    }

    public DynamicClassLoader getDynamicClassLoader() {
        return DynamicClassLoader.lookup(getSession());
    }

    /**
     * Add a {@link List} EntityType instances to a session and optionally generate
     * needed tables with or without FK constraints.
     *
     */
    public void addTypes(boolean createMissingTables, boolean generateFKConstraints, List<EmfType> emfTypes) {
        if (emfTypes == null || emfTypes.isEmpty()) {
            throw new IllegalArgumentException("No types provided");
        }
        Collection<ClassDescriptor> descriptors = new ArrayList<>(emfTypes.size());
        for (EmfType emfType : emfTypes) {
            ClassDescriptor classDescriptor = emfType.getDescriptor();
            descriptors.add(classDescriptor);
            if (!classDescriptor.requiresInitialization((AbstractSession) session)) {
                classDescriptor.getInstantiationPolicy().initialize((AbstractSession) session);
            }
        }
        session.addDescriptors(descriptors);
        for (ClassDescriptor desc : descriptors) {
            if (desc.getJavaClassName() != null) {
                fqClassnameToDescriptor.put(desc.getJavaClassName(), desc);
            }
        }

        if (createMissingTables) {
            if (!getSession().isConnected()) {
                getSession().login();
            }
            new DynamicSchemaManager(session).createTables(generateFKConstraints);
        }
    }

    /**
     * A SessionCustomizer which configures all descriptors as dynamic entity types.
     */
    public static class SessionCustomizer implements org.eclipse.persistence.config.SessionCustomizer {

        /**
         * Default constructor.
         */
        public SessionCustomizer() {
            // default
        }

        @Override
        public void customize(Session session) throws Exception {
            DynamicClassLoader dcl = DynamicClassLoader.lookup(session);

            for (Iterator<?> i = session.getProject().getDescriptors().values().iterator(); i.hasNext();) {
                new DynamicTypeBuilder(dcl, (ClassDescriptor) i.next(), null);
            }
        }
    }

}
