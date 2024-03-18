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
import java.util.stream.Collectors;

import org.eclipse.daanse.common.emf.model.emfdbmapping.Basic;
import org.eclipse.daanse.common.emf.model.emfdbmapping.Column;
import org.eclipse.daanse.common.emf.model.emfdbmapping.Entity;
import org.eclipse.daanse.common.emf.model.emfdbmapping.SecondaryTable;
import org.eclipse.daanse.common.emf.model.emfdbmapping.Table;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.helper.DatabaseTable;
import org.eclipse.persistence.jpa.dynamic.JPADynamicTypeBuilder;
import org.eclipse.persistence.mappings.DirectToFieldMapping;

public class EmfDynamicEntity {

    private EClassifier eClassifier;
    private Optional<Entity> oMappingEntity;
    private Class<?> javaClass;

    public EmfDynamicEntity(EClassifier eClassifier, Optional<Entity> mappingEntity, Class<?> javaClass) {
        this.eClassifier = eClassifier;
        this.oMappingEntity = mappingEntity;
        this.javaClass = javaClass;
    }

    public DynamicType calcDynamicType() {
        if (eClassifier instanceof EClass eClass) {

            String primaryTable = eClass.getName();
            String primaryTableSchema = "";
            List<SecondaryTable> secondaryTables = new ArrayList<>();

            if (oMappingEntity.isPresent()) {
                Entity entity = oMappingEntity.get();

                // primary Table
                if (entity.getTable() != null) {
                    Table table = entity.getTable();
                    if (table.getName() != null) {
                        primaryTable = table.getName();
                    }
                    if (table.getSchema() != null) {
                        primaryTableSchema = table.getSchema();
                    }
                }

                // secondary Tables
                if (entity.getSecondaryTable() != null) {
                    EList<SecondaryTable> secTables = entity.getSecondaryTable();
                    if (secTables != null) {
                        for (SecondaryTable secondaryTable : secTables) {
                            secondaryTables.add(secondaryTable);
                        }
                    }
                }
            }

            List<String> allTables = new ArrayList<String>();
            allTables.add(primaryTable);
            for (SecondaryTable currSecondaryTable : secondaryTables) {
                allTables.add(currSecondaryTable.getName());
            }

            JPADynamicTypeBuilder dynamicTypeBuilder = new JPADynamicTypeBuilder(javaClass, null,
                    allTables.toArray(String[]::new));

            // Attributes
            for (EAttribute eAttribute : eClass.getEAttributes()) {

                Optional<Column> oColumn = tryColumnForAttribute(eAttribute);

                DatabaseField databaseField;

                if (oColumn.isEmpty()) {
                    databaseField = new DatabaseField(eAttribute.getName());
                } else {
                    Column column = oColumn.get();

                    String currColName = column.getName();
                    if (currColName == null) {
                        currColName = eAttribute.getName();
                    }

                    if (column.getTable() == null) {

                        databaseField = new DatabaseField(currColName);

                    } else {
                        String tableName = column.getTable();

                        if (tableName.equals(primaryTable)) {
                            DatabaseTable databaseTable = new DatabaseTable(primaryTable, primaryTableSchema);
                            databaseField = new DatabaseField(currColName, databaseTable);
                        } else {

                            Optional<SecondaryTable> oTable = secondaryTables.stream()
                                    .filter(t -> t.getName().equals(tableName)).findAny();

                            if (oTable.isPresent()) {
                                SecondaryTable table = oTable.get();
                                String tName = table.getName();
                                if (tName == null) {
                                    tName = "";
                                }
                                String sName = table.getSchema();
                                if (sName == null) {
                                    sName = "";
                                }
                                DatabaseTable databaseTable = new DatabaseTable(tName, sName);
                                databaseField = new DatabaseField(currColName, databaseTable);
                            } else {
                                throw new IllegalArgumentException("Attribute : " + eAttribute.getName() + ", Column: "
                                        + currColName + " uses the Table: " + tableName
                                        + ", that is not defined in Secondary Tables: " + secondaryTables.stream()
                                                .map(Object::toString).collect(Collectors.joining(", ")));
                            }
                        }
                    }
                }

                DirectToFieldMapping directToFieldMapping = new DirectToFieldMapping();
                directToFieldMapping.setAttributeName(eAttribute.getName());
                directToFieldMapping.setField(databaseField);
                directToFieldMapping.setAttributeClassification(Util.convType(eAttribute));

                dynamicTypeBuilder.addMapping(directToFieldMapping);

                if (eAttribute.isID()) {
                    dynamicTypeBuilder.setPrimaryKeyFields(eAttribute.getName());
                    dynamicTypeBuilder.configureSequencing("SEQ_" + eClass.getName(), eAttribute.getName());
                }
            }
            return dynamicTypeBuilder.getType();

        }
        return null;

    }

    private Optional<Column> tryColumnForAttribute(EAttribute eAttribute) {

        if (oMappingEntity.isEmpty()) {
            return Optional.empty();
        }
        Entity entity = oMappingEntity.get();

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
