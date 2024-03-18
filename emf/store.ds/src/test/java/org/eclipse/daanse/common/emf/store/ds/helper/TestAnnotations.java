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
package org.eclipse.daanse.common.emf.store.ds.helper;

import static org.osgi.test.common.annotation.Property.ValueSource.SystemProperty;
import static org.osgi.test.common.annotation.Property.ValueSource.TestClass;
import static org.osgi.test.common.annotation.Property.ValueSource.TestMethod;
import static org.osgi.test.common.annotation.Property.ValueSource.TestUniqueId;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.eclipse.daanse.common.emf.store.ds.EntityManagerFactoryConfigurator;
import org.eclipse.daanse.common.emf.store.ds.api.JpaEmFConstants;
import org.eclipse.daanse.common.io.fs.watcher.api.FileSystemWatcherWhiteboardConstants;
import org.eclipse.daanse.common.jdbc.datasource.metatype.h2.api.Constants;
import org.gecko.emf.osgi.constants.EMFNamespaces;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.TemplateArgument;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;

public class TestAnnotations {

    @WithFactoryConfiguration(location = "?", factoryPid = org.eclipse.daanse.common.jdbc.loader.csv.api.Constants.PID_LOADER_FILEWATCHER, properties = { //
            @Property(key = "dataSource.target", value = "(" + MARKER_TEST_UNIQUEID_HEX + "=%h)", templateArguments = {
                    @TemplateArgument(source = TestUniqueId) }),
            @Property(key = FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_PATH, value = "/%s/target/test-classes/%s/%s/data/", //
                    templateArguments = { //
                            @TemplateArgument(source = SystemProperty, value = "basePath"), //
                            @TemplateArgument(source = TestClass), //
                            @TemplateArgument(source = TestMethod) }), //
    })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SaveCsvInDb {

    }

    @WithFactoryConfiguration(location = "?", factoryPid = JpaEmFConstants.PID_JPA_RESOURCE_FACTORY, properties = { //
            @Property(key = JpaEmFConstants.JPA_RESOURCE_FACTORY_PROPERTY_EMF + ".target", value = "("
                    + MARKER_TEST_UNIQUEID_HEX + "=%h)", //
                    templateArguments = { @TemplateArgument(source = TestUniqueId) }) })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SetupJpaResouceFactory {
    }

    public static final String PREFIX_MARKER_TESTING = "marker.testing.";
    public static final String MARKER_TEST_CLASS = PREFIX_MARKER_TESTING + "TestClass";
    public static final String MARKER_TEST_METHOD = PREFIX_MARKER_TESTING + "TestMethod";
    public static final String MARKER_TEST_UNIQUEID = PREFIX_MARKER_TESTING + "TestUniqueId";
    public static final String MARKER_TEST_UNIQUEID_HEX = PREFIX_MARKER_TESTING + "TestUniqueId.hex";

    @WithFactoryConfiguration(location = "?", factoryPid = Constants.PID_DATASOURCE, properties = { //
            @Property(key = Constants.DATASOURCE_PROPERTY_PLUGABLE_FILESYSTEM, value = Constants.OPTION_PLUGABLE_FILESYSTEM_FILE), //
            @Property(key = Constants.DATASOURCE_PROPERTY_IDENTIFIER, value = "~/%s_%s_%h", //
                    templateArguments = { //
                            @TemplateArgument(source = TestClass), //
                            @TemplateArgument(source = TestMethod), //
                            @TemplateArgument(source = TestUniqueId) }), //
            @Property(key = MARKER_TEST_CLASS, source = TestClass), //
            @Property(key = MARKER_TEST_METHOD, source = TestMethod), //
            @Property(key = MARKER_TEST_UNIQUEID, source = TestUniqueId), //
            @Property(key = MARKER_TEST_UNIQUEID_HEX, value = "%h", templateArguments = @TemplateArgument(source = TestUniqueId)), //
    })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RegisterH2DataSourceWithMarkerTestClassAnsMethodName {
    }

    @WithFactoryConfiguration(location = "?", factoryPid = EMFNamespaces.DYNAMIC_MODEL_CONFIGURATOR_CONFIG_NAME, properties = { //
            @Property(key = EMFNamespaces.EMF_MODEL_DYNAMIC_URI, value = "file:///%s/target/test-classes/%s/model.ecore", //
                    templateArguments = { //
                            @TemplateArgument(source = SystemProperty, value = "basePath"),
                            @TemplateArgument(source = TestClass) }), //
            @Property(key = MARKER_TEST_CLASS, source = TestClass), //
            @Property(key = MARKER_TEST_METHOD, source = TestMethod), //
            @Property(key = MARKER_TEST_UNIQUEID, source = TestUniqueId), //
            @Property(key = MARKER_TEST_UNIQUEID_HEX, value = "%h", templateArguments = @TemplateArgument(source = TestUniqueId)), //
    })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RegisterDynamicModelPackageWithTestClassName {
    }

    @WithConfiguration(location = "?", pid = EntityManagerFactoryConfigurator.PID, properties = {
            @Property(key = "dataSource.target", value = "(" + MARKER_TEST_UNIQUEID_HEX + "=%h)", templateArguments = {
                    @TemplateArgument(source = TestUniqueId) }),
            @Property(key = "ePackage.target", value = "(emf.name=%s)", templateArguments = {
                    @TemplateArgument(source = TestClass) }),
            @Property(key = "mappingFile", value = "file:///%s/target/test-classes/%s/%s/instance.xmi", //
                    templateArguments = { //
                            @TemplateArgument(source = SystemProperty, value = "basePath"), //
                            @TemplateArgument(source = TestClass), //
                            @TemplateArgument(source = TestMethod) }),
            @Property(key = MARKER_TEST_CLASS, source = TestClass), //
            @Property(key = MARKER_TEST_METHOD, source = TestMethod), //
            @Property(key = MARKER_TEST_UNIQUEID, source = TestUniqueId), //
            @Property(key = MARKER_TEST_UNIQUEID_HEX, value = "%h", templateArguments = @TemplateArgument(source = TestUniqueId)), //
    })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SetupEntityManagerFactoryWithModelDbAndMapping {
    }

}
