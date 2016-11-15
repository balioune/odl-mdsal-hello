## Model a simple HelloWorld GreetingRegistry

In this part, we are going to create a container for storing a list of greeting.
By doing that, all users that call the rpc and the greeting they receive are stored in the date tree 

## Edit api/src/main/yang/hello.yang

    container greeting-registry {
        list greeting-registry-entry {
            key "name";
            leaf name {
                type string;
            }
            leaf greeting {
                type string;
            }
        }
    }

Rebuild 

     mvn clean install -DskipTests


## Make DataBroker available to HelloWorldImp.java

    private DataBroker db;

    public HelloWorldImpl(DataBroker db) {
        this.db = db;
    }
    
and alter HelloProvider.java to pass the DataBroker to the HelloWorldImpl(...) contstructor: 

        DataBroker db = session.getSALService(DataBroker.class);
        helloService = session.addRpcImplementation(HelloService.class, new HelloWorldImpl(db));
        
## Have HelloWorldImpl consult the OPERATIONAL GreetingRegistry when responding
We do that to avoid adding a greeting that already exists in the data store

            private String readFromGreetingRegistry(HelloWorldInput input) {
        String result = "Hello " + input.getName();
        ReadOnlyTransaction transaction = db.newReadOnlyTransaction();
        InstanceIdentifier<GreetingRegistryEntry> iid = toInstanceIdentifier(input);
        CheckedFuture<Optional<GreetingRegistryEntry>, ReadFailedException> future =
                transaction.read(LogicalDatastoreType.OPERATIONAL, iid);
        Optional<GreetingRegistryEntry> optional = Optional.absent();
        try {
            optional = future.checkedGet();
        } catch (ReadFailedException e) {
            LOG.warn("Reading greeting failed:",e);
        }
        if(optional.isPresent()) {
            result = optional.get().getGreeting();
        }
        return result;
    }
