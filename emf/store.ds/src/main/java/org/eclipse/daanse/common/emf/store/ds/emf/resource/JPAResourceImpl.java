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
*   Data In Motion - initial API and implementation
*/
package org.eclipse.daanse.common.emf.store.ds.emf.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.emf.ecore.resource.impl.ResourceImpl;

public class JPAResourceImpl extends ResourceImpl {

    private final JPAEngine engine;

    public JPAResourceImpl(JPAEngine engine) {
        this.engine = engine;
        this.engine.setResource(this);
        setURI(engine.getUri());

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.emf.ecore.resource.impl.ResourceImpl#doLoad(java.io.InputStream,
     * java.util.Map)
     */
    @Override
    protected void doLoad(InputStream inputStream, Map<?, ?> options) throws IOException {
        engine.load(options);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.emf.ecore.resource.impl.ResourceImpl#doSave(java.io.OutputStream,
     * java.util.Map)
     */
    @Override
    protected void doSave(OutputStream outputStream, Map<?, ?> options) throws IOException {
        engine.save(options);
    }

}
