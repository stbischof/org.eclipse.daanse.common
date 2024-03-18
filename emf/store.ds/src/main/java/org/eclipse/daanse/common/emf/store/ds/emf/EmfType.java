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
package org.eclipse.daanse.common.emf.store.ds.emf;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.daanse.common.emf.model.emfdbmapping.Basic;
import org.eclipse.daanse.common.emf.model.emfdbmapping.Column;
import org.eclipse.daanse.common.emf.model.emfdbmapping.Entity;
import org.eclipse.daanse.common.emf.model.emfdbmapping.SecondaryTable;
import org.eclipse.daanse.common.emf.model.emfdbmapping.Table;
import org.eclipse.daanse.common.emf.store.ds.Util;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.RelationalDescriptor;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.helper.DatabaseTable;
import org.eclipse.persistence.internal.jpa.CMP3Policy;
import org.eclipse.persistence.mappings.DirectToFieldMapping;

public class EmfType {

    private Class<?> javaClass;
    private Optional<Entity> oEntity;
    private EClassifier eClassifier;

    protected ClassDescriptor classDescriptor;

    public EmfType(EClassifier eClassifier, Class<?> javaClass, Optional<Entity> oEntity) {
        super();
        this.javaClass = javaClass;
        this.oEntity = oEntity;
        this.eClassifier = eClassifier;
        this.classDescriptor = new RelationalDescriptor();
        setupClassDescriptor();
    }

    private void setupClassDescriptor() {
        classDescriptor.setJavaClass(javaClass);
        classDescriptor.setCMPPolicy(new CMP3Policy());
        setupEmfMapping();
    }

    private void setupEmfMapping() {
        if (eClassifier instanceof EClass eClass) {
            setupEClass(eClass);
        }
    }

    private void setupEClass(EClass eClass) {
        classDescriptor.setInstantiationPolicy(new EmfInstantiationPolicy(eClass, javaClass));

        DatabaseTable primaryDatabaseTable = new DatabaseTable();
        primaryDatabaseTable.setName(eClass.getName());

        List<DatabaseTable> secondaryDatabaseTables = new ArrayList<>();

        if (oEntity.isPresent()) {
            Entity entity = oEntity.get();

            // primary Table
            if (entity.getTable() != null) {
                Table table = entity.getTable();
                if (table.getName() != null) {
                    primaryDatabaseTable.setName(table.getName());
                }
                if (table.getSchema() != null) {
                    primaryDatabaseTable.setTableQualifier(table.getSchema());
                }
            }

            // secondary Tables
            if (entity.getSecondaryTable() != null) {
                EList<SecondaryTable> secTables = entity.getSecondaryTable();
                if (secTables != null) {
                    for (SecondaryTable secondaryTable : secTables) {

                        DatabaseTable secondaryDatabaseTable = new DatabaseTable();
                        secondaryDatabaseTable.setName(secondaryTable.getName());
                        String schema = secondaryTable.getSchema();
                        if (schema != null) {
                            secondaryDatabaseTable.setTableQualifier(schema);
                            secondaryDatabaseTables.add(secondaryDatabaseTable);
                        }
                    }
                }
            }
        }

        classDescriptor.addTable(primaryDatabaseTable);
        classDescriptor.setDefaultTable(primaryDatabaseTable);

        for (DatabaseTable secondaryDatabaseTable : secondaryDatabaseTables) {
            classDescriptor.addTable(secondaryDatabaseTable);
        }

//			JPADynamicTypeBuilder dynamicTypeBuilder = new JPADynamicTypeBuilder(javaClass, null,
//					allTables.toArray(String[]::new));

        // Attributes
        for (EAttribute eAttribute : eClass.getEAttributes()) {
            Optional<Column> oColumn = tryColumnForAttribute(eAttribute);
            DatabaseField databaseField;
            if (oColumn.isEmpty()) {
                databaseField = new DatabaseField(eAttribute.getName(), primaryDatabaseTable);
            } else {
                Column column = oColumn.get();

                String currColName = column.getName();
                if (currColName == null) {
                    currColName = eAttribute.getName();
                }
                if (column.getTable() == null) {
                    databaseField = new DatabaseField(currColName, primaryDatabaseTable);
                } else {
                    String tableName = column.getTable();
                    if (tableName.equals(primaryDatabaseTable.getName())) {
                        databaseField = new DatabaseField(currColName, primaryDatabaseTable);
                    } else {
                        Optional<DatabaseTable> oSecondaryTable = secondaryDatabaseTables.stream()
                                .filter(t -> t.getName().equals(tableName)).findAny();

                        if (oSecondaryTable.isPresent()) {
                            databaseField = new DatabaseField(currColName, oSecondaryTable.get());
                        } else {
                            throw new IllegalArgumentException("Attribute : " + eAttribute.getName() + ", Column: "
                                    + currColName + " uses the Table: " + tableName
                                    + ", that is not defined in Secondary Tables: " + secondaryDatabaseTables.stream()
                                            .map(Object::toString).collect(Collectors.joining(", ")));
                        }
                    }
                }
            }

            DirectToFieldMapping directToFieldMapping = new DirectToFieldMapping();
            directToFieldMapping.setAttributeName(eAttribute.getName());
            directToFieldMapping.setField(databaseField);
            directToFieldMapping.setAttributeClassification(Util.convType(eAttribute));
            directToFieldMapping.setAttributeAccessor(new EmfAttributeAccesor(eAttribute));

            classDescriptor.addMapping(directToFieldMapping);

            if (eAttribute.isID()) {
                getDescriptor().addPrimaryKeyFieldName(eAttribute.getName());
                getDescriptor().setSequenceNumberName("SEQ_" + eClass.getName());
                getDescriptor().setSequenceNumberFieldName(eAttribute.getName());
            }
        }
    }

    public Class<?> getJavaClass() {
        return javaClass;
    }

    public Optional<Entity> getMappingEntity() {
        return oEntity;
    }

    public EClassifier geteClassifier() {
        return eClassifier;
    }

    public ClassDescriptor getDescriptor() {
        return classDescriptor;
    }

    private Optional<Column> tryColumnForAttribute(EAttribute eAttribute) {
        if (oEntity.isEmpty()) {
            return Optional.empty();
        }
        Entity entity = oEntity.get();
        if (entity.getAttributes() == null) {
            return Optional.empty();
        }
        for (Basic basic : entity.getAttributes().getBasic()) {
            if (basic.getName().equals(eAttribute)) {
                Column col = basic.getColumn();
                if (col != null) {
                    return Optional.of(col);
                }
            }
        }
        return Optional.empty();
    }
}
