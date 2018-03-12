/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zigbee.internal.converter;

import java.util.Set;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.zigbee.ZigBeeBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zsmartsystems.zigbee.ZigBeeEndpoint;
import com.zsmartsystems.zigbee.zcl.ZclAttribute;
import com.zsmartsystems.zigbee.zcl.ZclAttributeListener;
import com.zsmartsystems.zigbee.zcl.ZclCommand;
import com.zsmartsystems.zigbee.zcl.ZclCommandListener;
import com.zsmartsystems.zigbee.zcl.clusters.ZclMultistateInputBasicCluster;
import com.zsmartsystems.zigbee.zcl.clusters.ZclMultistateOutputBasicCluster;
import com.zsmartsystems.zigbee.zcl.protocol.ZclClusterType;

/**
 * This channel supports changes through attribute updates, and also through received commands. This allows a switch
 * that is not connected to a load to send commands, or a switch that is connected to a load to send status (or both!).
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class ZigBeeConverterMultiStateInput extends ZigBeeBaseChannelConverter
        implements ZclAttributeListener, ZclCommandListener {
    private Logger logger = LoggerFactory.getLogger(ZigBeeConverterMultiStateInput.class);

    private ZclMultistateOutputBasicCluster clusterMultiStateClient;
    private ZclMultistateInputBasicCluster clusterMultiStateServer;

    @SuppressWarnings("unused")
    @Override
    public boolean initializeConverter() {
        if (endpoint.getParentNode().getNetworkAddress() == 60773) {
            logger.debug("{}: Initialising device multistate cluster", endpoint.getIeeeAddress());
        }
        clusterMultiStateClient = (ZclMultistateOutputBasicCluster) endpoint
                .getOutputCluster(ZclMultistateOutputBasicCluster.CLUSTER_ID);
        clusterMultiStateServer = (ZclMultistateInputBasicCluster) endpoint
                .getInputCluster(ZclMultistateInputBasicCluster.CLUSTER_ID);
        if (clusterMultiStateClient == null && clusterMultiStateServer == null) {
            logger.error("{}: Error opening device multistate controls", endpoint.getIeeeAddress());
            return false;
        }

        if (clusterMultiStateServer != null) {

            clusterMultiStateServer.bind();
            // Add a listener, then request the status
            clusterMultiStateServer.addAttributeListener(this);
            clusterMultiStateServer.addCommandListener(this);
            ZclAttribute attribute5 = clusterMultiStateServer
                    .getAttribute(ZclMultistateInputBasicCluster.ATTR_APPLICATIONTYPE);
            ZclAttribute attribute6 = clusterMultiStateServer
                    .getAttribute(ZclMultistateInputBasicCluster.ATTR_DESCRIPTION);
            ZclAttribute attribute7 = clusterMultiStateServer
                    .getAttribute(ZclMultistateInputBasicCluster.ATTR_NUMBEROFSTATES);
            ZclAttribute attribute8 = clusterMultiStateServer
                    .getAttribute(ZclMultistateInputBasicCluster.ATTR_PRESENTVALUE);
        }

        if (clusterMultiStateClient != null) {
            Set<ZclAttribute> attrs = clusterMultiStateClient.getAttributes();
            clusterMultiStateClient.bind();
            // Add a listener, then request the status
            clusterMultiStateClient.addAttributeListener(this);
            clusterMultiStateClient.addCommandListener(this);
            ZclAttribute attribute = clusterMultiStateClient
                    .getAttribute(ZclMultistateOutputBasicCluster.ATTR_APPLICATIONTYPE);
            ZclAttribute attribute2 = clusterMultiStateClient
                    .getAttribute(ZclMultistateOutputBasicCluster.ATTR_DESCRIPTION);
            ZclAttribute attribute3 = clusterMultiStateClient
                    .getAttribute(ZclMultistateOutputBasicCluster.ATTR_NUMBEROFSTATES);
            ZclAttribute attribute4 = clusterMultiStateClient
                    .getAttribute(ZclMultistateOutputBasicCluster.ATTR_PRESENTVALUE);
        }

        return true;
    }

    @Override
    public void disposeConverter() {
        logger.debug("{}: Closing device on/off cluster", endpoint.getIeeeAddress());

        if (clusterMultiStateClient != null) {
            clusterMultiStateClient.removeAttributeListener(this);
        }
        if (clusterMultiStateServer != null) {
            clusterMultiStateServer.removeAttributeListener(this);
        }
    }

    @Override
    public void handleRefresh() {
        if (clusterMultiStateClient != null) {
            // clusterOnOffClient.getOnOff(0);
        }
        if (clusterMultiStateServer != null) {
            // clusterOnOffServer.getOnOff(0);
        }
    }

    @Override
    public void handleCommand(final Command command) {
        OnOffType cmdOnOff = null;
        if (command instanceof PercentType) {
            if (((PercentType) command).intValue() == 0) {
                cmdOnOff = OnOffType.OFF;
            } else {
                cmdOnOff = OnOffType.ON;
            }
        } else if (command instanceof OnOffType) {
            cmdOnOff = (OnOffType) command;
        }

        if (cmdOnOff == OnOffType.ON) {
            // clusterOnOffServer.onCommand();
        } else {
            // clusterOnOffServer.offCommand();
        }
    }

    @Override
    public Channel getChannel(ThingUID thingUID, ZigBeeEndpoint endpoint) {
        if (endpoint.getInputCluster(ZclMultistateInputBasicCluster.CLUSTER_ID) == null
                && endpoint.getOutputCluster(ZclMultistateInputBasicCluster.CLUSTER_ID) == null) {
            return null;
        }
        return createChannel(thingUID, endpoint, ZigBeeBindingConstants.CHANNEL_MULTISTATE,
                ZigBeeBindingConstants.ITEM_TYPE_STRING, "State");
    }

    @Override
    public void attributeUpdated(ZclAttribute attribute) {
        logger.debug("{}: ZigBee attribute reports {}", endpoint.getIeeeAddress(), attribute);
        if (attribute.getCluster() == ZclClusterType.MULTISTATE_INPUT__BASIC
                && attribute.getId() == ZclMultistateInputBasicCluster.ATTR_STATETEXT) {
            String value = (String) attribute.getLastValue();
            // updateChannelState(value);
        }
    }

    @Override
    public void commandReceived(ZclCommand command) {
        logger.debug("{}: ZigBee command receiveds {}", endpoint.getIeeeAddress(), command);
        // if (command instanceof OnCommand) {
        // updateChannelState(OnOffType.ON);
        // }
        // if (command instanceof OffCommand) {
        // updateChannelState(OnOffType.OFF);
        // }
    }
}
