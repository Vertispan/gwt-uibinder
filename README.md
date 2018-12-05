# GWT UiBinder

This project is the APT conversion of the GWT 2 UiBinder module in preparation of GWT 3 and J2CL.

The goals for this project is to replace/migrate the code generation from the older GWT 2 generator
API to Java APT.  Future goals will include migration to new widget library, added 
features/functionality for UiBinder templates, etc.

## Getting Started

### Building

Currently, none of the GWT UiBinder jars have been uploaded to maven central.  For the time being, 
this repository can be cloned or 
[directly downloaded](https://github.com/Vertispan/gwt-uibinder/archive/master.zip). Once that is 
done, perform a manual maven build like the following:

`mvn clean install`

Then, add the uibinder dependencies to your Maven module:

```xml
  <dependency>
    <groupId>org.gwtproject.uibinder</groupId>
    <artifactId>gwt-uibinder-client</artifactId>
    <version>1.0.0-SNAPSHOT</version>  
  </dependency>

  <dependency>
    <groupId>org.gwtproject.uibinder</groupId>
    <artifactId>gwt-uibinder-processor</artifactId>
    <version>1.0.0-SNAPSHOT</version>  
  </dependency>
```

**Note:** This is a snapshot version, so use at your own risk.

The new GWT UiBinder has been completely renamespaced to the `org.gwtproject.uibinder` package.  
This means that both the old AND new UiBinder modules can be imported and used without interference
between the two.  This can be beneficial to slowly migrate your application from the old to the new.

Just import the module in your `gwt.xml` file and you're set
```xml
  <!-- new UiBinder -->
  <inherits name="org.gwtproject.uibinder.UiBinder"/>
```

*See:* [Example project](https://github.com/Vertispan/gwt-uibinder/tree/master/gwt-uibinder-example) 
for further uses.  

## Changes in the new GWT UiBinder

Most of the functionality from UiBinder still exist, as it is a goal to become somewhat feature 
parity with the old module, but there are some minor differences.

### `@UiTemplate` is now required.

With the former GWT 2 generators, code generation can be triggered for interface/class subtypes, 
but with APT, annotations are required.   The setup is essentially identical to what is documented
on [gwtproject.org](http://www.gwtproject.org/doc/latest/DevGuideUiBinder.html).  **Be sure to use
classes from the new package!**

The `value` attribute for `@UiTemplate` is now optional in which the annotation processor will infer
the name of the `.ui.xml` file from the name of the class.  This is the same behavior as before, just
with the annotation present.

As this module is in transition between legacy GWT widgets and the new gwt-widgets module, the 
`@UiTemplate` interface also has a boolean attribute called `legacyWidgets`.    By default, this is 
set to false; needed to generate classes for the new widgets.  However, by setting 
`legacyWidgets = true`, the legacy widgets, SafeHtml, etc will be used.   Be aware that widgets cannot
be mixed. 

### GWT.create() no longer used

Previously, one would use `GWT.create(MyUiBinder.class)` to obtain the instance of the generated
class, but with the change to using APT, that is no longer the case.  The implementation is created
at compile time and with the naming convention `<owning_class>_<uibinderinterface>Impl`.   This is
subject to change.  
See [example usage](#example-usage) for a real-world example.

##Example Usage

```java
package com.example.myapplication.view;

import org.gwtproject.uibinder.client.UiBinder;
import org.gwtproject.uibinder.client.UiFactory;
import org.gwtproject.uibinder.client.UiHandler;
import org.gwtproject.uibinder.client.UiTemplate;

import com.google.gwt.user.client.ui.IsWidget;

public class MyView implements IsWidget {
  
  @UiTemplate
  interface MyUiBinder extends UiBinder<VerticalPanel, MyView> {}

  private MyUiBinder binder = new MyView_MyUiBinderImpl();

  private Widget widget;

  @Override
  public Widget asWidget() {
    if (widget == null) {
      widget = binder.createAndBindUi(this);
    }
    return widget;
  }
}
```

