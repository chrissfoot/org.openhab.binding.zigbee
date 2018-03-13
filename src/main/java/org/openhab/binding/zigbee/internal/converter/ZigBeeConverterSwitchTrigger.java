/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zigbee.internal.converter;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.zigbee.ZigBeeBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zsmartsystems.zigbee.ZigBeeEndpoint;
import com.zsmartsystems.zigbee.zcl.ZclAttribute;
import com.zsmartsystems.zigbee.zcl.ZclAttributeListener;
import com.zsmartsystems.zigbee.zcl.ZclCommand;
import com.zsmartsystems.zigbee.zcl.ZclCommandListener;
import com.zsmartsystems.zigbee.zcl.clusters.ZclOnOffCluster;
import com.zsmartsystems.zigbee.zcl.clusters.onoff.OffCommand;
import com.zsmartsystems.zigbee.zcl.clusters.onoff.OnCommand;
import com.zsmartsystems.zigbee.zcl.protocol.ZclClusterType;

/**
 * This channel supports changes through attribute updates, and also through received commands. This allows a switch
 * that is not connected to a load to send commands, or a switch that is connected to a load to send status (or both!).
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class ZigBeeConverterSwitchTrigger extends ZigBeeBaseChannelConverter
        implements ZclAttributeListener, ZclCommandListener {
    private Logger logger = LoggerFactory.getLogger(ZigBeeConverterSwitchTrigger.class);

    private ZclOnOffCluster clusterOnOffServer;
    private ZclOnOffCluster clusterOnOffClient;

    @Override
    public boolean initializeConverter() {
        if (endpoint.getParentNode().getNetworkAddress() == 60773) {
            logger.debug("{}: Initialising device trigger cluster", endpoint.getIeeeAddress());
        }

        clusterOnOffServer = (ZclOnOffCluster) endpoint.getInputCluster(ZclOnOffCluster.CLUSTER_ID);
        clusterOnOffClient = (ZclOnOffCluster) endpoint.getOutputCluster(ZclOnOffCluster.CLUSTER_ID);

        if (clusterOnOffServer == null && clusterOnOffClient == null) {
            logger.error("{}: Error opening device trigger controls", endpoint.getIeeeAddress());
            return false;
        }

        if (clusterOnOffServer != null) {
            clusterOnOffServer.bind();
            clusterOnOffServer.addAttributeListener(this);
            clusterOnOffServer.addCommandListener(this);
        }

        if (clusterOnOffClient != null) {
            clusterOnOffClient.bind();
            clusterOnOffClient.addCommandListener(this);
            clusterOnOffClient.addAttributeListener(this);
        }

        return true;
    }

    @Override
    public void disposeConverter() {
        logger.debug("{}: Closing device trigger cluster", endpoint.getIeeeAddress());

        if (clusterOnOffServer != null) {
            clusterOnOffServer.removeAttributeListener(this);
        }
        if (clusterOnOffClient != null) {
            clusterOnOffClient.removeCommandListener(this);
        }
    }

    @Override
    public Channel getChannel(ThingUID thingUID, ZigBeeEndpoint endpoint) {
        if (endpoint.getInputCluster(ZclOnOffCluster.CLUSTER_ID) == null
                && endpoint.getOutputCluster(ZclOnOffCluster.CLUSTER_ID) == null) {
            return null;
        }

        return createChannel(thingUID, endpoint, ZigBeeBindingConstants.CHANNEL_SWITCH_TRIGGER,
                ZigBeeBindingConstants.ITEM_TYPE_TRIGGER, "Trigger");
    }

    @Override
    public void attributeUpdated(ZclAttribute attribute) {
        logger.debug("{}: ZigBee attribute reports {}", endpoint.getIeeeAddress(), attribute);
        if (attribute.getCluster() == ZclClusterType.ON_OFF && attribute.getId() == ZclOnOffCluster.ATTR_ONOFF) {
            thing.triggerChannel(channelUID);
        }
    }

    @Override
    public void commandReceived(ZclCommand command) {
        logger.debug("{}: ZigBee command received {}", endpoint.getIeeeAddress(), command);
        if (command instanceof OnCommand) {
            updateChannelState(OnOffType.ON);
        }
        if (command instanceof OffCommand) {
            updateChannelState(OnOffType.OFF);
        }
    }

}
