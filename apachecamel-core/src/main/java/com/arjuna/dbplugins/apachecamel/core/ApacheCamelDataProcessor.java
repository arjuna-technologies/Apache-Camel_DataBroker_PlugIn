/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbplugins.apachecamel.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.camel.CamelContext;
import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultCamelContext;

import com.arjuna.databroker.data.DataConsumer;
import com.arjuna.databroker.data.DataProvider;
import com.arjuna.databroker.data.DataFlow;
import com.arjuna.databroker.data.DataProcessor;
import com.arjuna.databroker.data.jee.annotation.DataConsumerInjection;
import com.arjuna.databroker.data.jee.annotation.DataProviderInjection;
import com.arjuna.databroker.data.jee.annotation.PostActivated;
import com.arjuna.databroker.data.jee.annotation.PreDeactivated;

public class ApacheCamelDataProcessor implements DataProcessor
{
    private static final Logger logger = Logger.getLogger(ApacheCamelDataProcessor.class.getName());

    public static final String CAMELCONFIG_PROPNAME  = "Camel Config";
    public static final String ENDPOINTNAME_PROPNAME = "Endpoint Name";

    public ApacheCamelDataProcessor(String name, Map<String, String> properties)
    {
        logger.log(Level.FINE, "ApacheCamelDataProcessor: " + name + ", " + properties);

        _name       = name;
        _properties = properties;
    }

    @Override
    public DataFlow getDataFlow()
    {
        return _dataFlow;
    }

    @Override
    public void setDataFlow(DataFlow dataFlow)
    {
        _dataFlow = dataFlow;
    }

    @Override
    public String getName()
    {
        return _name;
    }

    @Override
    public void setName(String name)
    {
        _name = name;
    }

    @Override
    public Map<String, String> getProperties()
    {
        return Collections.unmodifiableMap(_properties);
    }

    @Override
    public void setProperties(Map<String, String> properties)
    {
        _properties = properties;
    }

    @PostActivated
    public void start()
    {
        try
        {
            _camelContext = new DefaultCamelContext();
            _camelContext.start();
            _endpoint = _camelContext.getEndpoint(_properties.get(ENDPOINTNAME_PROPNAME));
            _consumer = _endpoint.createConsumer(null);
            _producer = _endpoint.createProducer();
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "ApacheCamelDataProcessor: failed to start camel context", throwable);
            _camelContext = null;
        }
    }

    @PreDeactivated
    public void finish()
    {
        try
        {
            _camelContext.stop();
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "ApacheCamelDataProcessor: failed to stop camel context", throwable);
        }
        _camelContext = null;
    }

    public void consume(String data)
    {
        String result;

        result = data;

        _dataProvider.produce(result);
    }

    @Override
    public Collection<Class<?>> getDataProviderDataClasses()
    {
        Set<Class<?>> dataProviderDataClasses = new HashSet<Class<?>>();

        dataProviderDataClasses.add(String.class);

        return dataProviderDataClasses;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> DataConsumer<T> getDataConsumer(Class<T> dataClass)
    {
        if (dataClass == String.class)
            return (DataConsumer<T>) _dataConsumer;
        else
            return null;
    }

    @Override
    public Collection<Class<?>> getDataConsumerDataClasses()
    {
        Set<Class<?>> dataConsumerDataClasses = new HashSet<Class<?>>();

        dataConsumerDataClasses.add(String.class);

        return dataConsumerDataClasses;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> DataProvider<T> getDataProvider(Class<T> dataClass)
    {
        if (dataClass == String.class)
            return (DataProvider<T>) _dataProvider;
        else
            return null;
    }

    private CamelContext _camelContext;
    private Endpoint     _endpoint;
    private Consumer     _consumer;
    private Producer     _producer;

    private DataFlow             _dataFlow;
    private String               _name;
    private Map<String, String>  _properties;
    @DataConsumerInjection(methodName="consume")
    private DataConsumer<String> _dataConsumer;
    @DataProviderInjection
    private DataProvider<String> _dataProvider;
}
