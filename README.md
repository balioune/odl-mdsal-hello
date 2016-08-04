### Test HelloIT

In this part we will manually test our HelloWorld RPC. It can be seen as an invocation of our RPC by another ODL App.

             Edit HelloIT class in the package org.opendaylight.hello.it

### Integration test our HelloWorld RPC


### Debugging your integration test

             cd it/
             mvn clean install -Dkaraf.debug

This will launch the IT test karaf container listening on port 5005.

Create a debug config and place a debug point in the testRPC test and walk through your code. 
