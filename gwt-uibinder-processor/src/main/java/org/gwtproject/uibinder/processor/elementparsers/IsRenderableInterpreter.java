package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.FieldManager;
import org.gwtproject.uibinder.processor.FieldWriter;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

/**
 * Used by {@link RenderablePanelParser} to interpret renderable elements. Declares the appropriate
 * {@link com.google.gwt.user.client.ui.IsRenderable}, and returns the correct HTML to be inlined in
 * the {@link com.google.gwt.user.client.ui.RenderablePanel}.
 */
class IsRenderableInterpreter implements XMLElement.Interpreter<String> {

  private final String fieldName;

  private final UiBinderWriter uiWriter;

  public IsRenderableInterpreter(String fieldName, UiBinderWriter writer) {
    this.fieldName = fieldName;
    this.uiWriter = writer;
    assert writer.useLazyWidgetBuilders();
  }

  public String interpretElement(XMLElement elem)
      throws UnableToCompleteException {
    if (!uiWriter.isRenderableElement(elem)) {
      return null;
    }

    String stamper = uiWriter.declareRenderableStamper();
    FieldManager fieldManager = uiWriter.getFieldManager();
    FieldWriter fieldWriter = fieldManager.require(fieldName);
    FieldWriter childFieldWriter = uiWriter.parseElementToField(elem);

    fieldWriter.addAttachStatement(
        "%s.claimElement(%s.findStampedElement());",
        fieldManager.convertFieldToGetter(childFieldWriter.getName()),
        fieldManager.convertFieldToGetter(stamper));

    // Some operations are more efficient when the Widget isn't attached to
    // the document. Perform them here.
    fieldWriter.addDetachStatement(
        "%s.initializeClaimedElement();",
        fieldManager.convertFieldToGetter(childFieldWriter.getName()));

    fieldWriter.addDetachStatement(
        "%s.logicalAdd(%s);",
        fieldManager.convertFieldToGetter(fieldName),
        fieldManager.convertFieldToGetter(childFieldWriter.getName()));

    // TODO(rdcastro): use the render() call that receives the SafeHtmlBuilder
    String elementHtml = fieldManager.convertFieldToGetter(childFieldWriter.getName()) + ".render("
        + fieldManager.convertFieldToGetter(stamper) + ")";
    return uiWriter.tokenForSafeHtmlExpression(elem, elementHtml);
  }
}
