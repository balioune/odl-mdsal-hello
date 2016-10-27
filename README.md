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
    
and alter HelloProvider.java to pass pass the DataBroker to the HelloWorldImpl(...) contstructor: 

        DataBroker db = session.getSALService(DataBroker.class);
        helloService = session.addRpcImplementation(HelloService.class, new HelloWorldImpl(db));
        
