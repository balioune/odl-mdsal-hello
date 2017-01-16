### Prerequisite
You need the followings in order to compile and test this project:
- Java 8
- Maven 3

### Create the hello project:

         mvn archetype:generate -DarchetypeGroupId=org.opendaylight.controller \
         -DarchetypeArtifactId=opendaylight-startup-archetype \
         -DarchetypeVersion=1.1.2-Beryllium-SR2 \
         -DarchetypeRepository=http://nexus.opendaylight.org/content/repositories/opendaylight.release/ \
         -DarchetypeCatalog=https://nexus.opendaylight.org/content/repositories/opendaylight.release/archetype-catalog.xml

## And responding to the prompts as follows:

        Define value for property 'groupId': : org.opendaylight.hello
        Define value for property 'artifactId': : hello
        Define value for property 'package':  org.opendaylight.hello: : 
        Define value for property 'classPrefix':  Hello: : 
        Define value for property 'copyright': : Alioune, BA.

## Build the project

        cd hello/

        mvn clean install -DskipTests

## Running the project
        cd karaf/target/assembly/bin
        ./karaf

- Verifying

At the karaf prompt type and wait to see a message :

        log:tail

The entry point of the project is the class HelloProvider. Feel free to modify it and verify the output on logs.

        vim impl/src/main/java/org/opendaylight/hello/impl/HelloWordImpl.java

        /*
        * Copyright © 2015 Alioune, BA. and others.  All rights reserved.
        *
        * This program and the accompanying materials are made available under the
        * terms of the Eclipse Public License v1.0 which accompanies this distribution,
        * and is available at http://www.eclipse.org/legal/epl-v10.html
        */
        package org.opendaylight.hello.impl;

        
        import java.util.concurrent.Future;

        import org.opendaylight.controller.md.sal.binding.api.DataBroker;
        import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
        import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
        import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
        import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
        import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
        import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.GreetingRegistry;
        import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.GreetingRegistryBuilder;
        import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloService;
        import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldInput;
        import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldOutput;
        import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldOutputBuilder;
        import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.greeting.registry.GreetingRegistryEntry;
        import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.greeting.registry.GreetingRegistryEntryBuilder;
        import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.greeting.registry.GreetingRegistryEntryKey;
        import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
        import org.opendaylight.yangtools.yang.common.RpcResult;
        import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;

        import com.google.common.base.Optional;
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
           return RpcResultBuilder.success(output).buildFuture();
        }
        
        }

