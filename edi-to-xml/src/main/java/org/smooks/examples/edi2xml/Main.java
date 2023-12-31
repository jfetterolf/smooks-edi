/*-
 * ========================LICENSE_START=================================
 * Smooks Example :: EDI-to-XML
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.examples.edi2xml;

import org.smooks.Smooks;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.engine.report.HtmlReportGenerator;
import org.smooks.support.StreamUtils;
import org.smooks.io.payload.StringResult;
import org.xml.sax.SAXException;


import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.json.JsonMapper;
// import com.fasterxml.jackson.databind.json.JsonMapper.Builder;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
// import com.thoughtworks.xstream.mapper.XmlFriendlyMapper;

// import javax.imageio.IIOException;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
// import java.util.Locale;
// import java.util.Scanner;

import org.json.JSONObject;
import org.json.XML;



/**
 * Simple example main class.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Main {

    // Change string to desired x12Format: "271", "834", "837", or "order" (testing: "837-generic", "834-generic")
    private static String x12Format = "271";
    private static byte[] messageIn = readInputMessage();
    

    protected static String runSmooksTransform() throws IOException, SAXException, SmooksException {
        // Instantiate Smooks with the config...
        Smooks smooks = new Smooks("smooks-config-" + x12Format + ".xml");
        try {
             // Create an exec context - no profiles....
            ExecutionContext executionContext = smooks.createExecutionContext();

            StringResult result = new StringResult();

            // Configure the execution context to generate a report...
            executionContext.getContentDeliveryRuntime().addExecutionEventListener(new HtmlReportGenerator("target/report/report.html"));

            // Filter the input message to the outputWriter, using the execution context...
            smooks.filterSource(executionContext, new StreamSource(new ByteArrayInputStream(messageIn)), result);


            // Saving xml to file
            String xmlFilePath = "output-message.xml";
            deleteFile(xmlFilePath);
            writeXml(xmlFilePath, result.getResult().toString());

            
            System.out.println("\n\n===========JSON Result============");
            String jsonString = convert(result.getResult().toString());
            System.out.println(jsonString);
            System.out.println("======================================\n\n");

            String jsonFilePath = "output-message.json";
            deleteFile(jsonFilePath);
            writeJson(jsonFilePath, jsonString);

            return result.getResult();
        } finally {
            smooks.close();
        }
    }

    public static void main(String[] args) throws IOException, SAXException, SmooksException {
        // // SETTING UP FOR USER INPUT
        // // need to get user input for 837, 834 or 271
        // Scanner s = new Scanner(System.in);
        // System.out.println("Enter desired X12 format: 837, 834, or 271");
        // String x12Format = s.nextLine().toString();
        
        // if (x12Format.equals("837") || x12Format.equals("834") || x12Format.equals("271")) {
        //     System.out.println("success: " + x12Format);
        // } else {
        //     x12Format = "order";
        //     System.out.println("Running with default message: " + x12Format);
        // }
        // s.close();
        // // save user input for choice of config file, input-message file, and output-message file(s)


        System.out.println("\n\n==============Message In==============");
        System.out.println(new String(messageIn));
        System.out.println("======================================\n");

        pause("The EDI input stream & resulting JSON can be seen above.  Press 'enter' to see this stream transformed into XML...");

        String messageOut = Main.runSmooksTransform();

        System.out.println("================XML Result============");
        System.out.println(messageOut);
        System.out.println("======================================\n\n");
        System.out.println("type:" + messageOut.getClass().getName());

        pause("And that's it!  Press 'enter' to finish...");
    }

    private static byte[] readInputMessage() {
        try {
            return StreamUtils.readStream(new FileInputStream("input-message-" + x12Format + ".edi"));
        } catch (IOException e) {
            e.printStackTrace();
            return "<no-message/>".getBytes();
        }
    }

    private static void pause(String message) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("> " + message);
            in.readLine();
        } catch (IOException e) {
        }
        System.out.println("\n");
    }

    public static void deleteFile(String filePath) {
        File fileToDelete = new File(filePath);

        if (fileToDelete.exists()) {
            fileToDelete.delete();
        }
    }

    public static void writeXml(String filePath, String xmlString) {
        try {
            Path path = Paths.get(filePath);
            Files.write(path, xmlString.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // created issue where duplicate elements overwrite previously written elements
    public static String convertXmlToJson(String xml) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        JsonNode node = xmlMapper.readTree(xml.getBytes());
        ObjectMapper jsonMapper = new ObjectMapper();
        return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    }

    // looks like this fixes the issue with overwriting duplicates
    public static String convert(String xml) throws IOException {

        // Convert XML to JSON
        JSONObject json = XML.toJSONObject(xml);

        // Return Output
        return json.toString();
    }

    public static void writeJson(String filePath, String jsonString) {
        try {
            Path path = Paths.get(filePath);
            Files.write(path, jsonString.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
