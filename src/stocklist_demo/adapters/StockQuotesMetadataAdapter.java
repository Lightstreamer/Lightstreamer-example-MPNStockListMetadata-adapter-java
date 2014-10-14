/*
  Copyright 2014 Weswit Srl

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package stocklist_demo.adapters;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.lightstreamer.adapters.metadata.LiteralBasedProvider;
import com.lightstreamer.interfaces.metadata.CreditsException;
import com.lightstreamer.interfaces.metadata.ItemsException;
import com.lightstreamer.interfaces.metadata.MetadataProviderException;
import com.lightstreamer.interfaces.metadata.MpnDeviceInfo;
import com.lightstreamer.interfaces.metadata.MpnPlatformType;
import com.lightstreamer.interfaces.metadata.MpnSubscriptionInfo;
import com.lightstreamer.interfaces.metadata.NotificationException;
import com.lightstreamer.interfaces.metadata.TableInfo;
import com.lightstreamer.interfaces.metadata.MpnSubscriptionInfo.MpnApnsSubscriptionInfo;
import com.lightstreamer.interfaces.metadata.MpnSubscriptionInfo.MpnGcmSubscriptionInfo;

public class StockQuotesMetadataAdapter extends LiteralBasedProvider {

    /**
     * Keeps the client context informations supplied by Lightstreamer
     * on the new session notifications.
     * Session information is used when logging MPN subscription activations.
     */
    private ConcurrentHashMap<String,Map<String,String>> sessions = new ConcurrentHashMap<String,Map<String,String>>();

    /**
     * Used as a Set, to keep unique identifiers for the currently connected
     * clients.
     * This information is used when logging MPN subscription activations to
     * uniquely identify by the client IP address and the HTTP user agent; 
     * in case of conflicts, a custom progressive is appended to
     * the user agent. This set lists the concatenations of the current
     * IP and user agent pairs, to help determining uniqueness.
     */
    private ConcurrentHashMap<String,Object> uaIpPairs = new ConcurrentHashMap<String,Object>();
    
    /**
     * Private logger; a specific "LS_demos_Logger.StockQuotesMetadata" category
     * should be supplied by log4j configuration.
     */
    private Logger logger;
    
    
    /////////////////////////////////////////////////////////////////////////
    // Initialization

    public StockQuotesMetadataAdapter() {}
    
    @SuppressWarnings("rawtypes") 
    public void init(Map params, File configDir)
            throws MetadataProviderException {
        
        // Call super's init method to handle basic Metadata Adapter features
        super.init(params, configDir);

        String logConfig = (String) params.get("log_config");
        if (logConfig != null) {
            File logConfigFile = new File(configDir, logConfig);
            String logRefresh = (String) params.get("log_config_refresh_seconds");
            if (logRefresh != null) {
                DOMConfigurator.configureAndWatch(logConfigFile.getAbsolutePath(), Integer.parseInt(logRefresh) * 1000);
            } else {
                DOMConfigurator.configure(logConfigFile.getAbsolutePath());
            }
        }
        logger = Logger.getLogger("LS_demos_Logger.StockQuotesMetadata");

        logger.info("StockQuotesMetadataAdapter ready");
    }
    

    /////////////////////////////////////////////////////////////////////////
    // Session management

    @SuppressWarnings({ "rawtypes", "unchecked" }) 
    public void notifyNewSession(String user, String session, Map clientContext)
        throws CreditsException, NotificationException {

        // We can't have duplicate sessions
        assert(!sessions.containsKey(session));

        /* If needed, modify the user agent and store it directly
         * in the session infos object, for logging support.
         * Note: we are free to change and store the received object.
         */
        uniquelyIdentifyClient(clientContext);

        // Register the session details on the sessions HashMap,
        // for Chat and Monitor support.
        sessions.put(session, clientContext);
    }

    public void notifySessionClose(String session) throws NotificationException {

        // The session must exist to be closed
        assert(sessions.containsKey(session));

        // We have to remove session informations from the session HashMap
        // and from the pairs "Set"
        Map<String,String> sessionInfo = sessions.get(session);
        String ua = sessionInfo.get("USER_AGENT");
        String IP =  sessionInfo.get("REMOTE_IP");

        uaIpPairs.remove(IP+" "+ua);
        sessions.remove(session);
    }
    
    
    /////////////////////////////////////////////////////////////////////////
    // Mobile push notifications management
    
    public void notifyMpnDeviceAccess(String user, MpnDeviceInfo device)
            throws CreditsException, NotificationException {
        // Authorize all devices
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" }) 
    public void notifyMpnSubscriptionActivation(String user, String sessionID, TableInfo table, MpnSubscriptionInfo mpnSubscription)
            throws CreditsException, NotificationException {

        // We suppose someone may try to communite with the Server directly, through
        // the text protocol, and force it to send unexpected or unwanted push notifications.
        // He could try to change both the items and the format of legit push notifications,
        // but could not change the app name, as it is checked by the Server during activation.
        // Hence notifications may only be delivered to the legit app. He could not even change 
        // the trigger expression, as it is filtered by the Server using the regular expression
        // list specified in configuration files.
        
        // Check the item name, should be one of those supported by the StockList Data Adapter 
        if (table.getId() == null) {
            throw new NotificationException("Unexpected error on item names");
        }
        
        String[] itemNames;
        try {
            itemNames= getItems(user, sessionID, table.getId());

        } catch (ItemsException e) {
            throw new NotificationException("Unexpected error on item names");
        }

        for (String itemName : itemNames) {
            if (!itemName.startsWith("item")) {
                throw new CreditsException(-102, "Invalid item argument for push notifications");
            }
        }
        
        // Check the platform type
        if (mpnSubscription.getDevice().getType().getName().equals(MpnPlatformType.APNS.getName())) {
            MpnApnsSubscriptionInfo apnsSubscription= (MpnApnsSubscriptionInfo) mpnSubscription;
            
            // Check the format, should be one of those supported by the iOS StockList Demo app
            if (apnsSubscription.getFormat() == null) {
                throw new CreditsException(-104, "Invalid format argument for push notifications");
            }
            
            if ((!apnsSubscription.getFormat().equals("Stock ${stock_name} is now ${last_price}")) &&
                (!apnsSubscription.getFormat().equals("Stock ${stock_name} rised above ${last_price}")) &&
                (!apnsSubscription.getFormat().equals("Stock ${stock_name} dropped below ${last_price}"))) {
                throw new CreditsException(-105, "Invalid format argument for push notifications");
            }
            
            // Check the badge and sound format
            if (!apnsSubscription.getBadge().equals("AUTO")) {
                throw new CreditsException(-106, "Invalid badge argument for push notifications");
            }
            
            if (!apnsSubscription.getSound().equals("Default")) {
                throw new CreditsException(-107, "Invalid sound argument for push notifications");
            }
            
            // Authorized, log it
            doLog(logger, sessionID, "Authorized APNS subscription with parameters:\n" + 
                    ((table.getId() != null) ? "\tgroup: " + table.getId() + "\n" : "") +
                    ((itemNames != null) && (itemNames.length > 0) ? "\titems: " + itemNames + "\n" : "") +
                    ((table.getSchema() != null) ? "\tschema: " + table.getSchema() + "\n" : "") +
                    ((apnsSubscription.getTrigger() != null) ? "\ttrigger expression: " + apnsSubscription.getTrigger() + "\n" : "") +
                    ((apnsSubscription.getSound() != null) ? "\tsound: " + apnsSubscription.getSound() + "\n" : "") +
                    ((apnsSubscription.getBadge() != null) ? "\tbadge: " + apnsSubscription.getBadge() + "\n" : "") +
                    ((apnsSubscription.getContentAvailable() != null) ? "\tcontent available: " + apnsSubscription.getContentAvailable() + "\n" : "") +
                    ((apnsSubscription.getLocalizedActionKey() != null) ? "\tloc. action key: " + apnsSubscription.getLocalizedActionKey() + "\n" : "") +
                    ((apnsSubscription.getCategory() != null) ? "\tcategory: " + apnsSubscription.getCategory() + "\n" : "") +
                    ((apnsSubscription.getLaunchImage() != null) ? "\tlaunch image: " + apnsSubscription.getLaunchImage() + "\n" : "") +
                    ((apnsSubscription.getFormat() != null) ? "\tformat: " + apnsSubscription.getFormat() + "\n" : "") +
                    ((apnsSubscription.getLocalizedFormatKey() != null) ? "\tloc. format key: " + apnsSubscription.getLocalizedFormatKey() + "\n" : "") +
                    ((apnsSubscription.getArguments() != null) && (apnsSubscription.getArguments().size() > 0) ? "\targuments: " + apnsSubscription.getArguments() + "\n" : "") +
                    ((apnsSubscription.getCustomData() != null) && (apnsSubscription.getCustomData().size() > 0) ? "\tcustom data: " + apnsSubscription.getCustomData() + "\n" : ""));
        
        } else if (mpnSubscription.getDevice().getType().getName().equals(MpnPlatformType.GCM.getName())) {
            MpnGcmSubscriptionInfo gcmSubscription= (MpnGcmSubscriptionInfo) mpnSubscription;
            
            // Check the collapse key
            if (!gcmSubscription.getCollapseKey().equals("Stock update")) {
                throw new CreditsException(-105, "Invalid collapse key for push notifications");
            }
            
            // Check the notification data, should be one of those supported by the Android StockList Demo app
            Map data = gcmSubscription.getData();
            for (Iterator<String> i = data.keySet().iterator(); i.hasNext();) {
                String key = (String) i.next();
                String val = (String) data.get(key);
                if (!(key.equals("stock_name") && val.equals("${stock_name}")) &&
                    !(key.equals("last_price") && val.equals("${last_price}")) &&
                    !(key.equals("time") && val.equals("${time}")) && 
                    !(key.equals("item") && val.matches("item\\d+"))) {
                    
                    throw new CreditsException(-105, "Invalid data argument for push notifications");
                }
            }
            
            // Authorized, log it
            doLog(logger, sessionID, "Authorized GCM subscription with parameters:\n" + 
                    ((table.getId() != null) ? "\tgroup: " + table.getId() + "\n" : "") +
                    ((itemNames != null) && (itemNames.length > 0) ? "\titems: " + itemNames + "\n" : "") +
                    ((table.getSchema() != null) ? "\tschema: " + table.getSchema() + "\n" : "") +
                    ((gcmSubscription.getTrigger() != null) ? "\ttrigger expression: " + gcmSubscription.getTrigger() + "\n" : "") +
                    ((gcmSubscription.getCollapseKey() != null) ? "\tcollapse key: " + gcmSubscription.getCollapseKey() + "\n" : "") +
                    ((gcmSubscription.getData() != null) && (gcmSubscription.getData().size() > 0) ? "\tdata: " + gcmSubscription.getData() + "\n" : "") +
                    ((gcmSubscription.getDelayWhileIdle() != null) ? "\tdelay while idle: " + gcmSubscription.getDelayWhileIdle() + "\n" : "") +
                    ((gcmSubscription.getTimeToLive() != null) ? "\ttime to live: " + gcmSubscription.getTimeToLive() + "\n" : ""));
            
        } else {
            throw new CreditsException(-103, "Invalid platform argument for push notifications");
        }
    }
    
    public void notifyMpnDeviceTokenChange(String user, MpnDeviceInfo device, String newDeviceToken)
            throws CreditsException, NotificationException {
        // Authorize all token changes
    }
    
    
    /////////////////////////////////////////////////////////////////////////
    // Internals

    /**
     * Modifies the clientContext to provide a unique identification
     * for the client session, for Chat support.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" }) 
    private void uniquelyIdentifyClient(Map clientContext) {

        // Extract user agent and ip from session infos
        String ua = (String) clientContext.get("USER_AGENT");
        String ip = (String) clientContext.get("REMOTE_IP");

        /*
         * we need to ensure that each pair IP-User Agent is unique so that
         * the sender can be identified on the client, if such pair is not
         * unique we add a counter on the user agent string
         */

        String count = "";
        int c = 0;
        
        // Synchronize to ensure that we do not generate two identical pairs
        synchronized(uaIpPairs) {
            while (uaIpPairs.containsKey(ip+" "+ua+" "+count)) {
                c++;
                count = "["+c+"]";
            }

            ua = ua+" "+count;

            uaIpPairs.put(ip+" "+ua,new Object());
        }

        clientContext.put("USER_AGENT", ua);
    }
    
    private void doLog(Logger logger, String session, String msg) {
        if (logger != null) {
            Map<String,String> sessionInfo = sessions.get(session);
            String IP =  sessionInfo.get("REMOTE_IP");
            logger.info(msg + "\tfrom " + IP);
        }
    }
}
