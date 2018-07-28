import _ from 'lodash';
import React, { Component } from 'react';
import { reduxForm, initialize, change, formValueSelector } from 'redux-form';
import { connect } from 'react-redux';
import update from 'immutability-helper';
import PropTypes from 'prop-types';

import TextField from '../form-elements/TextField';
import SelectField from '../form-elements/SelectField';
import ArrayField from '../form-elements/ArrayField';
import ButtonField from '../form-elements/ButtonField';
import LabelField from '../form-elements/LabelField';
import DateField from '../form-elements/DateField';
import TableRowWithSubfields from '../form-elements/TableRowWithSubfields';
import { renderFormField } from '../../utils/form-utils';
import Select from '../../utils/Select';
import Checkbox from '../../utils/Checkbox';
import { USERNAMES_MOCKS, BIN_LOCATION_MOCKS, RECEIPT_MOCKS } from '../../mockedData';

const isReceiving = (subfield, fieldValue) => {
  if (subfield) {
    return !_.isNil(_.get(fieldValue, 'receiveItem.quantity'));
  }

  if (!fieldValue.shipmentItems) {
    return false;
  }

  return _.every(fieldValue.shipmentItems, item => !_.isNil(_.get(item, 'receiveItem.quantity')));
};

const isIndeterminate = (subfield, fieldValue) => {
  if (subfield) {
    return false;
  }

  if (!fieldValue.shipmentItems) {
    return false;
  }

  return _.some(fieldValue.shipmentItems, item => !_.isNil(_.get(item, 'receiveItem.quantity')))
    && _.some(fieldValue.shipmentItems, item => _.isNil(_.get(item, 'receiveItem.quantity')));
};

const FIELDS = {
  'origin.name': {
    type: LabelField,
    label: 'Origin',
  },
  'destination.name': {
    type: LabelField,
    label: 'Destination',
  },
  actualShippingDate: {
    type: LabelField,
    label: 'Shipped On',
  },
  actualDeliveredDate: {
    type: DateField,
    label: 'Delivered On',
    attributes: {
      dateFormat: 'MM/DD/YYYY',
    },
  },
  buttons: {
    // eslint-disable-next-line react/prop-types
    type: ({ autofillLines }) => (
      <div className="mb-3 d-flex justify-content-center">
        <button type="button" className="btn btn-outline-primary mr-3" onClick={() => autofillLines()}>
        Autofill quantities
        </button>
        <button type="button" className="btn btn-outline-primary mr-3">Save</button>
        <button type="submit" className="btn btn-outline-primary">Next</button>
      </div>),
  },
  containers: {
    type: ArrayField,
    rowComponent: TableRowWithSubfields,
    subfieldKey: 'shipmentItems',
    fields: {
      autofillLine: {
        fieldKey: '',
        type: ({
          // eslint-disable-next-line react/prop-types
          subfield, parentIndex, rowIndex, autofillLines, fieldPreview, fieldValue,
        }) => (
          <Checkbox
            disabled={fieldPreview}
            className={subfield ? 'ml-4' : ''}
            value={isReceiving(subfield, fieldValue)}
            indeterminate={isIndeterminate(subfield, fieldValue)}
            onChange={(value) => {
              if (subfield) {
                autofillLines(!value, parentIndex, rowIndex);
              } else {
                autofillLines(!value, rowIndex);
              }
            }}
          />),
      },
      name: {
        type: params => (!params.subfield ? <LabelField {...params} /> : null),
        label: 'Packaging Unit',
        attributes: {
          formatValue: value => (value || 'Unpacked'),
        },
      },
      'inventoryItem.product.productCode': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Code',
      },
      'inventoryItem.product.name': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Product',
      },
      'inventoryItem.lotNumber': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Lot/Serial No',
      },
      'inventoryItem.expirationDate': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Expiration Date',
      },
      quantity: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Shipped',
      },
      received: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Received',
        attributes: {
          formatValue: value => (value || '0'),
        },
      },
      'receiveItem.quantity': {
        type: params => (params.subfield ? <TextField {...params} /> : null),
        label: 'To Receive',
      },
      'receiveItem.binLocation': {
        type: params => (
          params.subfield ?
            <SelectField {...params} /> :
            <Select
              disabled={params.fieldPreview}
              options={BIN_LOCATION_MOCKS}
              onChange={value => params.setLocation(params.rowIndex, value)}
            />),
        label: 'Bin Location',
        attributes: {
          options: BIN_LOCATION_MOCKS,
        },
      },
      edit: {
        type: params => (params.subfield ? <ButtonField {...params} /> : null),
        label: '',
        buttonLabel: 'Edit Line',
        attributes: {
          className: 'btn btn-outline-primary',
        },
      },
      'receiveItem.recipient': {
        type: params => (params.subfield ? <SelectField {...params} /> : null),
        label: 'Recipient',
        attributes: {
          options: USERNAMES_MOCKS,
        },
      },
    },
  },
};

