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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.persistence.exceptions.DescriptorException;
import org.eclipse.persistence.mappings.AttributeAccessor;

public class EmfAttributeAccesor extends AttributeAccessor {

    private static final long serialVersionUID = 1L;

    private transient EAttribute eAttribute;

    public EmfAttributeAccesor(EAttribute eAttribute) {
        super();
        this.eAttribute = eAttribute;
    }

    @Override
    public Object getAttributeValueFromObject(Object object) throws DescriptorException {
        EObject eObject = (EObject) object;
        return eObject.eGet(eAttribute);
    }

    @Override
    public void setAttributeValueInObject(Object object, Object value) throws DescriptorException {
        EObject eObject = (EObject) object;
        eObject.eSet(eAttribute, value);
    }

}
