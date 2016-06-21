# IBM iSeries plugin #

This document describes the functionality provided by the IBM iSeries plugin.

See the **Deployit Reference Manual** for background information on Deployit and deployment concepts.

# Overview #

The iSeries plugin is a Deployit plugin that is used to execute commands on a remote iSeries system.

##Features##

# Requirements #

* **Deployit requirements**
	* **Deployit**: version 3.9+

# Installation

Place the plugin JAR file into your `SERVER_HOME/plugins` directory.
Place the [jt400 JAR file](http://jt400.sourceforge.net/) into your `SERVER_HOME/plugins` directory. Pick up the version depending of your needs and the target system.


# Usage #

The plugin copies the behavior of the [generic.ExecutedScript](http://docs.xebialabs.com/releases/latest/deployit/genericPluginManual.html#executed-script) deployed provided by the [generic-plugin](http://docs.xebialabs.com/releases/latest/deployit/genericPluginManual.html) but it targets only an `iseries.Server`.
The scripts are using the freemarker template language to be generated: each lines will be executed one by one.

# Example #

This is an example how to execute a command. In your synthetic.xml file add the following definition.

```
<type type="mysoftware.ExecutedPackage" extends="iseries.ExecutedScript"
		deployable-type="mysoftware.Package" container-type="iseries.Server">
		<generate-deployable type="mysoftware.Package" extends="generic.Resource"/>
		<property name="createScript" default="mysoftware/create.ftl" hidden="true" />
		<property name="category"/>
		<property name="code"/>
		<property name="targetApplication"/>
		<property name="version"/>
</type>
```

with the content of mysoftware/create.ftl, relative to the location of the synthetic.xml file.

```
ADDLIBLE mysoftware_fra

ZEN_INST TYPE(${deployed.category}) VERSION('${deployed.version}') ENV(${deployed.category}) APPS(${deployed.targetApplication})
```
