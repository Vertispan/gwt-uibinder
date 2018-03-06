package org.gwtproject.uibinder.processor;

import java.util.Stack;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic.Kind;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * Uses SAX events to construct a DOM Document. Each node in the Document will have a {@link
 * XMLElement.Location} object attached to that Node's user data with the {@value
 * XMLElement#LOCATION_KEY} key.
 */
class W3cDocumentBuilder extends DefaultHandler2 {

  private final Document document;
  private final Stack<Node> eltStack = new Stack<Node>();
  private Locator locator;
  private final MortalLogger logger;
  private final GwtResourceEntityResolver resolver;


  public W3cDocumentBuilder(MortalLogger logger, String pathBase,
      ProcessingEnvironment processingEnvironment)
      throws ParserConfigurationException {
    this.logger = logger;
    document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    eltStack.push(document);
    resolver = new GwtResourceEntityResolver(logger, processingEnvironment, pathBase);
  }

  /**
   * Appends to the existing Text node, if possible.
   */
  @Override
  public void characters(char[] ch, int start, int length) {
    Node current = eltStack.peek();
    if (current.getChildNodes().getLength() == 1
        && current.getChildNodes().item(0).getNodeType() == Node.TEXT_NODE) {
      Text text = (Text) current.getChildNodes().item(0);
      text.appendData(new String(ch, start, length));
    } else {
      Text text = document.createTextNode(new String(ch, start, length));
      eltStack.peek().appendChild(text);
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) {
    Node elt = eltStack.pop();
    assert elt.getLocalName().equals(localName);
  }

  @Override
  public void error(SAXParseException exception) {
    logger.log(Kind.ERROR, exception.getMessage());
    logger.log(Kind.NOTE, "SAXParseException", exception);
  }

  @Override
  public void fatalError(SAXParseException exception) {
    /*
     * Fatal errors seem to be no scarier than error errors, and simply happen
     * due to badly formed XML.
     */
    logger.log(Kind.ERROR, exception.getMessage());
    logger.log(Kind.NOTE, "SAXParseException", exception);
  }

  public Document getDocument() {
    return document;
  }

  @Override
  public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) {
    return resolver.resolveEntity(publicId, systemId);
  }

  /**
   * This is the whole reason for this mess. We want to know where a given element comes from.
   */
  @Override
  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }

  @Override
  public void startElement(String uri, String localName, String qName,
      Attributes attributes) {
    Element elt = document.createElementNS(uri, qName);
    eltStack.peek().appendChild(elt);
    eltStack.push(elt);

    for (int i = 0, j = attributes.getLength(); i < j; i++) {
      elt.setAttributeNS(attributes.getURI(i), attributes.getQName(i),
          attributes.getValue(i));
    }

    XMLElement.Location location = new XMLElement.Location(
        locator.getSystemId(), locator.getLineNumber());
    elt.setUserData(XMLElement.LOCATION_KEY, location, null);
  }

  @Override
  public void warning(SAXParseException exception) {
    logger.log(Kind.WARNING, exception.getMessage());
    logger.log(Kind.NOTE, "SAXParseException", exception);
  }
}
