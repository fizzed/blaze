/*
 * Copyright 2020 Fizzed, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fizzed.blaze.ivy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import static java.util.Optional.ofNullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MavenSettings {
    
    private List<MavenServer> servers;

    public List<MavenServer> getServers() {
        return servers;
    }

    public void setServers(List<MavenServer> servers) {
        this.servers = servers;
    }
    
    public void addServer(MavenServer server) {
        if (this.servers == null) {
            this.servers = new ArrayList<>();
        }
        this.servers.add(server);
    }
    
    public MavenServer findServerById(String id) {
        if (this.servers == null || id == null) {
            return null;
        }
        return this.servers.stream()
            .filter(v -> id.equals(v.getId()))
            .findFirst()
            .orElse(null);
    }
    
    static private Element getElementByTagName(Element elem, String tagName) {
        NodeList elems = elem.getElementsByTagName(tagName);
        
        if (elems == null || elems.getLength() == 0) {
            return null;
        }
        
        return (Element)elems.item(0);
    }
    
    static public MavenSettings parse(
            Path file) throws Exception {
        
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        
        final MavenSettings settings = new MavenSettings();
        
        try (InputStream input = Files.newInputStream(file)) {
            Document doc = builder.parse(input);
            
            final NodeList serversNodes = doc.getElementsByTagName("servers");
            
            if (serversNodes == null || serversNodes.getLength() == 0) {
                return null;
            }
            else if (serversNodes.getLength() > 1) {
                throw new IOException("Invalid " + file + " file. Wanted 1 'servers' but got " + serversNodes.getLength());
            }
            else {
                final NodeList serverNodes = serversNodes.item(0).getChildNodes();

                for (int i = 0; i < serverNodes.getLength(); i++) {
                    final Node serverNode = serverNodes.item(i);
                    if (serverNode instanceof Element) {
                        final Element serverElem = (Element)serverNode;
                        
                        final MavenServer server = new MavenServer();
                        
                        ofNullable(getElementByTagName(serverElem, "id"))
                            .map(v -> v.getTextContent())
                            .ifPresent(v -> server.setId(v));
                        
                        ofNullable(getElementByTagName(serverElem, "username"))
                            .map(v -> v.getTextContent())
                            .ifPresent(v -> server.setUsername(v));
                        
                        ofNullable(getElementByTagName(serverElem, "password"))
                            .map(v -> v.getTextContent())
                            .ifPresent(v -> server.setPassword(v));
                        
                        settings.addServer(server);
                    }
                }
            }
        }
        
        return settings;
    }
    
}