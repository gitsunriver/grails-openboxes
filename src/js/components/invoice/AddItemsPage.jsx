import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';
import _ from 'lodash';
import update from 'immutability-helper';

import { showSpinner, hideSpinner } from '../../actions';
import Translate from '../../utils/Translate';
import ArrayField from '../form-elements/ArrayField';
import LabelField from '../form-elements/LabelField';
import { renderFormField } from '../../utils/form-utils';
import apiClient from '../../utils/apiClient';
import InvoiceItemsModal from './InvoiceItemsModal';
import ButtonField from '../form-elements/ButtonField';
import TextField from '../form-elements/TextField';

const DELETE_BUTTON_FIELD = {
  type: ButtonField,
  label: 'react.default.button.delete.label',
  defaultMessage: 'Delete',
  flexWidth: '1',
  fieldKey: '',
  buttonLabel: 'react.default.button.delete.label',
  buttonDefaultMessage: 'Delete',
  getDynamicAttr: ({
    fieldValue, removeItem, removeRow, updateTotalCount,
  }) => ({
    onClick: fieldValue && fieldValue.id ? () => {
      removeItem(fieldValue.id).then(() => removeRow());
      updateTotalCount(-1);
    } : () => { updateTotalCount(-1); removeRow(); },
  }),
  attributes: {
    className: 'btn btn-outline-danger',
  },
};

const FIELDS = {
  invoiceItems: {
    type: ArrayField,
    virtualized: true,
    totalCount: ({ totalCount }) => totalCount,
    isRowLoaded: ({ isRowLoaded }) => isRowLoaded,
    loadMoreRows: ({ loadMoreRows }) => loadMoreRows(),
    isFirstPageLoaded: ({ isFirstPageLoaded }) => isFirstPageLoaded,
    // eslint-disable-next-line react/prop-types
    addButton: () => (
      <InvoiceItemsModal
        btnOpenText="react.default.button.addLines.label"
        btnOpenDefaultText="Add lines"
      >
        <Translate id="react.default.button.addLine.label" defaultMessage="Add line" />
      </InvoiceItemsModal>
    ),
    fields: {
      orderNumber: {
        type: LabelField,
        label: 'react.invoice.orderNumber.label',
        defaultMessage: 'PO Number',
        flexWidth: '1',
      },
      shipmentNumber: {
        type: LabelField,
        label: 'react.invoice.shipmentNumber.label',
        defaultMessage: 'Shipment Number',
        flexWidth: '1',
      },
      budgetCode: {
        type: LabelField,
        label: 'react.invoice.budgetCode.label',
        defaultMessage: 'Budget Code',
        flexWidth: '1',
      },
      glCode: {
        type: LabelField,
        label: 'react.invoice.glCode.label',
        defaultMessage: 'GL Code',
        flexWidth: '1',
      },
      productCode: {
        type: LabelField,
        label: 'react.invoice.itemNo.label',
        defaultMessage: 'Item No',
        flexWidth: '1',
      },
      description: {
        type: LabelField,
        label: 'react.invoice.description.label',
        defaultMessage: 'Description',
        flexWidth: '1',
      },
      quantity: {
        type: TextField,
        label: 'react.invoice.qty.label',
        defaultMessage: 'Qty',
        flexWidth: '1',
        required: true,
        attributes: {
          type: 'number',
          showError: true,
        },
        getDynamicAttr: ({ rowIndex, values, updateRow }) => ({
          onBlur: () => updateRow(values, rowIndex),
        }),
      },
      uom: {
        type: LabelField,
        label: 'react.invoice.uom.label',
        defaultMessage: 'UOM',
        flexWidth: '1',
      },
      amount: {
        type: LabelField,
        label: 'react.invoice.unitPrice.label',
        defaultMessage: 'Unit Price',
        flexWidth: '1',
      },
      totalAmount: {
        type: LabelField,
        label: 'react.invoice.totalPrice.label',
        defaultMessage: 'Total Price',
        flexWidth: '1',
      },
      deleteButton: DELETE_BUTTON_FIELD,
    },
  },
};

class AddItemsPage extends Component {
  constructor(props) {
    super(props);
    this.state = {
      values: { ...this.props.initialValues, invoiceItems: [] },
      isFirstPageLoaded: false,
    };

    this.isRowLoaded = this.isRowLoaded.bind(this);
    this.loadMoreRows = this.loadMoreRows.bind(this);
    this.updateTotalCount = this.updateTotalCount.bind(this);
    this.removeItem = this.removeItem.bind(this);
    this.updateRow = this.updateRow.bind(this);
  }

