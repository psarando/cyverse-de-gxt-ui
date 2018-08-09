/**
 * @author psarando
 */
import React, { Component } from "react";
import { Field } from "redux-form";
import { injectIntl } from "react-intl";

import build from "../../util/DebugIDUtil";
import withI18N, { formatMessage, getMessage } from "../../util/I18NWrapper";
import intlData from "../messages";
import styles from "../style";
import ids from "./ids";

import { FormCheckbox, FormCheckboxTableCell, FormTextField } from "../../util/FormField";
import OrderedGridToolbar from "./OrderedGridToolbar";

import Button from "@material-ui/core/Button";
import Dialog from "@material-ui/core/Dialog";
import DialogActions from "@material-ui/core/DialogActions";
import DialogContent from "@material-ui/core/DialogContent";
import DialogTitle from "@material-ui/core/DialogTitle";
import IconButton from "@material-ui/core/IconButton";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import { withStyles } from "@material-ui/core/styles";

import ContentRemove from "@material-ui/icons/Delete";
import ContentEdit from "@material-ui/icons/Edit";

const normalizeDefault = (fields, change, is_default) => {
    if (is_default) {
        fields.forEach((field, index) => {
            let attrEnum = fields.get(index);
            if (attrEnum.is_default) {
                change(field, {...attrEnum, is_default: false});
            }
        });
    }

    return is_default;
};

class AttributeEnumEditDialog extends Component {
    normalizeDefaultField = (is_default) => {
        const { fields, change } = this.props;
        return normalizeDefault(fields, change, is_default);
    };

    render() {
        const { open, intl, field, error, onClose } = this.props;

        const dialogID = build(ids.METADATA_TEMPLATE_FORM, field, ids.DIALOG);
        const dialogTitleID = build(dialogID, ids.TITLE);

        return (
            <Dialog
                open={open}
                disableBackdropClick
                disableEscapeKeyDown
                aria-labelledby={dialogTitleID}
            >
                <DialogTitle id={dialogTitleID}>{getMessage("dialogTitleEditEnumValue")}</DialogTitle>
                <DialogContent>
                    <Field name={`${field}.value`}
                           label={formatMessage(intl, "value")}
                           id={build(dialogID, ids.ENUM_VALUE)}
                           required={true}
                           autoFocus
                           margin="dense"
                           component={FormTextField}
                    />
                    <Field name={`${field}.is_default`}
                           label={formatMessage(intl, "enumDefaultLabel")}
                           id={build(dialogID, ids.ENUM_VALUE_DEFAULT)}
                           color="primary"
                           component={FormCheckbox}
                           normalize={this.normalizeDefaultField}
                    />
                </DialogContent>
                <DialogActions>
                    <Button id={build(dialogID, ids.BUTTONS.CLOSE)}
                            color="primary"
                            disabled={!!error}
                            onClick={onClose}
                    >
                        {getMessage("done")}
                    </Button>
                </DialogActions>
            </Dialog>
        );
    }
}

class AttributeEnumEditGrid extends Component {
    constructor(props) {
        super(props);

        this.newEnumCount = 1;

        this.state = {
            selected: -1,
            editingEnumIndex: -1,
        };
    }

    newEnumValue = () => formatMessage(this.props.intl, "newValue", {count: this.newEnumCount++});

    onAddEnum = () => {
        let value = this.newEnumValue();
        const fields = this.props.fields;
        const attributeEnums = fields.getAll() || [];

        const valuesMatch = attrEnum => (attrEnum.value === value);
        while (attributeEnums.findIndex(valuesMatch) > -1) {
            value = this.newEnumValue();
        }

        this.setState({selected: 0});

        this.props.fields.unshift({
            value,
            is_default: false,
        });
    };

    onEnumRemoved = (index) => {
        let { selected } = this.state;

        // fix selection
        if (index === selected) {
            selected = -1;
        } else if (index < selected) {
            selected--;
        }

        this.setState({selected});

        this.props.fields.remove(index);
    };

