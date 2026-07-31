/*
  Copyright (c) Lightstreamer Srl

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

package com.lightstreamer.stocklist_demo.adapters;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lightstreamer.adapter.common.InitException;
import com.lightstreamer.adapter.metadata.CreditsException;
import com.lightstreamer.adapter.metadata.ItemsException;
import com.lightstreamer.adapter.metadata.LoginInfo;
import com.lightstreamer.adapter.metadata.MetadataAdapter;
import com.lightstreamer.adapter.metadata.SessionListener;
import com.lightstreamer.adapter.metadata.SessionRequestHandler;
import com.lightstreamer.adapter.metadata.SubscriptionHandler;
import com.lightstreamer.adapter.metadata.SubscriptionInfo;
import com.lightstreamer.adapter.metadata.mpn.MPNActivationHandler;
import com.lightstreamer.adapter.metadata.mpn.MPNActivationInfo;
import com.lightstreamer.adapter.metadata.mpn.MPNDataException;
import com.lightstreamer.adapter.metadata.mpn.MPNDeviceInfo;
import com.lightstreamer.adapter.metadata.mpn.MPNPlatformType;
import com.lightstreamer.adapter.metadata.mpn.MPNSubscriptionInfo;

public class MPNStockQuotesMetadataAdapter implements MetadataAdapter {

    /**
     * Used as a Set, to keep unique identifiers for the currently connected
     * clients.
     * This information is used when logging MPN subscription activations to
     * uniquely identify by the client IP address and the HTTP user agent; 
     * in case of conflicts, a custom progressive is appended to
     * the user agent. This set lists the concatenations of the current
     * IP and user agent pairs, to help determining uniqueness.
     */
    private final ConcurrentHashMap<String,Object> uaIpPairs = new ConcurrentHashMap<String,Object>();
    
    /**
     * Private logger; a specific "LS_demos_Logger.StockQuotesMetadata" category
     * should be supplied by log4j configuration.
     */
    private Logger logger;
    
    
    /////////////////////////////////////////////////////////////////////////
    // Initialization

    public MPNStockQuotesMetadataAdapter() {}
    
    @Override
    public void init(Map<String, String> params, File configDir)
            throws InitException {
        
        /*
        String logConfig = (String) params.get("log_config");
        if (logConfig != null) {
            File logConfigFile = new File(configDir, logConfig);
            String logRefresh = (String) params.get("log_config_refresh_seconds");
            if (logRefresh != null) {
                DOMConfigurator.configureAndWatch(logConfigFile.getAbsolutePath(), Integer.parseInt(logRefresh) * 1000);
            } else {
                DOMConfigurator.configure(logConfigFile.getAbsolutePath());
            }
        }*/
        logger = LogManager.getLogger("LS_demos_Logger.StockQuotesMetadata");

        logger.info("StockQuotesMetadataAdapter ready");
    }
    
    @Override
    public void shutdown() {
    }

    /////////////////////////////////////////////////////////////////////////
    // Login and session management

    @Override
    public CompletionStage<SessionRequestHandler> loginAsync(LoginInfo loginInfo) {
        //////////////////////////////////////////////////////////////////////////
        // WARNING: This demo adapter performs no authentication.
        // In a production environment, validate credentials here.
        //////////////////////////////////////////////////////////////////////////
        Map<String, String> clientContext = new HashMap<>(loginInfo.getClientContext());

        return CompletableFuture.completedFuture(new SessionRequestHandler() {

            @Override
            public void onStart(SessionListener listener) {
                /* If needed, modify the user agent and store it directly
                 * in the session infos object, for logging support.
                 * Note: we are free to change and store the received object.
                 */
                uniquelyIdentifyClient(clientContext);
            }

            @Override
            public void onClose() {

                // We have to remove session information from the pairs "Set"
                String ua = clientContext.get("USER_AGENT");
                String IP =  clientContext.get("REMOTE_IP");

                uaIpPairs.remove(IP+" "+ua);
            }

            @Override
            public CompletionStage<SubscriptionHandler> newSubscription(SubscriptionInfo info) {
                return CompletableFuture.completedFuture(new SubscriptionHandler(info));
            }

            @Override
            public CompletionStage<String> userMessageAsync(String message) throws CreditsException {
                throw new CreditsException(0, "Unsupported feature");
            }

            @Override
            public CompletionStage<Void> notifyMpnDeviceAccess(MPNDeviceInfo info)
                    throws CreditsException, MPNDataException {
                // Authorize all devices
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public CompletionStage<MPNActivationHandler> newMPNActivation(MPNActivationInfo info)
                    throws CreditsException, ItemsException {

                // We suppose someone may try to communicate with the Server directly, through
                // the TLCP protocol, and force it to send unexpected or unwanted push notifications.
                // They could try to change both the items and the format of legit push notifications,
                // but could not change the app name, as it is checked by the Server during subscription.
                // Hence notifications may only be delivered to the legit app. They could not even change 
                // the trigger expression, as it is filtered by the Server using the regular expression
                // list specified in configuration files.
                
                // Check the item name, should be one of those supported by the StockList Data Adapter 
                List<String> itemNames = info.getItemRefs();
                if (itemNames == null || itemNames.isEmpty()) {
                    throw new ItemsException("Unexpected error on item names");
                }

                for (String itemName : itemNames) {
                    if (!itemName.startsWith("item")) {
                        throw new CreditsException(-102, "Invalid item argument for push notifications");
                    }
                }

                return CompletableFuture.completedFuture(
                    new StockMPNActivationHandler(info, clientContext));
            }

            @Override
            public CompletionStage<Void> notifyMpnDeviceTokenChange(MPNDeviceInfo info, String newToken)
                    throws CreditsException, MPNDataException {
                // Authorize all token changes
                return CompletableFuture.completedFuture(null);
            }

            /**
             * Modifies the clientContext to provide a unique identification
             * for the client session, for Chat support.
             */
            private void uniquelyIdentifyClient(Map<String,String> clientContext) {

                // Extract user agent and ip from session infos
                String ua = clientContext.get("USER_AGENT");
                String ip = clientContext.get("REMOTE_IP");

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

        });
    }

    private class StockMPNActivationHandler extends MPNActivationHandler {
        private final Map<String, String> clientContext;

        StockMPNActivationHandler(MPNActivationInfo info, Map<String, String> clientContext) {
            super(info);
            this.clientContext = clientContext;
        }

        @Override
        public CompletionStage<Void> notifyMpnSubscriptionActivation(MPNSubscriptionInfo mpnSubscription)
                throws CreditsException, MPNDataException {

            // Check the platform type
            MPNDeviceInfo device = mpnSubscription.getDevice();
            if (device.getType() == MPNPlatformType.APPLE) {

                // Here, we can add APNS-related checks, by inspecting the JSON string returned by:
                // mpnSubscription.getNotificationFormat()
                
                // Authorized, log it
                doLog("Authorized APNS subscription with parameters:\n" + 
                        "\titems: " + getActivationInfo().getItemRefs() + "\n" +
                        "\tfields: " + getActivationInfo().getFieldRefs() + "\n" +
                        ((mpnSubscription.getTrigger() != null) ? "\ttrigger expression: " + mpnSubscription.getTrigger() + "\n" : "") +
                        ((mpnSubscription.getNotificationFormat() != null) ? "\tformat: " + mpnSubscription.getNotificationFormat() + "\n" : ""));
                        // Here, we can add custom APNS log, by inspecting the JSON string returned by:
                        // mpnSubscription.getNotificationFormat()
            
            } else if (device.getType() == MPNPlatformType.GOOGLE) {

                // Here, we can add FCM-related checks, by inspecting the JSON string returned by:
                // mpnSubscription.getNotificationFormat()
                
                // Authorized, log it
                doLog("Authorized FCM subscription with parameters:\n" + 
                        "\titems: " + getActivationInfo().getItemRefs() + "\n" +
                        "\tfields: " + getActivationInfo().getFieldRefs() + "\n" +
                        ((mpnSubscription.getTrigger() != null) ? "\ttrigger expression: " + mpnSubscription.getTrigger() + "\n" : "") +
                        ((mpnSubscription.getNotificationFormat() != null) ? "\tformat: " + mpnSubscription.getNotificationFormat() + "\n" : ""));
                        // Here, we can add custom FCM log, by inspecting the JSON string returned by:
                        // mpnSubscription.getNotificationFormat()
                
            } else {
                throw new CreditsException(-103, "Invalid platform argument for push notifications");
            }
            return CompletableFuture.completedFuture(null);
        }

        private void doLog(String msg) {
            if (logger != null) {
                logger.info(msg + "\tfrom " + clientContext.get("REMOTE_IP"));
            }
        }
    }
}