  /**
   * Sets state of invoice items after fetch and calls method to fetch next items
   * @param {string} startIndex
   * @public
   */
  setInvoiceItems(response, startIndex) {
    this.props.showSpinner();
    const { data } = response.data;

    this.setState({
      values: {
        ...this.state.values,
        invoiceItems: !_.isNull(startIndex) ? _.uniqBy(_.concat(this.state.values.invoiceItems, data), 'id') : data,
      },
    }, () => {
      if (!_.isNull(startIndex) &&
        this.state.values.invoiceItems.length !== this.state.values.totalCount) {
        this.loadMoreRows({ startIndex: startIndex + this.props.pageSize });
      }
      this.props.hideSpinner();
    });
  }

  /**
   * Checks if row is loaded, needed for pagination
   * @param {string} index
   * @public
   */
  isRowLoaded({ index }) {
    return !!this.state.values.invoiceItems[index];
  }

  /**
   * Loads more rows, needed for pagination
   * @param {index} startIndex
   * @public
   */
  loadMoreRows({ startIndex }) {
    this.setState({
      isFirstPageLoaded: true,
    });
    const url = `/openboxes/api/invoices/${this.state.values.id}/invoiceItems?offset=${startIndex}&max=${this.props.pageSize}`;
    apiClient.get(url)
      .then((response) => {
        this.setInvoiceItems(response, startIndex);
      });
  }

  /**
   * Updates total count of items after removing item
   * @param {integer} value
   * @public
   */
  updateTotalCount(value) {
    this.setState({
      values: {
        ...this.state.values,
        totalCount: this.state.values.totalCount + value,
      },
    });
  }

  /**
   * Updates row after changing value
   * @param {integer} value
   * @param {string} index
   * @public
   */
  updateRow(values, index) {
    const item = values.invoiceItems[index];
    this.setState({
      values: update(values, {
        invoiceItems: { [index]: { $set: item } },
      }),
    });
  }

  /**
   * Saves invoice items
   * @param {object} values
   * @public
   */
  saveInvoiceItems(values) {
    const url = `/openboxes/api/invoices/${this.state.values.id}/items`;
    const payload = {
      id: values.id,
      invoiceItems: _.map(values.invoiceItems, item => ({
        id: item.id,
        quantity: _.toInteger(item.quantity),
      })),
    };
    if (payload.invoiceItems.length) {
      return apiClient.post(url, payload)
        .catch(() => Promise.reject(new Error('react.invoice.error.saveInvoiceItems.label')));
    }
    return Promise.resolve();
  }

  /**
   * Saves invoices items and goes to the step.
   * @param {object} values
   * @public
   */
  nextPage(values) {
    this.saveInvoiceItems(values).then(() => {
      this.props.nextPage(values);
    });
  }

  /**
   * Removes chosen item from items list.
   * @param {string} itemId
   * @public
   */
  removeItem(itemId) {
    const removeItemsUrl = `/openboxes/api/invoices/${itemId}/removeItem`;

    return apiClient.delete(removeItemsUrl)
      .catch(() => {
        this.props.hideSpinner();
        return Promise.reject(new Error('react.invoice.error.deleteInvoiceItem.label'));
      });
  }

  render() {
    return (
      <Form
        onSubmit={() => {}}
        mutators={{ ...arrayMutators }}
        initialValues={this.state.values}
        render={({ handleSubmit, values }) => (
          <div className="d-flex flex-column">
            <form onSubmit={handleSubmit}>
              <div className="table-form mt-4">
                {_.map(FIELDS, (fieldConfig, fieldName) =>
                  renderFormField(fieldConfig, fieldName, {
                    values,
                    totalCount: this.state.values.totalCount,
                    loadMoreRows: this.loadMoreRows,
                    isRowLoaded: this.isRowLoaded,
                    isFirstPageLoaded: this.state.isFirstPageLoaded,
                    updateTotalCount: this.updateTotalCount,
                    removeItem: this.removeItem,
                    updateRow: this.updateRow,
                  }))}
              </div>
              <div className="font-weight-bold float-right mr-5er e mt-1">
                <Translate id="react.default.total.label" defaultMessage="Total" />: {this.state.values.totalValue}
              </div>
              &nbsp;
              <div className="submit-buttons">
                <button
                  onClick={() => this.props.previousPage(this.props.initialValues)}
                  className="btn btn-outline-primary btn-form btn-xs"
                >
                  <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
                </button>
                <button
                  onClick={() => this.nextPage(values)}
                  className="btn btn-outline-primary btn-form float-right btn-xs"
                >
                  <Translate id="react.default.button.next.label" defaultMessage="Next" />
                </button>
              </div>
            </form>
          </div>
        )}
      />
    );
  }
}

const mapStateToProps = state => ({
  pageSize: state.session.pageSize,
});

export default (connect(mapStateToProps, { showSpinner, hideSpinner })(AddItemsPage));

AddItemsPage.propTypes = {
  /** Initial component's data */
  initialValues: PropTypes.shape({}).isRequired,
  /** Function returning user to the previous page */
  previousPage: PropTypes.func.isRequired,
  /** Function taking user to the next page */
  nextPage: PropTypes.func.isRequired,
  /** Number of page size needed for pagination */
  pageSize: PropTypes.number.isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
};
