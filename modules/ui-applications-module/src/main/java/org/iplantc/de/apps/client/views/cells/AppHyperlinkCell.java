package org.iplantc.de.apps.client.views.cells;

import org.iplantc.de.apps.client.AppsView;
import org.iplantc.de.apps.client.events.selection.AppNameSelectedEvent;
import org.iplantc.de.apps.shared.AppsModule;
import org.iplantc.de.client.models.apps.App;
import org.iplantc.de.resources.client.messages.I18N;

import static com.google.gwt.dom.client.BrowserEvents.*;
import com.google.common.base.Strings;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.debug.client.DebugInfo;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style.TextDecoration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Event;

/**
 * This is a custom cell which combines the functionality of the {@link AppFavoriteCell} with a clickable
 * hyper-link of an app name.
 *
 * FIXME Create appearance
 * @author jstroot
 * 
 */
public class AppHyperlinkCell extends AbstractCell<App> {

    public interface MyCss extends CssResource {
        String appName();

        String appDisabled();
    }

    public interface Resources extends ClientBundle {
        @Source("AppHyperlinkCell.css")
        MyCss css();
    }

    /**
     * The HTML templates used to render the cell.
     */
    public interface Templates extends SafeHtmlTemplates {

        @SafeHtmlTemplates.Template("<span name='{3}' class='{0}' qtip='{2}'>{1}</span>")
        SafeHtml cell(String textClassName, SafeHtml name, String textToolTip, String elementName);

        @SafeHtmlTemplates.Template("<span id='{4}' name='{3}' class='{0}' qtip='{2}'>{1}</span>")
        SafeHtml debugCell(String textClassName, SafeHtml name, String textToolTip, String elementName, String debugId);
    }

    public final Resources resources = GWT.create(Resources.class);
    protected final Templates templates = GWT.create(Templates.class);
    protected final AppFavoriteCell favoriteCell = new AppFavoriteCell();
    public static final String ELEMENT_NAME = "appName";
    private final AppsView appsView;
    private String baseID;
    private HasHandlers hasHandlers;

    public AppHyperlinkCell(final AppsView appsView) {
        super(CLICK, MOUSEOVER, MOUSEOUT);
        this.appsView = appsView;
        resources.css().ensureInjected();
    }

    @Override
    public void render(Cell.Context context, App value, SafeHtmlBuilder sb) {
        if (value == null) {
            return;
        }
        favoriteCell.render(context, value, sb);
        sb.appendHtmlConstant("&nbsp;");
        String textClassName, textToolTip, elementName;
        SafeHtml safeHtmlName = SafeHtmlUtils
                .fromTrustedString(appsView.highlightSearchText(value.getName()));
        if (!value.isDisabled()) {
            textClassName = resources.css().appName();
            textToolTip = I18N.DISPLAY.run();
            elementName = ELEMENT_NAME;
        } else {
            textClassName = resources.css().appDisabled();
            textToolTip = I18N.DISPLAY.appUnavailable();
            elementName = ELEMENT_NAME;
        }

        if(DebugInfo.isDebugIdEnabled() && !Strings.isNullOrEmpty(baseID)){
            String debugId = baseID + "." + value.getId() + AppsModule.Ids.APP_NAME_CELL;
            sb.append(templates.debugCell(textClassName, safeHtmlName, textToolTip, elementName, debugId));
        }else {
            sb.append(templates.cell(textClassName, safeHtmlName, textToolTip, elementName));
        }

    }

    @Override
    public void onBrowserEvent(Cell.Context context, Element parent, App value, NativeEvent event,
            ValueUpdater<App> valueUpdater) {
        Element eventTarget = Element.as(event.getEventTarget());
        if ((value == null) || !parent.isOrHasChild(eventTarget)) {
            return;
        }
        favoriteCell.onBrowserEvent(context, parent, value, event, valueUpdater);

        Element child = findAppNameElement(parent);
        if (child != null && child.isOrHasChild(eventTarget)) {

            switch (Event.as(event).getTypeInt()) {
                case Event.ONCLICK:
                    if(hasHandlers != null){
                        hasHandlers.fireEvent(new AppNameSelectedEvent(value));
                    }
                    break;
                case Event.ONMOUSEOVER:
                    doOnMouseOver(child, value);
                    break;
                case Event.ONMOUSEOUT:
                    doOnMouseOut(child, value);
                    break;
                default:
                    break;
            }
        }
    }

    public void setBaseDebugId(String baseID) {
        this.baseID = baseID;
        favoriteCell.setBaseDebugId(baseID);

    }

    public void setHasHandlers(HasHandlers hasHandlers) {
        this.hasHandlers = hasHandlers;
        favoriteCell.setHasHandlers(hasHandlers);
    }

    private Element findAppNameElement(Element parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            Node childNode = parent.getChild(i);

            if (Element.is(childNode)) {
                Element child = Element.as(childNode);
                if (child.getAttribute("name").equalsIgnoreCase(ELEMENT_NAME)) { //$NON-NLS-1$
                    return child;
                }
            }
        }

        return null;
    }

    private void doOnMouseOut(Element eventTarget, App value) {
        eventTarget.getStyle().setTextDecoration(TextDecoration.NONE);
    }

    private void doOnMouseOver(Element eventTarget, App value) {
        eventTarget.getStyle().setTextDecoration(TextDecoration.UNDERLINE);
    }

}
