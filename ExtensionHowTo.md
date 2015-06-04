# Extension HowTo #

## Implementation ##

An OpenID4Java extension needs to implement the following interfaces:

### MessageExtensionFactory ###
```
public MessageExtension getExtension(ParameterList parameterList, boolean isRequest) throws MessageException;
```

### MessageExtension ###
```
public String getTypeUri();
public ParameterList getParameters();
public void  setParameters(ParameterList params);
public boolean providesIdentifier();
public boolean signRequired();
```

For working examples please have a look at the Attribute Exchange and Simple Registration implementation in the message.ax and message.sreg packages.

A typical scenario is to implement both interfaces in an ExampleMessage base class (for a hypothetical "Example Extension"), and then have extension specific messages inherit from the ExampleMessage class.

Once the interfaces are implemented, the factory must be registered with the Message class.

If the implementation is part of the openid4java package (and will therefore always be available), this initialization can be hardcoded in the Message class:

```
static
{
    _extensionFactories.put(AxMessage.OPENID_NS_AX, AxMessage.class);
    _extensionFactories.put(SRegMessage.OPENID_NS_SREG, SRegMessage.class);
    _extensionFactories.put(ExampleMessage.OPENID_NS_EXAMPLE, ExampleMessage.class);
}
```

Otherwise, the extension facory can be registered at runtime with the following static method:
```
Message.addExtensionFactory(ExampleMessage.class);
```


## Usage ##

### Adding an extension to a message ###

```
Message msg =...;
Extension ext = new ExampleMessage(...)
msg.addExtension(ext);
```

### Extracting an extension from a message ###
```
Message msg = ...;
String typeUri = "...";
Extension ext = msg.getExtension(typeUri);
if (ext instance of <ExampleMessageRequest>)
{
    ... process specific extension message ...
}
else if (ext instance of <ExampleMessageResponse>)
{
    ... process specific extension message ...
}
```
