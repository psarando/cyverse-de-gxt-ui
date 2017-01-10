import React from 'react';
import ReactDOM from 'react-dom';

import CategoryTree from './apps/details/CategoryTree';
import CopyTextArea from './apps/details/CopyTextArea';
import ToolDetails from './apps/details/ToolDetails';

const renderToolDetails = (elementID, toolDetailsAppearance, app) => {
    ReactDOM.render(
        <ToolDetails appearance={toolDetailsAppearance} app={app} />,
        document.getElementById(elementID)
    );
};

const renderCopyTextArea = (elementID, btnText, textToCopy) => {
    ReactDOM.render(
        <CopyTextArea btnText={btnText} text={ textToCopy }/>,
        document.getElementById(elementID)
    );
};

const renderCategoryTree = (elementID, app, presenter, appearance) => {
    ReactDOM.render(
        <CategoryTree app={app} presenter={presenter} appearance={appearance} />,
        document.getElementById(elementID)
    );
};

export { renderCopyTextArea, renderToolDetails, renderCategoryTree };