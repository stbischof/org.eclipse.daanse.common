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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.resource.Resource;

public class JPAWrapperEObject extends EObjectImpl implements EObject {

    private EObject eObject;

    public JPAWrapperEObject() {
        super();
    }

    @Override
    public EList<Adapter> eAdapters() {
        return eObject.eAdapters();
    }

    @Override
    public boolean eDeliver() {
        return eObject.eDeliver();
    }

    @Override
    public void eSetDeliver(boolean deliver) {
        eObject.eSetDeliver(deliver);
    }

    @Override
    public void eNotify(Notification notification) {
        eObject.eNotify(notification);
    }

    @Override
    public EClass eClass() {
        return eObject.eClass();
    }

    @Override
    public Resource eResource() {
        return eObject.eResource();
    }

    @Override
    public EObject eContainer() {
        return eObject.eContainer();
    }

    @Override
    public EStructuralFeature eContainingFeature() {
        return eObject.eContainingFeature();
    }

    @Override
    public EReference eContainmentFeature() {
        return eObject.eContainmentFeature();
    }

    @Override
    public EList<EObject> eContents() {
        return eObject.eContents();
    }

    @Override
    public TreeIterator<EObject> eAllContents() {
        return eObject.eAllContents();
    }

    @Override
    public boolean eIsProxy() {
        return eObject.eIsProxy();
    }

    @Override
    public EList<EObject> eCrossReferences() {
        return eObject.eCrossReferences();
    }

    @Override
    public Object eGet(EStructuralFeature feature) {
        return eObject.eGet(feature);
    }

    @Override
    public Object eGet(EStructuralFeature feature, boolean resolve) {
        return eObject.eGet(feature, resolve);
    }

    @Override
    public void eSet(EStructuralFeature feature, Object newValue) {
        eObject.eSet(feature, newValue);
    }

    @Override
    public boolean eIsSet(EStructuralFeature feature) {
        return eObject.eIsSet(feature);
    }

    @Override
    public void eUnset(EStructuralFeature feature) {
        eObject.eUnset(feature);
    }

    @Override
    public Object eInvoke(EOperation operation, EList<?> arguments) throws InvocationTargetException {
        return eObject.eInvoke(operation, arguments);
    }

    public void init(EObject eObject) {
        this.eObject = eObject;
    }

}
