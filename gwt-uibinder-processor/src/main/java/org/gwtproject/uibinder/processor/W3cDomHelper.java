/*
 * Copyright 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gwtproject.uibinder.processor;

import org.gwtproject.uibinder.processor.ext.MyTreeLogger;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.StringReader;

import javax.annotation.processing.ProcessingEnvironment;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 *
 */
public class W3cDomHelper {

  private static final String LOAD_EXTERNAL_DTD =
      "http://apache.org/xml/features/nonvalidating/load-external-dtd";

  private final SAXParserFactory factory;
  private final MyTreeLogger logger;
  private final ProcessingEnvironment processingEnvironment;
  // private final Pro

  public W3cDomHelper(MyTreeLogger logger, ProcessingEnvironment processingEnvironment) {
    this.logger = logger;
    this.processingEnvironment = processingEnvironment;
    this.factory = SAXParserFactory.newInstance();
    try {
      factory.setFeature(LOAD_EXTERNAL_DTD, true);
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    } catch (SAXException e) {
      // ignore since parser doesn't know about this feature
    }
    factory.setNamespaceAware(true);
  }

  public Document documentFor(String string, String resourcePath) throws SAXParseException {
    try {
      if (resourcePath != null) {
        int pos = resourcePath.lastIndexOf('/');
        resourcePath = (pos < 0) ? "" : resourcePath.substring(0, pos + 1);
      }
      W3cDocumentBuilder handler = new W3cDocumentBuilder(logger, resourcePath,
          processingEnvironment);
      SAXParser parser = factory.newSAXParser();
      InputSource input = new InputSource(new StringReader(string));
      input.setSystemId(resourcePath);
      parser.parse(input, handler);
      return handler.getDocument();
    } catch (SAXParseException e) {
      // Let SAXParseExceptions through.
      throw e;
    } catch (SAXException | IOException | ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
  }
}
