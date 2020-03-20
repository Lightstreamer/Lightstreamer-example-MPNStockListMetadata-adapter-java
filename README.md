# Lightstreamer - MPN Stock-List Demo Metadata - Java Adapter

<!-- START DESCRIPTION lightstreamer-example-mpnstocklistmetadata-adapter-java -->

This project includes the resources needed to develop a Metadata Adapter for the [Lightstreamer - MPN Stock-List Demo - iOS Client](https://github.com/Lightstreamer/Lightstreamer-example-MPNStockList-client-ios), the [Lightstreamer - MPN Stock-List Demo - Android Client](https://github.com/Lightstreamer/Lightstreamer-example-MPNStockList-client-android), and the [Lightstreamer - MPN Stock-List Demo - HTML Client](https://github.com/Lightstreamer/Lightstreamer-example-MPNStockList-client-javascript) that is pluggable into Lightstreamer Server.<br>
The Stock-List demos simulate a market data feed and front-end for stock quotes. They show a list of stock symbols and update prices and other fields displayed on the page in real-time. All clients support Mobile Push Notifications (MPN). The HTML Client in particular supports Web Push Notifications.<br>

## Details

The project is comprised of source code and a deployment example.

### Dig the Code

The MPN Stock-List Metadata Adapter is comprised of one Java class.

#### MPNStockQuotesMetadataAdapter

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

If you want to install a version of the *MPN Stock-List Demo* in your local Lightstreamer Server, follow these steps:

* Download *Lightstreamer Server Version 7.1 or greater* (Lightstreamer Server comes with a free non-expiring demo license for 20 connected users) from [Lightstreamer Download page](http://www.lightstreamer.com/download.htm), and install it, as explained in the `GETTING_STARTED.TXT` file in the installation home directory.
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

* Apple&trade;  APNs for iOS, macOS, tvOS, watchOS and Safari
* Google&trade; FCM for Android, Chrome and Firefox

Depending on the target operating system of your app, you may need to configure the `apple_notifier_conf.xml` file under `conf/mpn/apple`, `google_notifier_conf.xml` under `conf/mpn/google`, or both.

#### Configuring the APNs Provider

For the APNs provider, you need the following material in order to configure it correctly:

* the *app ID* of your app;
* a development or production *APNs client certificate*, exported in *p12* format and related to the app ID above;
* the *password* for the p12 client certificate above.

All this may be obtained on the [Apple Developer Center](https://developer.apple.com/membercenter/index.action). Exporting the client certificate in p12 format may be done easily from the *Keychain Access* system app. This [guide](https://code.google.com/p/javapns/wiki/GetAPNSCertificate) describes the full procedure in details.

If you are configuring the server also for the MPN Stock-List Demo HTML Client, which makes use of web push notifications, you also need the following:

* a *website push ID* in place of the *app ID*;
* a *push package zip file*.

Detailed instructions on how to prepare a push package zip file can be found in the *General Concepts* document (available under `DOCS-SDKs` in your Lightstreamer Server installation).

Once you have the required material, add the following segment to the `apple_notifier_conf.xml` file:

```xml
   <app id="your.app.id.or.website.push.id">
      <service_level>development</service_level>
      <keystore_file>your_client_certificate.p12</keystore_file>
      <keystore_password>your certificate password</keystore_password>
      
      <!-- Only for the HTML Client -->
      <push_package_file>your_push_package.zip</push_package_file> 

      <trigger_expressions>
         <accept>Double\.parseDouble\(\$\{[A-Za-z0-9_]+\}\) [&lt;&gt;] [+-]?(\d*\.)?\d+</accept>
         <accept>Double\.parseDouble\(\$\[\d+\]\) [&lt;&gt;] [+-]?(\d*\.)?\d+</accept>
      </trigger_expressions>
   </app>
```

Replace `your.app.id.or.website.push.id`, `your_client_certificate.p12`, `your certificate password` and `your_push_package.zip` with the corresponding information. The certificate and the push package files must be located in the same folder of `apple_notifier_conf.xml`, unless an absolute path is specified. The `<service_level>` tag must be set accordingly to your client certificate type: `development` (sandbox) or `production` (note that certificates for web push notifications can be only of `production` type).

For more information on the meaning of these tags please consult the `apple_notifier_conf.xml` itself or the *Mobile Push Notifications* section of the *General Concepts* document (available under `DOCS-SDKs` in your Lightstreamer Server installation).

#### Configuring the FCM Provider

For the FCM provider, you need the following material:

* the *package name* of your project;
* the *service JSON descriptor* of your project.

The *package name* is typically chosen by you when developing your project. For our MPN Stock-List Demo Android Client and HTML Client it is `com.lightstreamer.demo.android.fcm`.

The *service JSON descriptor* can obtained from the [Google Cloud Platform console](https://console.cloud.google.com/). Detailed instructions on how to obtain the descriptor can be found in the *General Concepts* document (available under `DOCS-SDKs` in your Lightstreamer Server installation).

Once you have the required material, add the following segment to the `google_notifier_conf.xml` file:

```xml
   <app packageName="your.package.name">
      <service_level>production</service_level>
      <service_json_file>your_service_descriptor.json</service_json_file>

      <trigger_expressions>
         <accept>Double\.parseDouble\(\$\[\d+\]\)\s?[&lt;&gt;]\s?=?[+-]?(\d*\.)?\d+</accept>
      </trigger_expressions>
   </app>
```

Replace `your.package.name` and `your_service_descriptor.json` with the corresponding information. The service JSON files must be located in the same folder of `google_notifier_conf.xml`, unless an absolute path is specified.

For more information on the meaning of these tags please consult the `google_notifier_conf.xml` itself or the *Mobile Push Notifications* section of the *General Concepts* document (available under `DOCS-SDKs` in your Lightstreamer Server installation).

### Configuring the MPN Database

The MPN module requires a working SQL database in order to store persistent data regarding devices and subscriptions. The database configuration must be specified in the `hibernate.cfg.xml` file under `conf/mpn` in your Lightstreamer Server installation.

If you don't have a working database instance, an HSQL test database may be installed and configured quickly following these steps:

* Download the latest stable release of HSQL from [hsqldb.org](http://hsqldb.org) and unzip it in a folder of your choice.
* Copy the `hsqldb.jar` file from the `lib` folder of your HSQL installation to the `lib/mpn/hibernate` folder of your Lightstreamer Server installation.
* Launch the HSQL instance by running the `runServer.sh` or `runServer.bat` script in the `bin` folder of your HSQL installation.
* Open the `hibernate.cfg.xml` file and locate the pre-enabled section indicated by *Sample database connection settings for HSQL*.
* If your HSQL instance is running on a separate machine than the Lightstreamer Server, specify its IP address in place of `localhost` in the following property: `<property name="connection.url">jdbc:hsqldb:hsql://localhost</property>`.

If you have a working database instance, follow these steps:

* Copy the JDBC driver jar file (or files) to the `lib/mpn/hibernate` folder of your Lightstreamer Server installation.
* Open the `hibernate.cfg.xml` file.
* Specify the appropriate connection properties and SQL dialect (samples are provided for MySQL, HSQL and Oracle), including the IP address.

A complete guide on configuring the Hibernate JDBC connection may be found [here](https://docs.jboss.org/hibernate/orm/5.0/manual/en-US/html/ch03.html#configuration-hibernatejdbc) (and [here](https://docs.jboss.org/hibernate/orm/5.0/manual/en-US/html/ch03.html#configuration-optional-dialects) is a list of available SQL dialects). Avoid introducing optional parameters, like those from tables 3.3 - 3.7, if they are not already present in the `hibernate.cfg.xml` file, as they may have not been tested and may lead to unexpected behavior. Do it only if you know what you are doing.

### Compiling and Building the Clients

You may download the source code for the MPN Stock-List Demo iOS Client, Android Client and HTML Client here:

* [Lightstreamer - MPN Stock-List Demo - iOS Client](https://github.com/Lightstreamer/Lightstreamer-example-MPNStockList-client-ios)
* [Lightstreamer - MPN Stock-List Demo - Android Client](https://github.com/Lightstreamer/Lightstreamer-example-MPNStockList-client-android)
* [Lightstreamer - MPN Stock-List Demo - HTML Client](https://github.com/Lightstreamer/Lightstreamer-example-MPNStockList-client-javascript)

Each project must be modified in order to work with your configuration and to point to your Lightstreamer Server.

#### The iOS Client

* Your app ID must be set as the *Bundle Identifier* of the project (in Xcode, it may be found in the General tab of the project);
* the IP address of your Lightstreamer Server must be set in the `PUSH_SERVER_URL` constant in the `Shared/Constants.h` file.

Also, remember to install an appropriate *provisioning profile* for the app, enabled for push notifications, before building or running the code.

#### The Android Client

* Open the `res/values/strings.xml` file;
* set the IP address of your Lightstreamer Server in the `host` property.

Note that to receive push notifications you need a Google account configured on the system. In case the emulator is used, a "Google APIs" OS image is needed.

#### The HTML Client

The MPN Stock-List Demo HTML Client is a push notifications-enabled web app compatible with two very different platforms (Safari and Firebase), and as such the parameters needed to fully configure it are numerous. Moreover, the JavaScript files that it is composed of not always can share information.

With this premise, follow these steps:

* open the `js/const.js` file of the specific demo you are configuring (basic or standard);
  * for the *Apple (Safari)* web push configuration:
      * set the `APPLE_WEBSITE_URL`, `APPLE_WEB_SERVICE_URL` values to the IP address of your Lightstreamer Server (leave the path intanct in the `APPLE_WEB_SERVICE_URL`);
      * set the `APPLE_WEBSITE_PUSH_ID` to your website push ID;
  * for the *Firebase (Chrome/Firefox)* web push configuration:
      * set the `GOOGLE_API_KEY`, `GOOGLE_PROJECT_ID`, `GOOGLE_APP_ID`, `GOOGLE_SENDER_ID` and `GOOGLE_VAPID_KEY` according to your project;
* open the `firebase-messaging-sw.js` file;
  * set the `SENDER_ID` value according to your project;
  * set the `PAGE_URL` value to the URL of your demo installation (a click on a web push notification will open this URL);
* open the `js/lsClient.js` file of the specific demo you are configuring (basic or standard);
  * search this line and set the IP address of your Lightstreamer Server:
      * `var lsClient= new LightstreamerClient(protocolToUse+"//localhost:"+portToUse,"DEMO");`
* get the `lightstreamer.min.js` file from [npm](https://www.npmjs.com/package/lightstreamer-client-web) or [unpkg](https://unpkg.com/lightstreamer-client-web/lightstreamer.min.js) and put it in the `src/[demo_name]/js` folder of the demo (if that is the case, please create it);
* get the `require.js` file form [requirejs.org](http://requirejs.org/docs/download.html) and put it in the `src/[demo_name]/js` folder of the demo.

You can deploy these demos to use the Lightstreamer server as web server or in any external web server you are running. If you choose the former case, please create the folders `<LS_HOME>/pages/[demo_name]` then copy here the contents of the `src` and `src/[demo_name]` folders of this project.

For further information on how to configure your web app for web push notifications, see the *General Concepts* document (available under `DOCS-SDKs` in your Lightstreamer Server installation) and the original guides by Apple and Google:

* Apple (Safari): [Configuring Safari Push Notifications](https://developer.apple.com/library/archive/documentation/NetworkingInternet/Conceptual/NotificationProgrammingGuideForWebsites/PushNotifications/PushNotifications.html#//apple_ref/doc/uid/TP40013225-CH3-SW1)
* Google FCM (Chrome/Firefox): [Set up a JavaScript Firebase Cloud Messaging client app](https://firebase.google.com/docs/cloud-messaging/js/client)

### Finishing Installation

Done all this, the installation is finished and ready to be tested:

* Launch the Lightstreamer Server.
* Launch the iOS Client, the Android Client or the HTML Client on your device, emulator or browser (remember that the iOS Simulator does not support push notifications).
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
* [Lightstreamer - MPN Stock-List Demo - HTML Client](https://github.com/Lightstreamer/Lightstreamer-example-MPNStockList-client-javascript)

<!-- END RELATED_ENTRIES -->

### Related Projects

* [Lightstreamer - Reusable Metadata Adapters - Java Adapter](https://github.com/Lightstreamer/Lightstreamer-example-ReusableMetadata-adapter-java)
* [Lightstreamer - Stock-List Demo - Java Adapter](https://github.com/Lightstreamer/Lightstreamer-example-StockList-adapter-java)

## Lightstreamer Compatibility Notes

- Compatible with Lightstreamer SDK for Java Adapters Since 7.0
- Configuration instructions compatible with Lightstreamer Server since version 7.1
- For an example compatible with Lightstreamer Server version 7.0.x, please refer to [this tag](https://github.com/Lightstreamer/Lightstreamer-example-MPNStockListMetadata-adapter-java/tree/for-server-7.0).
- For an example compatible with Lightstreamer SDK for Java Adapters version 6.x, please refer to [this tag](https://github.com/Lightstreamer/Lightstreamer-example-MPNStockListMetadata-adapter-java/tree/for-server-6).
