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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.internal.jpa.deployment.SEPersistenceUnitProperty;

import jakarta.persistence.PersistenceException;
import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.spi.PersistenceUnitTransactionType;

public class DynamicPersistenceUnitInfo implements PersistenceUnitInfo {

    protected SharedCacheMode cacheMode;
    protected ValidationMode validationMode;
    protected String persistenceUnitName;
    protected String persistenceProviderClassName;
    protected DataSource jtaDataSource;
    protected DataSource nonJtaDataSource;
    protected PersistenceUnitTransactionType persistenceUnitTransactionType;
    protected List<String> mappingFiles;

    private Collection<String> jarFiles = new ArrayList<>();
    protected List<URL> jarFileUrls = List.of();
    protected List<String> managedClassNames;
    protected URL persistenceUnitRootUrl;
    protected boolean excludeUnlistedClasses = true;

    protected List<SEPersistenceUnitProperty> persistenceUnitProperties = new ArrayList<>();
    protected Properties properties;

    protected ClassLoader tempClassLoader;
    protected ClassLoader realClassLoader;

    public DynamicPersistenceUnitInfo(String name, URL persistenceUnitRootUrl, Map<String, Object> props) {

        persistenceUnitName = name;
        this.persistenceUnitRootUrl = persistenceUnitRootUrl;
        mappingFiles = new ArrayList<>();
        managedClassNames = new ArrayList<>();

        properties = new Properties();
        properties.putAll(properties);

        if (props.containsKey(PersistenceUnitProperties.CLASSLOADER)) {
            setClassLoader((ClassLoader) props.get(PersistenceUnitProperties.CLASSLOADER));
        }
        persistenceUnitTransactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;
    }

    @Override
    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public void setPersistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    public List<SEPersistenceUnitProperty> getPersistenceUnitProperties() {
        return persistenceUnitProperties;
    }

    @Override
    public String getPersistenceProviderClassName() {
        return persistenceProviderClassName;
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return persistenceUnitTransactionType;
    }

    @Override
    public DataSource getJtaDataSource() {
        return jtaDataSource;
    }

    @Override
    public DataSource getNonJtaDataSource() {
        return nonJtaDataSource;
    }

    public void setNonJtaDataSource(DataSource nonJtaDataSource) {
        this.nonJtaDataSource = nonJtaDataSource;
    }

    @Override
    public List<String> getMappingFileNames() {
        return mappingFiles;
    }

    @Override
    public List<URL> getJarFileUrls() {
        return Collections.unmodifiableList(jarFileUrls);
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
        return persistenceUnitRootUrl;
    }

    @Override
    public List<String> getManagedClassNames() {
        return managedClassNames;
    }

    @Override
    public boolean excludeUnlistedClasses() {
        return excludeUnlistedClasses;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public ClassLoader getClassLoader() {
        return realClassLoader;
    }

    @Override
    public ClassLoader getNewTempClassLoader() {
        return tempClassLoader;
    }

    public void setClassLoader(ClassLoader loader) {
        this.realClassLoader = loader;
    }

    public Collection<String> getJarFiles() {
        return jarFiles;
    }

    @Override
    public String getPersistenceXMLSchemaVersion() {
        throw new PersistenceException("Not Yet Implemented");
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
        return cacheMode;
    }

    @Override
    public ValidationMode getValidationMode() {
        return validationMode;
    }

    @Override
    public void addTransformer(ClassTransformer transformer) {

    }

}
