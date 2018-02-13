# Lightstreamer - MPN Stock-List Demo Metadata - Java Adapter

<!-- START DESCRIPTION lightstreamer-example-mpnstocklistmetadata-adapter-java -->

This project includes the resources needed to develop a Metadata Adapter for the [Lightstreamer - MPN Stock-List Demo - iOS Client](https://github.com/Lightstreamer/Lightstreamer-example-MPNStockList-client-ios#lightstreamer---mpn-stock-list-demo---ios-client) and the [Lightstreamer - MPN Stock-List Demo - Android Client](https://github.com/Lightstreamer/Lightstreamer-example-MPNStockList-client-android#lightstreamer---mpn-stock-list-demo---android-client) that is pluggable into Lightstreamer Server.<br>
The Stock-List demos simulate a market data feed and front-end for stock quotes. They show a list of stock symbols and updates prices and other fields displayed on the page in real-time. Both clients support Mobile Push Notifications (MPN).<br>

## Details

The project is comprised of source code and a deployment example.

### Dig the Code

The MPN Stock-List Metadata Adapter is comprised of one Java class.

#### StockQuotesMetadataAdapter

Implements the MetadataProvider interface to handle the communication with Lightstreamer Kernel, and extends the [LiteralBasedProvider](https://github.com/Lightstreamer/Lightstreamer-example-ReusableMetadata-adapter-java). The code shows a few best practices to ensure safety of Mobile Push Notifications (MPN) activity.

See the source code comments for further details.

<!-- END DESCRIPTION lightstreamer-example-mpnstocklistmetadata-adapter-java -->

### The Adapter Set Configuration

The Data Adapter functionalities are absolved by the `StockQuotesDataAdapter`, a full implementation of a Data Adapter, explained in [Lightstreamer - Stock-List Demo - Java Adapter](https://github.com/Lightstreamer/Lightstreamer-example-StockList-adapter-java).

This Adapter Set is configured and will be referenced by the clients as `DEMO`.

The `adapters.xml` file for the Stock-List Demo, should look like:

```xml
<?xml version="1.0"?>

<!-- Mandatory. Define an Adapter Set and sets its unique ID. -->
<adapters_conf id="DEMO">

    <!--
      Not all configuration options of an Adapter Set are exposed by this file.
      You can easily expand your configurations using the generic template,
      `DOCS-SDKs/sdk_adapter_java_inprocess/doc/adapter_conf_template/adapters.xml`,
      as a reference.
    -->

    <metadata_adapter_initialised_first>Y</metadata_adapter_initialised_first>

    <!-- Mandatory. Define the Metadata Adapter. -->
    <metadata_provider>

        <!-- Mandatory. Java class name of the adapter. -->
        <adapter_class>stocklist_demo.adapters.metadata.MPNStockQuotesMetadataAdapter</adapter_class>

        <!-- Optional.
             See LiteralBasedProvider javadoc. -->
        <!--
        <param name="max_bandwidth">40</param>
        <param name="max_frequency">3</param>
        <param name="buffer_size">30</param>
        <param name="distinct_snapshot_length">10</param>
        <param name="prefilter_frequency">5</param>
        <param name="allowed_users">user123,user456</param>
        -->

        <!-- Optional.
             See LiteralBasedProvider javadoc. -->
        <param name="item_family_1">item.*</param>
        <param name="modes_for_item_family_1">MERGE</param>

        <!-- Optional for MPNStockQuotesMetadataAdapter.
             Configuration file for the Adapter's own logging.
             Logging is managed through log4j. -->
        <param name="log_config">adapters_log_conf.xml</param>
        <param name="log_config_refresh_seconds">10</param>

    </metadata_provider>

    <!-- Mandatory. Define the Data Adapter. -->
    <data_provider name="STOCKLIST_ADAPTER">

        <!-- Mandatory. Java class name of the adapter. -->
        <adapter_class>stocklist_demo.adapters.StockQuotesDataAdapter</adapter_class>

    </data_provider>

</adapters_conf>
```

<i>NOTE: not all configuration options of an Adapter Set are exposed by the file suggested above.
You can easily expand your configurations using the generic template, `DOCS-SDKs/sdk_adapter_java_inprocess/doc/adapter_conf_template/adapters.xml`, as a reference.</i><br>
<br>
Please refer to the *General Concepts* document (available under `DOCS-SDKs` in your Lightstreamer Server installation) for more details about Lightstreamer Adapters.<br>

## Install

If you want to install a version of the *Stock-List Demo* in your local Lightstreamer Server, follow these steps:

* Download *Lightstreamer Server Version 7.0 or greater* (Lightstreamer Server comes with a free non-expiring demo license for 20 connected users) from [Lightstreamer Download page](http://www.lightstreamer.com/download.htm), and install it, as explained in the `GETTING_STARTED.TXT` file in the installation home directory.
* Make sure that Lightstreamer Server is not running.

### Installing the Adapter

* Get the `deploy.zip` file of the [Metadata Adapter latest release](https://github.com/Lightstreamer/Lightstreamer-example-MPNStockListMetadata-adapter-java/releases), unzip it, and copy the `StockList` folder into the `adapters` folder of your Lightstreamer Server installation.
* Get the `deploy.zip` file of the [Data Adapter latest release](https://github.com/Lightstreamer/Lightstreamer-example-StockList-adapter-java/releases), unzip it, and copy the content of `StockList/lib` folder into the `adapters/StockList/lib` folder of your Lightstreamer Server installation.

### Enabling the MPN Module

The Mobile Push Notifications (MPN) module of Lightstreamer is not enabled by default and requires configuration.

* Open the `lightstreamer_conf.xml` file under the `conf` directory of your Lightstreamer Server installation.
* Find the `<mpn>` tag.
* Set the `<enabled>` tag to `Y`.

### Configuring the MPN Service Provider

The MPN module currently supports two MPN providers:

* Apple's APNs for iOS
* Google's FCM for Android

Depending on the target operating system of your app, you may need to configure the `apple_notifier_conf.xml` file under `conf/mpn/apple`, `google_notifier_conf.xml` under `conf/mpn/google`, or both.

#### Configuring the APNs Provider

For the APNs provider, you need the following material in order to configure it correctly:

* the *app ID* of your app;
* a development or production *APNs client certificate*, exported in *p12* format and related to the app ID above;
* the *password* for the p12 client certificate above.

All this may be obtained on the [Apple Developer Center](https://developer.apple.com/membercenter/index.action). Exporting the client certificate in p12 format may be done easily from the *Keychain Access* system app. This [guide](https://code.google.com/p/javapns/wiki/GetAPNSCertificate) describes the full procedure in details.

Once you have the required material, add the following segment to the `apple_notifier_conf.xml` file:

```xml
   <app id="your.app.id">
      <service_level>development</service_level>
      <keystore_file>your_client_certificate.p12</keystore_file>
      <keystore_password>your certificate password</keystore_password>

      <trigger_expressions>
         <accept>Double\.parseDouble\(\$\{[A-Za-z0-9_]+\}\) [&lt;&gt;] [+-]?(\d*\.)?\d+</accept>
         <accept>Double\.parseDouble\(\$\[\d+\]\) [&lt;&gt;] [+-]?(\d*\.)?\d+</accept>
      </trigger_expressions>
   </app>
```

Replace `your.app.id`, `your_client_certificate.p12` and `your certificate password` with the corresponding informations. The certificate file must be located in the same folder of `apple_notifier_conf.xml`, unless an absolute path is specified. The `<service_level>` tag must be set accordingly to your client certificate type: `development` (sandbox) or `production`. For more informations on the meaning of these tags please consult the `apple_notifier_conf.xml` itself or the *Mobile Push Notifications* section of the *General Concepts* document (available under `DOCS-SDKs` in your Lightstreamer Server installation).

#### Configuring the FCM Provider

For the FCM provider, you need the following material:

* the *FCM sender ID* of your project;
* the *API key* of your project.

All this may be both obtained from the [Google Developers Console](https://console.developers.google.com/project). Follow [this guide](https://firebase.google.com/docs/cloud-messaging/server) for informations on how to configure your project appropriately to use FCM services.

Once you have the required material, add the following segment to the `google_notifier_conf.xml` file:

```xml
   <app packageName="com.lightstreamer.demo.android.fcm">
      <service_level>production</service_level>
      <api_key>your-API-key</api_key>

      <trigger_expressions>
         <accept>Double\.parseDouble\(\$\[\d\]+\)[&lt;&gt;]=[+-]?(\d*\.)?\d+</accept>
      </trigger_expressions>
   </app>
```

Replace `your-API-key` with the corresponding information. You will need the GMC sender ID later. For more informations on the meaning of these tags please consult the `google_notifier_conf.xml` itself or the *Mobile Push Notifications* section of the *General Concepts* document (available under `DOCS-SDKs` in your Lightstreamer Server installation).

### Configuring the MPN Database

The MPN module requires a working SQL database in order to store persistent data regarding devices and subscriptions. The database configuration must be specified in the `hibernate.cfg.xml` file under `conf/mpn` in your Lightstreamer Server installation.

If you don't have a working database instance, an HSQL test database may be installed and configured quickly following these steps:

* Download the latest stable release of HSQL from [hsqldb.org](http://hsqldb.org) and unzip it in a folder of your choice.
* Copy the `hsqldb.jar` file from the `lib` folder of your HSQL installation to the `lib/mpn/hibernate` folder of your Lightstreamer Server installation.
* Launch the HSQL instance by running the `runServer.sh` or `runServer.bat` script in the `bin` folder of your HSQL installation.
* Open the `hibernate.cfg.xml` file and locate the pre-enabled section indicated by *Sample database connection settings for HSQL (not for production)*.
* If your HSQL instance is running on a separate machine than the Lightstreamer Server, specify its IP address in place of `localhost` in the following property: `<property name="connection.url">jdbc:hsqldb:hsql://localhost</property>`.

If you have a working database instance, follow these steps:

* Copy the JDBC driver jar file (or files) to the `lib/mpn/hibernate` folder of your Lightstreamer Server installation.
* Open the `hibernate.cfg.xml` file.
* Specify the appropriate connection properties and SQL dialect (samples are provided for MySQL, HSQL and Oracle), including the IP address.

A complete guide on configuring the Hibernate JDBC connection may be found [here](https://docs.jboss.org/hibernate/orm/5.0/manual/en-US/html/ch03.html#configuration-hibernatejdbc) (and [here](https://docs.jboss.org/hibernate/orm/5.0/manual/en-US/html/ch03.html#configuration-optional-dialects) is a list of available SQL dialects). Avoid introducing optional parameters, like those from tables 3.3 - 3.7, if they are not already present in the `hibernate.cfg.xml` file, as they may have not been tested and may lead to unexpected behavior. Do it only if you know what you are doing.

### Compiling and Building the Clients

You may download the source code for the MPN Stock-List Demo iOS Client and MPN Stock-List Demo Android Client here:

* [Lightstreamer - MPN Stock-List Demo - iOS Client](https://github.com/Lightstreamer/Lightstreamer-example-MPNStockList-client-ios)
* [Lightstreamer - MPN Stock-List Demo - Android Client](https://github.com/Lightstreamer/Lightstreamer-example-MPNStockList-client-android)

Each project must be modified in order to work with your app ID and certificate (for APNs) or your sender ID (for FCM), and to point to your Lightstreamer Server.

For the iOS Client:

* your app ID must be set as the *Bundle Identifier* of the project (in Xcode, it may be found in the General tab of the project);
* the IP address of your Lightstreamer Server must be set in the `PUSH_SERVER_URL` constant in the `Shared/Constants.h` file.

Also, remember to install an appropriate *provisioning profile* for the app, enabled for push notifications, before building or running the code.

For the Android Client:

* open the `res/values/strings.xml` file;
* set the FCM sender ID in the  in the `sender_id` property;
* set the IP address of your Lightstreamer Server in the `host` property.

Note that to receive push notifications you need a Google account configured on the system. In case the emulator is used a "Google APIs"
OS image has to be used.

### Finishing Installation

Done all this, the installation is finished and ready to be tested:

* Launch the Lightstreamer Server.
* Launch the iOS Client or the Android Client on your device or emulator (remember the iOS Simulator does not support push notifications).
* Wait for the connection to be established (the real-time stock-list data will start to blink).
* Set up a push notification with the UI provided.

If everything is correct, you should receive the push notification within a few seconds. In case of any problem, first double check all the steps above, then check for any errors reported on the Lightstreamer Server log.

## Build

To build your own version of `LS_MPN_StockListDemo_MetadataAdapter.jar`, instead of using the one provided in the `deploy.zip` file from the [Install](https://github.com/Lightstreamer/Lightstreamer-example-MPNStocklistMetadata-adapter-java#install) section above, follow these steps:

* Download this project.
* Get the `ls-adapter-interface.jar` file from the `/lib` folder of a [Lightstreamer distribution 7.0 or greater](http://www.lightstreamer.com/download), and copy it into the `lib` folder.
* Get the `log4j-1.2.17.jar` file from [Apache log4j](https://logging.apache.org/log4j/1.2/) and copy it into the `lib` folder.
* Create the jar `LS_MPN_StockListDemo_MetadataAdapter.jar` with commands like these:

```sh
 >javac -source 1.8 -target 1.8 -nowarn -g -classpath lib/log4j-1.2.17.jar;lib/ls-adapter-interface.jar -sourcepath src -d tmp_classes src/stocklist_demo/adapters/MPNStockQuotesMetadataAdapter.java

 >jar cvf LS_MPN_StockListDemo_MetadataAdapter.jar -C tmp_classes src
```

* Copy the just compiled `LS_MPN_StockListDemo_MetadataAdapter.jar` in the `adapters/Stocklist/lib` folder of your Lightstreamer Server installation.

## See Also

### Clients Using This Adapter

<!-- START RELATED_ENTRIES -->

* [Lightstreamer - MPN Stock-List Demo - iOS Client](https://github.com/Lightstreamer/Lightstreamer-example-MPNStockList-client-ios)
* [Lightstreamer - MPN Stock-List Demo - Android Client](https://github.com/Lightstreamer/Lightstreamer-example-MPNStockList-client-android)

<!-- END RELATED_ENTRIES -->

### Related Projects

* [Lightstreamer - Reusable Metadata Adapters - Java Adapter](https://github.com/Lightstreamer/Lightstreamer-example-ReusableMetadata-adapter-java)
* [Lightstreamer - Stock-List Demo - Java Adapter](https://github.com/Lightstreamer/Lightstreamer-example-StockList-adapter-java)

## Lightstreamer Compatibility Notes

- Compatible with Lightstreamer SDK for Java Adapters Since 7.0
- For an example compatible with Lightstreamer SDK for Java Adapters version 6.x, please refer to [this tag](https://github.com/Lightstreamer/Lightstreamer-example-MPNStockListMetadata-adapter-java/tree/for-server-6).
