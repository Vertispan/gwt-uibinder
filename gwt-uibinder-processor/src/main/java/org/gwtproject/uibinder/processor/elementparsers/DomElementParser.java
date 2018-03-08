package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.AptUtil;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.type.TypeMirror;

/**
 * Parses a dom element and all of its children. Note that this parser does not make recursive calls
 * to parse child elements, unlike what goes on with widget parsers. Instead, we consume the inner
 * html of the given element into a single string literal, used to instantiate the dom tree at run
 * time.
 */
public class DomElementParser implements ElementParser {

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {
    HtmlInterpreter interpreter = new HtmlInterpreter(writer, fieldName,
        new HtmlMessageInterpreter(writer, fieldName));

    interpreter.interpretElement(elem);

    writer.beginAttachedSection(fieldName);
    String html = elem.consumeOpeningTag() + elem.consumeInnerHtml(interpreter)
        + elem.getClosingTag();
    writer.endAttachedSection();

    writer.setFieldInitializer(fieldName, String.format(
        "(%1$s) UiBinderUtil.fromHtml(%2$s)",
        AptUtil.asQualifiedNameable(type).getQualifiedName(),
        writer.declareTemplateCall(html, fieldName)));
  }
}
