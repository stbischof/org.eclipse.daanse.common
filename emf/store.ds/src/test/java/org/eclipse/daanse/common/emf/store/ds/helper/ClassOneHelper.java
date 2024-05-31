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

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

public class ClassOneHelper {
    public static void assertEObject(EObject eObject) {
        assertThat(eObject).isNotNull();

        EStructuralFeature attributeOne = eObject.eClass().getEStructuralFeature("attributeOne");
        EStructuralFeature attributeTwo = eObject.eClass().getEStructuralFeature("attributeTwo");
        EStructuralFeature attributeThree = eObject.eClass().getEStructuralFeature("attributeThree");

        Object foo = eObject.eGet(attributeOne);
        Object bar = eObject.eGet(attributeTwo);
        Object buzz = eObject.eGet(attributeThree);

        assertThat(foo).isEqualTo("foo");
        assertThat(bar).isEqualTo("bar");
        assertThat(buzz).isEqualTo("buzz");
    }
}