class PartialReceivingPage extends Component {
  static autofillLine(clearValue, shipmentItem) {
    return {
      ...shipmentItem,
      receiveItem: {
        ...shipmentItem.receiveItem,
        quantity: clearValue ? null
          : _.toInteger(shipmentItem.quantity) - _.toInteger(shipmentItem.received),
      },
    };
  }

  constructor(props) {
    super(props);

    this.autofillLines = this.autofillLines.bind(this);
    this.setLocation = this.setLocation.bind(this);
  }

  componentDidMount() {
    this.props.initialize('partial-receiving-wizard', RECEIPT_MOCKS, true);
  }

  setLocation(rowIndex, location) {
    if (this.props.containers && !_.isNil(rowIndex)) {
      const containers = update(this.props.containers, {
        [rowIndex]: {
          shipmentItems: {
            $apply: items => (!items ? [] : items.map(item => ({
              ...item,
              receiveItem: {
                ...item.receiveItem,
                binLocation: location,
              },
            }))),
          },
        },
      });

      this.props.change('partial-receiving-wizard', 'containers', containers);
    }
  }

  autofillLines(clearValue, parentIndex, rowIndex) {
    if (this.props.containers) {
      let containers = [];

      if (_.isNil(parentIndex)) {
        containers = update(this.props.containers, {
          $apply: items => (!items ? [] : items.map(item => update(item, {
            shipmentItems: {
              $apply: shipmentItems => (!shipmentItems ? [] : shipmentItems.map(shipmentItem =>
                PartialReceivingPage.autofillLine(clearValue, shipmentItem))),
            },
          }))),
        });
      } else if (_.isNil(rowIndex)) {
        containers = update(this.props.containers, {
          [parentIndex]: {
            shipmentItems: {
              $apply: items => (!items ? [] : items.map(item =>
                PartialReceivingPage.autofillLine(clearValue, item))),
            },
          },
        });
      } else {
        containers = update(this.props.containers, {
          [parentIndex]: {
            shipmentItems: {
              [rowIndex]: {
                $apply: item => PartialReceivingPage.autofillLine(clearValue, item),
              },
            },
          },
        });
      }

      this.props.change('partial-receiving-wizard', 'containers', containers);
    }
  }

  nextPage(formValues) {
    const containers = _.map(formValues.containers, container => ({
      ...container,
      shipmentItems: _.filter(container.shipmentItems, item => !_.isNil(_.get(item, 'receiveItem.quantity'))),
    }));
    this.props.change('partial-receiving-wizard', 'containers', _.filter(containers, container => container.shipmentItems.length));
    this.props.onSubmit();
  }

  render() {
    const { handleSubmit } = this.props;
    return (
      <form onSubmit={handleSubmit(values => this.nextPage(values))}>
        {_.map(FIELDS, (fieldConfig, fieldName) =>
          renderFormField(fieldConfig, fieldName, {
            autofillLines: this.autofillLines,
            setLocation: this.setLocation,
          }))}
      </form>
    );
  }
}

const selector = formValueSelector('partial-receiving-wizard');

const mapStateToProps = state => ({
  containers: selector(state, 'containers'),
});

export default reduxForm({
  form: 'partial-receiving-wizard',
  destroyOnUnmount: false,
  forceUnregisterOnUnmount: true,
})(connect(mapStateToProps, { initialize, change })(PartialReceivingPage));

PartialReceivingPage.propTypes = {
  initialize: PropTypes.func.isRequired,
  change: PropTypes.func.isRequired,
  handleSubmit: PropTypes.func.isRequired,
  onSubmit: PropTypes.func.isRequired,
  containers: PropTypes.arrayOf(PropTypes.shape({})),
};

PartialReceivingPage.defaultProps = {
  containers: [],
};
