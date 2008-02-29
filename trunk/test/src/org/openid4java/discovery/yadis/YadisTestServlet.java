/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery.yadis;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import java.io.*;

/**
 * Simple servlet that builds up responses from varios test-data files
 * for testing the Yadis protocol.
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class YadisTestServlet extends HttpServlet
{
    String _testDataPath;

    public YadisTestServlet() throws ServletException
    {
        _testDataPath = System.getProperty("YADIS_TEST_DATA");

        if (_testDataPath == null)
            throw new ServletException("YADIS_TEST_DATA path not initialized");
    }

    public void doHead(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        // set the headers
        String headersFile = request.getParameter("headers");
        setHeadersFromFile(headersFile, response);

    }

    /**
     * Builds a response based on the parameters received in the request,
     * with the following conventions:
     *
     * - the header name-values are extracted from a file with the name specified
     * by the "headers" or "getheaders" (if they need to be different
     * for HEAD and GET requests) parameters;
     * the file should contain a "headername=value" pair on each line
     * Status code should be given on a line with the header name "status"
     *
     * - if there is a "xrds" parameter, its value should point to a file
     * which is streamed for download
     *
     * - otherwise, if there is a "html" parameter, its value should point
     * to a file which is returned as a HTML resonse
     *
     * Headers will always be set if specified; only one of "xrds" and "html"
     * (in this order) will be handled.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        String headersFile = request.getParameter("headers");
        String getHeadersFile = request.getParameter("getheaders");
        String xrdsFile = request.getParameter("xrds");
        String htmlFile = request.getParameter("html");

        // set the headers
        if (getHeadersFile != null)
            setHeadersFromFile(getHeadersFile, response);
        else if (headersFile != null)
            setHeadersFromFile(headersFile, response);

        // XRDS download
        if (xrdsFile != null)
        {
            BufferedInputStream input = new BufferedInputStream(
                    new FileInputStream(_testDataPath + "/xrds/" + xrdsFile));

            ServletOutputStream output = response.getOutputStream();

            byte[] data = new byte[8192];
            int bytesRead = input.read(data, 0, data.length);
            while (bytesRead > 0)
            {
                output.write(data, 0, bytesRead);
                bytesRead = input.read(data, 0, data.length);
            }

            input.close();
            output.close();

        } else if (htmlFile != null) // HTML response
        {
            BufferedReader input = new BufferedReader(
                    new FileReader(_testDataPath + "/html/" + htmlFile));

            //PrintWriter output = new PrintWriter( response.getWriter());
            ServletOutputStream output = response.getOutputStream();

            String line = input.readLine();
            while (line != null)
            {
                output.println(line);
                line = input.readLine();
            }

            input.close();
            output.close();
        }

    }

    private void setHeadersFromFile(String filename,
                                    HttpServletResponse response)
            throws IOException
    {
        BufferedReader input = new BufferedReader(
                new FileReader(_testDataPath + "/headers/" + filename));

        String line;
        while ((line = input.readLine()) != null)
        {
            int equalPos = line.indexOf("=");
            if (equalPos > -1)
            {
                String headerName = line.substring(0, equalPos);
                String headerValue = line.substring(equalPos + 1);

                if (headerName.equals("status"))
                    response.setStatus(Integer.parseInt(headerValue));
                else
                {
                    response.addHeader(headerName, headerValue);
                }
            }
        }
    }


}
