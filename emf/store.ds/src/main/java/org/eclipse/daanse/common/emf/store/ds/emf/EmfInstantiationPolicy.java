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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.persistence.exceptions.DescriptorException;
import org.eclipse.persistence.exceptions.DiscoveryException;
import org.eclipse.persistence.internal.descriptors.InstantiationPolicy;

public class EmfInstantiationPolicy extends InstantiationPolicy {

    private static final long serialVersionUID = 1L;
    private transient EClass eClass;
    private Class<?> javaClass;

    public EmfInstantiationPolicy(EClass eClass, Class<?> javaClass) {
        super();
        this.eClass = eClass;
        this.javaClass = javaClass;
    }

    @Override
    public Object buildNewInstance() throws DescriptorException {

        EObject eObject = EcoreUtil.create(eClass);// eClass.getEPackage().getEFactoryInstance().create(eClass);
        if (JPAWrapperEObject.class.isAssignableFrom(javaClass)) {

            try {
                JPAWrapperEObject wrapper = (JPAWrapperEObject) javaClass.getConstructor().newInstance();
                wrapper.init(eObject);
                return wrapper;
            } catch (Exception e) {
                throw new DiscoveryException(e.getMessage());
            }
        }
        return eObject;
    }

}
