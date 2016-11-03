/*
 * Copyright © 2015 Alioune, BA. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.hello.impl;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.FoutatoroTop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.GreetingRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.GreetingRegistryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.Node1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.foutatoro.top.Salutation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.foutatoro.top.SalutationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.foutatoro.top.SalutationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.greeting.registry.GreetingRegistryEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.greeting.registry.GreetingRegistryEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.greeting.registry.GreetingRegistryEntryKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.network.topology.topology.node.Peer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.network.topology.topology.node.PeerBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;


public class HelloWorldImpl implements HelloService{

    private static final Logger LOG = LoggerFactory.getLogger(HelloWorldImpl.class);
    private DataBroker db;
    
    public HelloWorldImpl(DataBroker db) {
        this.db = db;
        initializeDataTree(this.db);
    }
    
    @Override
    public Future<RpcResult<HelloWorldOutput>> helloWorld(HelloWorldInput input) {
        HelloWorldOutput output = new HelloWorldOutputBuilder()
                .setGreeting("Hello " + input.getName())
                .build();
        writeToGreetingRegistry(input,output);
        writeOrangeTopo(input,output);
            return RpcResultBuilder.success(output).buildFuture();
    }
    
    private void initializeDataTree(DataBroker db) {
        LOG.info("Preparing to initialize the greeting registry");
        WriteTransaction transaction = db.newWriteOnlyTransaction();
        InstanceIdentifier<GreetingRegistry> iid = InstanceIdentifier.create(GreetingRegistry.class);
        GreetingRegistry greetingRegistry = new GreetingRegistryBuilder()
                .build();
        transaction.put(LogicalDatastoreType.OPERATIONAL, iid, greetingRegistry);
        CheckedFuture<Void, TransactionCommitFailedException> future = transaction.submit();
        Futures.addCallback(future, new LoggingFuturesCallBack<>("Failed to create greeting registry", LOG));
        
        LOG.info("Preparing to initialize Orange network topology");
        // Orange Network Topology
        OrangeNetworkInitTopology(LogicalDatastoreType.OPERATIONAL);
        LOG.info("Orange network topology has been initialized ");
                
    }

    private void writeToGreetingRegistry(HelloWorldInput input, HelloWorldOutput output) {
        WriteTransaction transaction = db.newWriteOnlyTransaction();
        InstanceIdentifier<GreetingRegistryEntry> iid = toInstanceIdentifier(input);
        GreetingRegistryEntry greeting = new GreetingRegistryEntryBuilder()
                .setGreeting(output.getGreeting())
                .setName(input.getName())
                .build();
        transaction.put(LogicalDatastoreType.OPERATIONAL, iid, greeting);
        CheckedFuture<Void, TransactionCommitFailedException> future = transaction.submit();
        Futures.addCallback(future, new LoggingFuturesCallBack<Void>("Failed to write greeting to greeting registry", LOG));
    }

    private InstanceIdentifier<GreetingRegistryEntry> toInstanceIdentifier(HelloWorldInput input) {
        InstanceIdentifier<GreetingRegistryEntry> iid = InstanceIdentifier.create(GreetingRegistry.class)
            .child(GreetingRegistryEntry.class, new GreetingRegistryEntryKey(input.getName()));
        return iid;
    }
    
    /**
     * Orange Network topology
     */
    private void OrangeNetworkInitTopology(LogicalDatastoreType type){
        //Initialize Topology in order to insert the augmented topo
        ReadWriteTransaction topoTransaction = this.db.newReadWriteTransaction();
        initializeTopology(type);
        InstanceIdentifier<Topology> path = InstanceIdentifier.create(NetworkTopology.class).child(Topology.class, new TopologyKey(new TopologyId("orange")));
        CheckedFuture<com.google.common.base.Optional<Topology>, ReadFailedException> nodeOptional;
        nodeOptional = topoTransaction.read(type, path);
        Topology orangeTopo = new TopologyBuilder().build();
        //topoTransaction.put(type, path, orangeTopo);
        try {
            if (!nodeOptional.get().isPresent()) {
            	LOG.info("node Optional is present");
                TopologyBuilder tpb = new TopologyBuilder();
                tpb.setTopologyId(new TopologyId(new TopologyId("orange")));
                topoTransaction.put(type, path, tpb.build());
                topoTransaction.submit();
            } else {
            	LOG.info("node Optional is NOT present");
            	topoTransaction.cancel();
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.info("Error initializing orange topology", e);
        }
    }
    
    private void initializeTopology(LogicalDatastoreType type) {
        ReadWriteTransaction transaction = db.newReadWriteTransaction();
        InstanceIdentifier<NetworkTopology> path = InstanceIdentifier.create(NetworkTopology.class);
        CheckedFuture<com.google.common.base.Optional<NetworkTopology>, ReadFailedException> topology = transaction.read(type,path);
        try {
            if (!topology.get().isPresent()) {
            	LOG.info("topology is present");
                NetworkTopologyBuilder ntb = new NetworkTopologyBuilder();
                transaction.put(type,path,ntb.build());
                transaction.submit();
            } else {
            	LOG.info("topology is NOT present");
                transaction.cancel();
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.info("Error initializing orange topology", e);
        }
    }
    
    private InstanceIdentifier<Salutation> OrangeInstanceIdentifier(HelloWorldInput input) {

    	InstanceIdentifier<Salutation>  identifier = InstanceIdentifier.create(Peer.class).child(Salutation.class, new SalutationKey(input.getName()));	
    	/** builder(Salutation.class, new SalutationKey(input.getName()));**/
    	return identifier;
    }
    
    private void writeOrangeTopo(HelloWorldInput input, HelloWorldOutput output) {
        WriteTransaction transaction = db.newWriteOnlyTransaction();
        InstanceIdentifier<Salutation> iid = OrangeInstanceIdentifier(input);
        Salutation salutation = new SalutationBuilder()
                .setGreeting(output.getGreeting())
                .setName(input.getName())
                .build();
        transaction.put(LogicalDatastoreType.OPERATIONAL, iid, salutation);
        CheckedFuture<Void, TransactionCommitFailedException> future = transaction.submit();
        Futures.addCallback(future, new LoggingFuturesCallBack<Void>("Failed to write greeting to greeting registry", LOG));
    }
}
