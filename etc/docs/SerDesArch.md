## SERDES Architecture Doc

This is mostly being written so I can grok what I'm trying to write

### SERDES Pools

A SerDes pool is a common serialisation item; the simplest would be a single lock. Basicly it is a common mutex for anything wanting to interface with it.

It features two methods:

- Generating a configuration object from a string->string map
- Executing a runable under it's serialisation regime with a given config.

The config being null should always be legal.

### SERDES Filters
 
A filter describes a class -> pool relationship.

It describes two methods:

- Enumerating supported classes
- Executing a given target runnable for a target class

#### Hook Types

These describe types of hooks and allow for minimising the search space for any given hook

For example, all tile entity tick() handling events are all executing a single standard template method, and all inherit from ITickableTileEntity