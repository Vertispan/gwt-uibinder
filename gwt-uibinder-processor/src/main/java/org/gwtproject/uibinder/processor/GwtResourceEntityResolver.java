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

import static java.util.stream.Collectors.toSet;

import org.gwtproject.uibinder.processor.ext.MyTreeLogger;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Makes the sax xml parser use the {@link javax.annotation.processing.ProcessingEnvironment}.
 *
 * <p>Does special case handling of GWT specific DTDs to be fetched from our download site. If the
 * requested uri starts with <code>http://dl.google.com/gwt/DTD/</code> (or one or two others),
 * provides the contents from a built in resource rather than allowing sax to make a network
 * request.
 */
public class GwtResourceEntityResolver implements EntityResolver {

  private static final Set<String> EXTERNAL_PREFIXES = Collections
      .unmodifiableSet(Arrays.stream(new String[]{
          "http://google-web-toolkit.googlecode.com/files/",
          "http://dl.google.com/gwt/DTD/", "https://dl-ssl.google.com/gwt/DTD/"
      }).collect(toSet()));

  private static final String RESOURCES = "org.gwtproject.uibinder.resources";

  private final String pathBase;

  private final ProcessingEnvironment processingEnvironment;
  private final MyTreeLogger logger;

  public GwtResourceEntityResolver(MyTreeLogger logger, ProcessingEnvironment processingEnvironment,
      String pathBase) {
    this.logger = logger;
    this.processingEnvironment = processingEnvironment;
    this.pathBase = pathBase;
  }

  @Override
  public InputSource resolveEntity(String publicId, String systemId) {
    String matchingPrefix = findMatchingPrefix(systemId);

    FileObject resource = null;
    if (matchingPrefix != null) {

      try {
        resource = processingEnvironment.getFiler()
            .getResource(StandardLocation.CLASS_PATH, RESOURCES,
                systemId.substring(matchingPrefix.length()));
      } catch (IOException e) {
        // empty catch
      }
    }

    if (resource == null) {
      try {
        resource = processingEnvironment.getFiler()
            .getResource(StandardLocation.CLASS_OUTPUT, pathBase, systemId);
      } catch (IOException e) {
        // empty catch
      }
    }

    if (resource != null) {
      String content;
      try {
        CharSequence charSequence = resource.getCharContent(false);
        content = charSequence.toString();
      } catch (IOException ex) {
        logger.log(Kind.ERROR, "Error reading resource: " + resource.getName());
        throw new RuntimeException(ex);
      }
      InputSource inputSource = new InputSource(new StringReader(content));
      inputSource.setPublicId(publicId);
      inputSource.setSystemId(resource.getName());
      return inputSource;
    }

    /*
     * Let Sax find it on the interweb.
     */
    return null;
  }

  private String findMatchingPrefix(String systemId) {
    for (String prefix : EXTERNAL_PREFIXES) {
      if (systemId.startsWith(prefix)) {
        return prefix;
      }
    }
    return null;
  }
}
