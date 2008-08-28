/*
 * Copyright 2008 University of Chicago/Argonne National Laboratory
 */

package org.openid4java.util;

import java.io.File;
import java.io.IOException;

import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import org.xml.sax.SAXException;

import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.traversal.DocumentTraversal;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author Neill Miller, Rachana Ananthakrishnan
 */
public class XmlConfigReader
{
    public XmlConfigReader()
    {
    }

    public Vector getValidators(String filename)
    {
        Vector validators = null;

        Document doc = parseConfigFile(filename);
        if (doc != null)
        {
            validators = extractValidatorConfigs(doc);
        }
        return validators;
    }

    public Vector getAttrProviders(String filename)
    {
        Vector attrProviders = null;

        Document doc = parseConfigFile(filename);
        if (doc != null)
        {
            attrProviders = extractAttrProviderConfigs(doc);
        }
        return attrProviders;
    }

    private Document parseConfigFile(String filename)
    {
        Document doc = null;
        DocumentBuilder docBuilder = null;
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setIgnoringElementContentWhitespace(true);
        try
        {
            docBuilder = docBuilderFactory.newDocumentBuilder();

            File sourceFile = new File(filename);
            try
            {
                doc = docBuilder.parse(sourceFile);
            }
            catch(SAXException e)
            {
                System.out.println("Invalid XML file structure: " + e.getMessage());
            }
            catch(IOException e)
            {
                System.out.println("Could not read source file: " + e.getMessage());
            }
        }
        catch(ParserConfigurationException e)
        {
            System.out.println("Wrong parser configuration: " + e.getMessage());
        }
        return doc;
    }

    private Vector extractValidatorConfigs(Document doc)
    {
        Vector validators = new Vector();
        DocumentTraversal traversal = (DocumentTraversal)doc;

        NodeIterator iterator = traversal.createNodeIterator(
            doc.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);

        int paramIndex = 0;
        IdPValidatorConfig vConfig = null;
        NameValuePair[] parameters = new NameValuePair[0];

        for (Node n = iterator.nextNode(); n != null; n = iterator.nextNode())
        {
            Element elem = (Element)n;
            if (elem.getTagName().toLowerCase().equals("validator"))
            {
                if (vConfig != null)
                {
                    vConfig.setParameters(parameters);
                    validators.add((Object)vConfig);
                }
                vConfig = new IdPValidatorConfig();
                String className = elem.getAttribute("name");
                vConfig.setClassName(className);
            }
            else if (elem.getTagName().toLowerCase().equals("parameter"))
            {
                String name = elem.getAttribute("name");
                String value = elem.getAttribute("value");
                NameValuePair[] tmpParameters = new NameValuePair[parameters.length + 1];

                for(int i = 0; i < parameters.length; i++)
                {
                    tmpParameters[i] = parameters[i];
                }
                tmpParameters[parameters.length] = new NameValuePair(name, value);
                parameters = tmpParameters;
            }
        }

        if (vConfig != null)
        {
            vConfig.setParameters(parameters);
            validators.add((Object)vConfig);
        }
        return validators;
    }

    private Vector extractAttrProviderConfigs(Document doc)
    {
        Vector attrProviders = new Vector();
        DocumentTraversal traversal = (DocumentTraversal)doc;

        NodeIterator iterator = traversal.createNodeIterator(
            doc.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);

        int paramIndex = 0;
        AttributeProviderConfig vConfig = null;
        NameValuePair[] parameters = new NameValuePair[0];

        for (Node n = iterator.nextNode(); n != null; n = iterator.nextNode())
        {
            Element elem = (Element)n;
            if (elem.getTagName().toLowerCase().equals("attributeprovider"))
            {
                if (vConfig != null)
                {
                    vConfig.setParameters(parameters);
                    attrProviders.add((Object)vConfig);
                }
                vConfig = new AttributeProviderConfig();
                String className = elem.getAttribute("name");
                vConfig.setClassName(className);
            }
            else if (elem.getTagName().toLowerCase().equals("parameter"))
            {
                String name = elem.getAttribute("name");
                String value = elem.getAttribute("value");
                NameValuePair[] tmpParameters = new NameValuePair[parameters.length + 1];

                for(int i = 0; i < parameters.length; i++)
                {
                    tmpParameters[i] = parameters[i];
                }
                tmpParameters[parameters.length] = new NameValuePair(name, value);
                parameters = tmpParameters;
            }
        }

        if (vConfig != null)
        {
            vConfig.setParameters(parameters);
            attrProviders.add((Object)vConfig);
        }
        return attrProviders;
    }
}
