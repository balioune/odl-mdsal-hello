/*
 * Copyright © 2015 Alioune, BA. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.hello.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloProvider implements BindingAwareProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(HelloProvider.class);
    private RpcRegistration<HelloService> helloService;

    @Override
    public void onSessionInitiated(ProviderContext session) {
    	// step 4
    	DataBroker db = session.getSALService(DataBroker.class);
    	helloService = session.addRpcImplementation(HelloService.class, new HelloWorldImpl(db));
        //helloService = session.addRpcImplementation(HelloService.class, new HelloWorldImpl());
        LOG.info("HelloProvider Session Initiated");
    }

    @Override
    public void close() throws Exception {
        if (helloService != null) {
            helloService.close();
        }
        LOG.info("HelloProvider Closed");
    }

}
