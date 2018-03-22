# GWT UiBinder Example

This module (along with sub modules) is intended to be a testbed and showcase for the new GWT 
UiBinder module.

## Misc Info

This module was created from the 
[gwt-maven-archetype](https://github.com/tbroyer/gwt-maven-archetypes/).  This link can be used to
see how to compile, run code-server, etc. 

`gwt-uibinder-example` module (parent of *-{shared,client,server}) does not inherit from the root 
maven module.  This was intentional as this example is geared towards a "real world" scenario.
This means that some maven properties, plugins, etc will need to be updated as necessary as they will
not inherit from the root module.  