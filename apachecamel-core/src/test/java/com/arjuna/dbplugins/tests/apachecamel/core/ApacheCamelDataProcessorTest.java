/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbplugins.tests.apachecamel.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import com.arjuna.databroker.data.connector.ObservableDataProvider;
import com.arjuna.databroker.data.connector.ObserverDataConsumer;
import com.arjuna.databroker.data.core.DataFlowNodeLifeCycleControl;
import com.arjuna.dbplugins.apachecamel.core.ApacheCamelDataProcessor;
import com.arjuna.dbutilities.testsupport.dataflownodes.dummy.DummyDataSink;
import com.arjuna.dbutilities.testsupport.dataflownodes.dummy.DummyDataSource;

public class ApacheCamelDataProcessorTest
{
    private static final Logger logger = Logger.getLogger(ApacheCamelDataProcessorTest.class.getName());

    @Test
    public void invocation01()
    {
    	DataFlowNodeLifeCycleControl dataFlowNodeLifeCycleControl = TestJ;

        String              name       = "Apache Camel Data Processor";
        Map<String, String> properties = new HashMap<String, String>();

        DummyDataSource          dummyDataSource       = new DummyDataSource("Dummy Data Source", Collections.<String, String>emptyMap());
        ApacheCamelDataProcessor apacheCamelDataSource = new ApacheCamelDataProcessor(name, properties);
        DummyDataSink            dummyDataSink         = new DummyDataSink("Dummy Data Sink", Collections.<String, String>emptyMap());

        dataFlowNodeLifeCycleControl.processCreatedDataFlowNode(UUID.randomUUID().toString(), dummyDataSource, null);
        dataFlowNodeLifeCycleControl.processCreatedDataFlowNode(UUID.randomUUID().toString(), apacheCamelDataSource, null);
        dataFlowNodeLifeCycleControl.processCreatedDataFlowNode(UUID.randomUUID().toString(), dummyDataSink, null);

        ((ObservableDataProvider<String>) dummyDataSource.getDataProvider(String.class)).addDataConsumer((ObserverDataConsumer<String>) apacheCamelDataSource.getDataConsumer(String.class));
        ((ObservableDataProvider<String>) apacheCamelDataSource.getDataProvider(String.class)).addDataConsumer((ObserverDataConsumer<String>) dummyDataSink.getDataConsumer(String.class));

        dummyDataSource.sendData("Test");
        
        try
        {
            Thread.sleep(5000);
        }
        catch (InterruptedException interruptedException)
        {
            fail("Interrupted during sleep");
        }

        dataFlowNodeLifeCycleControl.removeDataFlowNode(dummyDataSource);
        dataFlowNodeLifeCycleControl.removeDataFlowNode(apacheCamelDataSource);
        dataFlowNodeLifeCycleControl.removeDataFlowNode(dummyDataSink);

        assertTrue("Didn't receive any responce", dummyDataSink.receivedData().size() > 0);
    }
}
