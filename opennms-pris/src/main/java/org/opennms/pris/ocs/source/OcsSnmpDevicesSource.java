/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C)
 * 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 *
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 ******************************************************************************
 */
package org.opennms.pris.ocs.source;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.commons.configuration.Configuration;
import org.opennms.ocs.inventory.client.request.logic.GetSnmpDevicesLogic;
import org.opennms.ocs.inventory.client.response.snmp.SnmpDevices;
import org.opennms.pris.source.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OcsSnmpDevicesSource extends AbstractOcsSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OcsSnmpDevicesSource.class);

    public static class Factory implements Source.Factory {

        @Override
        public Source create(final String instance,
                final Configuration config) {
            return new OcsSnmpDevicesSource(instance, config);
        }
    }

    public OcsSnmpDevicesSource(final String instance,
            final Configuration config) {
        super(instance,
                config);
    }

    @Override
    public Object dump() throws Exception {
        final GetSnmpDevicesLogic ocsClient = new GetSnmpDevicesLogic();

        ocsClient.init(this.getUrl(),
                this.getUsername(),
                this.getPassword(),
                this.getChecksum(),
                this.getTags());

        SnmpDevices snmpDevices = ocsClient.getSnmpDevices();

        //** dump the snmp-devices into a file as xml
        if (this.getConfig().containsKey("ocs.source.dump")) {
            File dumpComputers = new File(this.getConfig().getString("ocs.source.dump"));
            if (dumpComputers.createNewFile()) {
                try {
                    JAXBContext jaxbContext = JAXBContext.newInstance(SnmpDevices.class);
                    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
                    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    jaxbMarshaller.marshal(snmpDevices, dumpComputers);
                } catch (JAXBException ex) {
                    LOGGER.error("Dumping snmp-devices to file '{}' has failed.", dumpComputers, ex);
                }
            } else {
                LOGGER.error("Can not dump snmp-devices to file '{}'", dumpComputers);
            }
        }
        return snmpDevices;
    }

}
