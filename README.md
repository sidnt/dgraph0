# dgraph0

[message registry app - sources](https://github.com/sidnt/dgraph0/tree/main/src/main/scala/clientApps/messageRegistry)  
a local dgraph instance must be up and writable to  
`sbt runMain clientApps.messageRegistry.CreateMessageSchema` to create the required schema in dgraph  
`sbt runMain clientApps.Cli` to interact with the database from CLI  
- needs a local dg instance up
- can store / query for simple messages based on terms
