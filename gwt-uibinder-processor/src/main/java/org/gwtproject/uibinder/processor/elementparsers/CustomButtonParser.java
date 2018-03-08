package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.XMLElement.Interpreter;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import com.google.gwt.user.client.ui.Image;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.lang.model.type.TypeMirror;

/**
 * Parses CustomButton widgets.
 */
public class CustomButtonParser implements ElementParser {

  private static final Set<String> faceNames = new HashSet<String>();
  private static final String IMAGE_CLASS = Image.class.getCanonicalName();

  static {
    faceNames.add("upFace");
    faceNames.add("downFace");
    faceNames.add("upHoveringFace");
    faceNames.add("downHoveringFace");
    faceNames.add("upDisabledFace");
    faceNames.add("downDisabledFace");
  }

  public void parse(final XMLElement elem, final String fieldName,
      TypeMirror type, final UiBinderWriter writer)
      throws UnableToCompleteException {

    /*
     * Parse children. Use an interpreter to leave text in place for
     * HasHTMLParser to find.
     */
    elem.consumeChildElements(new Interpreter<Boolean>() {
      public Boolean interpretElement(XMLElement child)
          throws UnableToCompleteException {
        // CustomButton can only contain Face elements.
        String ns = child.getNamespaceUri();
        String faceName = child.getLocalName();

        if (!ns.equals(elem.getNamespaceUri())) {
          writer.die(elem, "Invalid child namespace: %s", ns);
        }
        if (!faceNames.contains(faceName)) {
          writer.die(elem, "Invalid CustomButton face: %s:%s", ns, faceName);
        }

        HtmlInterpreter interpreter = HtmlInterpreter.newInterpreterForUiObject(
            writer, fieldName);
        String innerHtml = child.consumeInnerHtml(interpreter);
        if (innerHtml.length() > 0) {
          writer.addStatement("%s.%s().setHTML(%s);", fieldName,
              faceNameGetter(faceName), writer.declareTemplateCall(innerHtml,
                  fieldName));
        }

        if (child.hasAttribute("image")) {
          String image = child.consumeImageResourceAttribute("image");
          writer.addStatement("%s.%s().setImage(new %s(%s));", fieldName,
              faceNameGetter(faceName), IMAGE_CLASS, image);
        }
        return true; // We consumed it
      }
    });
  }

  private String faceNameGetter(String faceName) {
    return "get" + faceName.substring(0, 1).toUpperCase(Locale.ROOT)
        + faceName.substring(1);
  }
}
