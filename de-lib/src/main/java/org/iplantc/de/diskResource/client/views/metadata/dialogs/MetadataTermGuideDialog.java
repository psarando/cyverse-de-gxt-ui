package org.iplantc.de.diskResource.client.views.metadata.dialogs;

import org.iplantc.de.client.models.diskResources.MetadataTemplateAttribute;
import org.iplantc.de.diskResource.client.MetadataView;

import com.google.gwt.user.client.ui.HTML;
import com.google.inject.Inject;

import com.sencha.gxt.core.client.dom.ScrollSupport;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;

import java.util.List;

/**
 * Created by sriram on 3/16/17.
 */
public class MetadataTermGuideDialog extends Dialog {

    @Inject
    public MetadataTermGuideDialog(MetadataView.Appearance appearance) {
        setHideOnButtonClick(true);
        setSize("350", "400");
        setPredefinedButtons(PredefinedButton.OK);
        setBodyStyle(appearance.backgroundStyle());
    }


    public void show(List<MetadataTemplateAttribute> attributes, String header) {
        setHeading(header);
        setWidget(buildGuide(attributes));
        show();
    }

    private VerticalLayoutContainer buildGuide(List<MetadataTemplateAttribute> attributes) {
        VerticalLayoutContainer helpVlc = new VerticalLayoutContainer();
        helpVlc.setScrollMode(ScrollSupport.ScrollMode.AUTOY);
        for (MetadataTemplateAttribute mta : attributes) {
            HTML l = new HTML("<b>" + mta.getName() + ":</b> <br/>");
            HTML helpText = new HTML("<p>" + mta.getDescription() + "</p><br/>");
            helpVlc.add(l, new VerticalLayoutContainer.VerticalLayoutData(.25, -1));
            helpVlc.add(helpText, new VerticalLayoutContainer.VerticalLayoutData(.90, -1));
        }
        return helpVlc;
    }
}