    handleSelect = (index) => {
        const { selected } = this.state;
        this.setState({ selected: selected === index ? -1 : index });
    };

    moveUp = () => {
        this.moveSelectedEnum(-1);
    };

    moveDown = () => {
        this.moveSelectedEnum(1);
    };

    moveSelectedEnum = (offset) => {
        const { selected } = this.state;

        this.setState({selected: selected + offset});

        this.props.fields.move(selected, selected + offset);
    };

    normalizeDefaultField = (is_default) => {
        const { fields, change } = this.props;
        return normalizeDefault(fields, change, is_default);
    };

    render() {
        const { classes, intl, parentID, fields, change, meta: { error } } = this.props;
        const { selected, editingEnumIndex } = this.state;

        const tableID = build(parentID, ids.ENUM_VALUES_GRID);

        return (
            <div className={classes.attributeTableContainer}>

                <OrderedGridToolbar title={getMessage("enumValues")}
                                    error={error}
                                    parentID={tableID}
                                    onAddItem={this.onAddEnum}
                                    moveUp={this.moveUp}
                                    moveUpDisabled={selected <= 0}
                                    moveDown={this.moveDown}
                                    moveDownDisabled={selected < 0 || (fields.length - 1) <= selected}
                />

                <div className={classes.attributeTableWrapper}>
                    <Table aria-labelledby={build(tableID, ids.TITLE)}>
                        <TableBody>
                            {fields && fields.map((field, index) => {
                                const isSelected = (index === selected);
                                const {
                                    value,
                                } = fields.get(index);

                                const rowID = build(ids.METADATA_TEMPLATE_FORM, field);

                                return (
                                    <TableRow
                                        hover
                                        tabIndex={-1}
                                        key={field}
                                        selected={isSelected}
                                        onClick={() => this.handleSelect(index)}
                                    >
                                        <TableCell component="th" scope="row">
                                            {value}
                                        </TableCell>
                                        <Field name={`${field}.is_default`}
                                               id={build(rowID, ids.ENUM_VALUE_DEFAULT)}
                                               component={FormCheckboxTableCell}
                                               normalize={this.normalizeDefaultField}
                                        />
                                        <TableCell padding="none">
                                            <IconButton id={build(rowID, ids.BUTTONS.EDIT)}
                                                        aria-label={formatMessage(intl, "edit")}
                                                        className={classes.button}
                                                        onClick={event => {
                                                            event.stopPropagation();
                                                            this.setState({editingEnumIndex: index});
                                                        }}
                                            >
                                                <ContentEdit />
                                            </IconButton>
                                            <IconButton id={build(rowID, ids.BUTTONS.DELETE)}
                                                        aria-label={formatMessage(intl, "delete")}
                                                        classes={{root: classes.deleteIcon}}
                                                        onClick={event => {
                                                            event.stopPropagation();
                                                            this.onEnumRemoved(index);
                                                        }}
                                            >
                                                <ContentRemove />
                                            </IconButton>
                                        </TableCell>
                                    </TableRow>
                                );
                            })}
                        </TableBody>
                        <TableHead className={classes.tableHead}>
                            <TableRow>
                                <TableCell component="th" scope="row">{getMessage("value")}</TableCell>
                                <TableCell padding="checkbox">{getMessage("default")}</TableCell>
                                <TableCell padding="none" />
                            </TableRow>
                        </TableHead>
                    </Table>
                </div>

                {fields && fields.map((field, index) =>
                    <AttributeEnumEditDialog key={field}
                                             open={editingEnumIndex === index}
                                             change={change}
                                             fields={fields}
                                             field={field}
                                             error={error}
                                             intl={intl}
                                             onClose={() => this.setState({editingEnumIndex: -1})}
                    />
                )
                }
            </div>
        );
    }
}

export default withStyles(styles)(withI18N(injectIntl(AttributeEnumEditGrid), intlData));
