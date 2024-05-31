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
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.sql.DataSource;

import org.eclipse.daanse.common.emf.model.emfdbmapping.EmfDbMappingPackage;
import org.eclipse.daanse.common.emf.model.emfdbmapping.EntityMappingsType;
import org.eclipse.daanse.common.emf.store.ds.emf.EmfType;
import org.eclipse.daanse.common.emf.store.ds.emf.JPAWrapperEObject;
import org.eclipse.daanse.common.emf.store.ds.emf.helper.JPAEmfHelper;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.dynamic.DynamicClassLoader;
import org.gecko.emf.osgi.constants.EMFNamespaces;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceProvider;

@Designate(factory = true, ocd = EntityManagerFactoryConfigurator.Config.class)
@Component(configurationPid = { EntityManagerFactoryConfigurator.PID })
public class EntityManagerFactoryConfigurator {

    public static final String PID = "org.eclipse.daanse.common.emf.store.ds.EntityManagerFactoryConfigurator";

    @ObjectClassDefinition
    @interface Config {

        @AttributeDefinition()
        String mappingFile();
    }

    @Reference
    DataSource dataSource;

    @Reference
    private EPackage ePackage;

    @Reference(target = "(" + EMFNamespaces.EMF_MODEL_NAME + "=" + EmfDbMappingPackage.eNAME + ")")
    private ResourceSet rsMapping;

//    private List<DynamicType> dynamicTypes;

    @Activate
    void activate(BundleContext bCtx, EntityManagerFactoryConfigurator.Config config, Map<String, Object> properties)
            throws IOException {

        DynamicClassLoader dcl = new DynamicClassLoader(DynamicClassLoader.class.getClassLoader()) {

            @Override
            public URL getResource(String name) {
                return super.getResource(name);
            }

            @Override
            public InputStream getResourceAsStream(String name) {
                return super.getResourceAsStream(name);
            }

            @Override
            public Enumeration<URL> getResources(String name) throws IOException {
                if ("META-INF/persistence.xml".equals(name)) {
                    return bCtx.getBundle().getResources(name);
                }
                return super.getResources(name);
            }

            @Override
            protected Class<?> findClass(String className) throws ClassNotFoundException {
                if (className.equals(JPAWrapperEObject.class.getName())) {
                    return JPAWrapperEObject.class;
                }
                return super.findClass(className);
            }
        };

        Resource res = rsMapping.createResource(URI.createURI(config.mappingFile()));
        res.load(Map.of());

        EntityMappingsType emt = (EntityMappingsType) res.getContents().get(0);

        MappingDynamicInstanceGenerator generator = new MappingDynamicInstanceGenerator(dcl, emt);

//		List<EmfDynamicEntity> list = generator.generate(ePackage);
//		dynamicTypes = list.stream().map(EmfDynamicEntity::calcDynamicType).collect(Collectors.toList());

        List<EmfType> emfTypes = generator.generateEmfType(ePackage);

        PersistenceProvider persistenceProvider = new org.eclipse.persistence.jpa.PersistenceProvider();

        URL url = bCtx.getBundle().getEntry("META-INF/persistence.xml");

        System.out.println(url);
        HashMap<String, Object> map = new HashMap<>();
        map.put(PersistenceUnitProperties.CLASSLOADER, dcl);
        map.put(PersistenceUnitProperties.WEAVING, "static");
        map.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, dataSource);
        map.put("eclipselink.target-database", "org.eclipse.persistence.platform.database.H2Platform");
        map.put("eclipselink.logging.level", "FINE");
        map.put("eclipselink.logging.timestamp", "false");
        map.put("eclipselink.logging.thread", "false");
        map.put("eclipselink.logging.exceptions", "true");
        map.put("eclipselink.orm.throw.exceptions", "true");
        map.put("eclipselink.jdbc.read-connections.min", "1");
        map.put("eclipselink.jdbc.write-connections.min", "1");
        map.put("eclipselink.ddl-generation", "none");

        map.put("eclipselink.cache.shared.default", "false");
//		map.put("eclipselink.ddl-generation.output-mode", "both");

        DynamicPersistenceUnitInfo pui = new DynamicPersistenceUnitInfo("DynamicEmf" + UUID.randomUUID(), url, map);
        EntityManagerFactory emf = persistenceProvider.createContainerEntityManagerFactory(pui, map);

//		JPADynamicHelper helper = new JPADynamicHelper(emf);
//		DynamicType[] types = dynamicTypes.stream().toArray(DynamicType[]::new);
//		helper.addTypes(true, true, types);

        JPAEmfHelper helper = new JPAEmfHelper(emf);
        helper.addTypes(true, true, emfTypes);

//		SchemaManager schemaManager = new SchemaManager(helper.getSession());
//		schemaManager.outputCreateDDLToWriter(new PrintWriter(System.out));
//		schemaManager.outputCreateDDLToWriter(new PrintWriter(System.out));
//		schemaManager.outputDropDDLToWriter(new PrintWriter(System.out));
//		schemaManager.replaceDefaultTables();
//		schemaManager.setCreateSQLFiles(true);

        Dictionary<String, Object> entityMapperProps = new Hashtable<String, Object>();

        for (Entry<String, Object> e : properties.entrySet()) {
            if (e.getKey().endsWith(".target")) {
                continue;
            }
            entityMapperProps.put(e.getKey(), e.getValue());

        }
        bCtx.registerService(EntityManagerFactory.class, emf, entityMapperProps);

    }

}
