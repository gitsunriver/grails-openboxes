import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import _ from 'lodash';
import update from 'immutability-helper';
import { getTranslate } from 'react-localize-redux';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';
import DatePicker from 'react-datepicker';
import moment from 'moment';
import Alert from 'react-s-alert';

import 'react-table/react-table.css';
import 'react-datepicker/dist/react-datepicker.css';

import { renderFormField } from '../../utils/form-utils';
import LabelField from '../form-elements/LabelField';
import Select from '../../utils/Select';
import ArrayField from '../form-elements/ArrayField';
import TextField from '../form-elements/TextField';
import Checkbox from '../../utils/Checkbox';
import { showSpinner, hideSpinner } from '../../actions';
import Translate, { translateWithDefaultMessage } from '../../utils/Translate';
import apiClient, { flattenRequest, parseResponse } from '../../utils/apiClient';
import { debounceAvailableItemsFetch } from '../../utils/option-utils';
import renderHandlingIcons from '../../utils/product-handling-icons';

const FIELDS = {
  returnItems: {
    type: ArrayField,
    maxTableHeight: 'calc(100vh - 500px)',
    getDynamicRowAttr: ({ rowValues, translate }) => {
      let className = '';
      let tooltip = '';
      if (rowValues.recalled && rowValues.onHold) {
        className = 'recalled-and-on-hold';
        tooltip = translate('react.outboundReturns.recalledAndOnHold.label');
      } else if (rowValues.recalled) {
        className = 'recalled';
        tooltip = translate('react.outboundReturns.recalled.label');
      } else if (rowValues.onHold) {
        className = 'on-hold';
        tooltip = translate('react.outboundReturns.onHold.label');
      }
      return { className, tooltip };
    },
    fields: {
      checked: {
        fieldKey: '',
        label: '',
        flexWidth: '0.5',
        type: ({
          // eslint-disable-next-line react/prop-types
          rowIndex, fieldValue, selectRow,
        }) => (
          <Checkbox
            id={rowIndex.toString()}
            disabled={false}
            className="ml-4"
            value={fieldValue.checked}
            onChange={value => selectRow(value, rowIndex)}
          />
        ),
      },
      'product.productCode': {
        type: LabelField,
        label: 'react.outboundReturns.productCode.label',
        defaultMessage: 'Code',
        flexWidth: '1',
      },
      'product.name': {
        type: LabelField,
        label: 'react.outboundReturns.productName.label',
        defaultMessage: 'Product',
        flexWidth: '4.5',
        attributes: {
          className: 'text-left ml-1',
          showValueTooltip: true,
        },
      },
      lotNumber: {
        type: LabelField,
        label: 'react.outboundReturns.lot.label',
        defaultMessage: 'Lot',
        flexWidth: '1',
      },
      expirationDate: {
        type: LabelField,
        label: 'react.outboundReturns.expiry.label',
        defaultMessage: 'Expiry',
        flexWidth: '1',
      },
      quantityOnHand: {
        type: LabelField,
        label: 'react.outboundReturns.qoh.label',
        defaultMessage: 'QOH',
        flexWidth: '1',
      },
      originZone: {
        type: LabelField,
        label: 'react.outboundReturns.zone.label',
        defaultMessage: 'Zone',
        flexWidth: '1',
        attributes: {
          showValueTooltip: true,
        },
      },
      'originBinLocation.name': {
        type: LabelField,
        label: 'react.outboundReturns.bin.label',
        defaultMessage: 'Bin Location',
        flexWidth: '1',
        attributes: {
          showValueTooltip: true,
        },
      },
      quantity: {
        type: TextField,
        label: 'react.outboundReturns.quantity.label',
        defaultMessage: 'Qty to Return',
        flexWidth: '1',
        attributes: {
          type: 'number',
        },
        getDynamicAttr: ({
          updateRow, values, rowIndex,
        }) => ({
          onChange: () => updateRow(values, rowIndex),
        }),
      },
    },
  },
};

function validate(values) {
  const errors = {};
  errors.returnItems = [];

  _.forEach(values.returnItems, (item, key) => {
    if (
      item.checked &&
      (
        (_.toInteger(item.quantity) > item.quantityOnHand) ||
        _.toInteger(item.quantity) < 0
      )
    ) {
      errors.returnItems[key] = { quantity: 'react.outboundReturns.errors.quantityToReturn.label' };
    }
  });

  return errors;
}

const INITIAL_STATE = {
  // Filters
  selectedProductId: '',
  selectedLotNumber: '',
  selectedExpirationDate: '',
  selectedBinLocation: '',
  // Selected items for return
  selectedItems: [],
  formValues: { returnItems: [] },
  outboundReturn: {},
  // Options
  bins: [],
};

class AddItemsPage extends Component {
  constructor(props) {
    super(props);
    this.state = INITIAL_STATE;

    this.debounceAvailableItemsFetch = debounceAvailableItemsFetch(
      this.props.debounceTime,
      this.props.minSearchLength,
    );

    this.selectRow = this.selectRow.bind(this);
    this.updateRow = this.updateRow.bind(this);
  }

  componentDidMount() {
    if (this.props.outboundReturnsTranslationsFetched) {
      this.dataFetched = true;
      this.fetchBins();
      this.fetchOutboundReturn();
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.outboundReturnsTranslationsFetched) {
      if (!this.dataFetched) {
        this.dataFetched = true;

        this.fetchBins();
        this.fetchOutboundReturn();
      } else if (this.props.locationId !== nextProps.locationId) {
        this.fetchBins();
        this.fetchOutboundReturn();
      }
    }
  }

  setSelectedProduct(selectedProduct) {
    this.setState({
      selectedProductId: selectedProduct ? selectedProduct.id : '',
    }, () => this.fetchReturnCandidates());
  }

  setSelectedLotNumber(selectedLotNumber) {
    this.setState({
      selectedLotNumber: selectedLotNumber || '',
    }, () => {
      if (selectedLotNumber.length >= this.props.minSearchLength || !selectedLotNumber) {
        return this.fetchReturnCandidates();
      }
      return null;
    });
  }

  setSelectedExpirationDate(selectedExpirationDate) {
    this.setState({
      selectedExpirationDate: selectedExpirationDate ? moment(selectedExpirationDate).format('MM/DD/YYYY') : '',
    }, () => this.fetchReturnCandidates());
  }

  setSelectedBinLocation(selectedBinLocation) {
    this.setState({
      selectedBinLocation: selectedBinLocation || '',
    }, () => this.fetchReturnCandidates());
  }

  fetchOutboundReturn() {
    if (this.props.match.params.outboundReturnId) {
      this.props.showSpinner();
      const url = `/openboxes/api/stockTransfers/${this.props.match.params.outboundReturnId}`;
      apiClient.get(url)
        .then((resp) => {
          const outboundReturn = parseResponse(resp.data.data);
          const returnItems = _.map(
            outboundReturn.stockTransferItems,
            item => ({ ...item, checked: true }),
          );
          this.setState({
            outboundReturn,
            selectedItems: _.chain(returnItems).keyBy('productAvailabilityId').value(),
            formValues: { returnItems },
          }, () => this.props.hideSpinner());
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  fetchReturnCandidates() {
    const { locationId } = this.props;
    const {
      selectedProductId,
      selectedLotNumber,
      selectedExpirationDate,
      selectedBinLocation,
    } = this.state;

    if (
      selectedProductId ||
      selectedLotNumber ||
      selectedExpirationDate ||
      selectedBinLocation
    ) {
      this.props.showSpinner();
      const url = '/openboxes/api/stockTransfers/candidates';
      const payload = {
        productId: selectedProductId,
        lotNumber: selectedLotNumber,
        expirationDate: selectedExpirationDate,
        binLocationId: selectedBinLocation.id,
        locationId,
      };

      apiClient.post(url, payload)
        .then((resp) => {
          const returnItems = _.map(parseResponse(resp.data.data), item => ({
            ...item,
            quantity: this.state.selectedItems[item.productAvailabilityId] ? this.state.selectedItems[item.productAvailabilityId].quantity : '',
            checked: !!this.state.selectedItems[item.productAvailabilityId],
          }));
          this.setState({ formValues: { returnItems } }, () => this.props.hideSpinner());
        })
        .catch(() => this.props.hideSpinner());
    } else if (
      !selectedProductId &&
      !selectedLotNumber &&
      !selectedExpirationDate &&
      !selectedBinLocation
    ) {
      this.setState({ formValues: { returnItems: _.values(this.state.selectedItems) } });
    }
  }

  dataFetched = false;

  saveAndTransition() {
    const errors = validate({ returnItems: _.values(this.state.selectedItems) });
    if (errors && errors.returnItems.length) {
      Alert.error(this.props.translate('react.outboundReturns.errors.quantityToReturn.label'));
      this.setState({
        selectedProductId: '',
        selectedLotNumber: '',
        selectedExpirationDate: '',
        selectedBinLocation: '',
      }, () => this.fetchReturnCandidates());
      return;
    }

    const payload = {
      ...this.state.outboundReturn,
      status: 'APPROVED',
      stockTransferItems: _.values(this.state.selectedItems),
    };
    const url = '/openboxes/api/stockTransfers/';

    apiClient.post(url, flattenRequest(payload))
      .then((resp) => {
        const outboundReturns = resp.data.data;
        this.props.hideSpinner();
        this.props.nextPage(outboundReturns);
      })
      .catch(() => this.props.hideSpinner());
  }

  previousPage(values) {
    this.props.previousPage(values);
  }

  fetchBins() {
    this.props.showSpinner();
    const url = `/openboxes/api/internalLocations?location.id=${this.props.locationId}&locationTypeCode=BIN_LOCATION`;

    const mapBins = bins => (_.chain(bins)
      .map(bin => ({
        value: {
          id: bin.id, name: bin.name, zoneId: bin.zoneId, zoneName: bin.zoneName,
        },
        label: bin.name,
      }))
      .orderBy(['label'], ['asc']).value()
    );

    return apiClient.get(url)
      .then((response) => {
        const binGroups = _.partition(response.data.data, bin => (bin.zoneName));
        const binsWithZone = _.chain(binGroups[0]).groupBy('zoneName')
          .map((value, key) => ({ label: key, options: mapBins(value) }))
          .orderBy(['label'], ['asc'])
          .value();
        const binsWithoutZone = mapBins(binGroups[1]);
        this.setState(
          { bins: [...binsWithZone, ...binsWithoutZone] },
          () => this.props.hideSpinner(),
        );
      })
      .catch(() => this.props.hideSpinner());
  }

  selectRow(value, rowIndex) {
    const { formValues, selectedItems } = this.state;
    let newState = {
      formValues: {
        returnItems: _.map(formValues.returnItems, (item, idx) => {
          if (rowIndex === idx) {
            return {
              ...item,
              checked: value,
              quantity: value ? item.quantityOnHand : '',
            };
          }
          return { ...item };
        }),
      },
    };
    if (!value) {
      delete selectedItems[formValues.returnItems[rowIndex].productAvailabilityId];
      newState = {
        ...newState,
        selectedItems,
      };
    } else {
      newState = {
        ...newState,
        selectedItems: {
          ...selectedItems,
          [formValues.returnItems[rowIndex].productAvailabilityId]: {
            ...formValues.returnItems[rowIndex],
            checked: true,
            quantity: value ? formValues.returnItems[rowIndex].quantityOnHand : '',
          },
        },
      };
    }

    newState = {
      formValues: update(formValues, {
        returnItems: { [rowIndex]: { $set: newState.formValues.returnItems[rowIndex] } },
      }),
      selectedItems: newState.selectedItems,
    };

    this.setState(newState);
  }

  updateRow(values, index) {
    let { selectedItems } = this.state;
    const item = values.returnItems[index];

    if (item.quantity && item.quantity > 0) {
      item.checked = true;
      selectedItems = {
        ...selectedItems,
        [item.productAvailabilityId]: { ...item },
      };
    } else {
      item.checked = false;
      delete selectedItems[item.productAvailabilityId];
    }

    this.setState({
      formValues: update(values, {
        returnItems: { [index]: { $set: item } },
      }),
      selectedItems,
    });
  }

  render() {
    const {
      selectedExpirationDate,
      selectedLotNumber,
      bins,
      selectedBinLocation,
      formValues,
      outboundReturn,
    } = this.state;

    return (
      <Form
        onSubmit={() => this.saveAndTransition()}
        mutators={{ ...arrayMutators }}
        initialValues={formValues}
        validate={validate}
        render={({ handleSubmit, values }) => (
          <div className="d-flex flex-column">
            <div className="d-flex mb-3 justify-content-start align-items-center w-100 combined-shipment-filter">
              <Select
                async
                placeholder={this.props.translate('react.outboundReturns.selectProduct.label', 'Select product...')}
                options={[]}
                classes=""
                showValueltip
                loadOptions={this.debounceAvailableItemsFetch}
                onChange={value => this.setSelectedProduct(value)}
                openOnClick={false}
                autoload={false}
                filterOption={options => options}
                cache={false}
                optionRenderer={option => (
                  <strong style={{ color: option.color ? option.color : 'black' }} className="d-flex align-items-center">
                    {option.label}
                    &nbsp;
                    {renderHandlingIcons(option.value ? option.value.handlingIcons : [])}
                  </strong>
                )}
                valueRenderer={option => (
                  <span className="d-flex align-items-center">
                    <span className="text-truncate">
                      {option.label}
                    </span>
                    &nbsp;
                    {renderHandlingIcons(option ? option.handlingIcons : [])}
                  </span>
                )}
              />
              &nbsp;
              <input
                id="selectedLotNumber"
                name="selectedLotNumber"
                type="text"
                className="form-control my-2"
                placeholder={this.props.translate('react.outboundReturns.enterLotNumber.label', 'Enter lot number...')}
                value={selectedLotNumber}
                onChange={event => this.setSelectedLotNumber(event.target.value)}
              />
              &nbsp;
              <div className="form-element-container returns-date-picker">
                <DatePicker
                  className="form-control"
                  selected={moment(selectedExpirationDate, 'MM/DD/YYYY').isValid() ? moment(selectedExpirationDate, 'MM/DD/YYYY') : null}
                  highlightDates={[!moment(selectedExpirationDate, 'MM/DD/YYYY').isValid() ?
                    moment(new Date(), 'MM/DD/YYYY') : {}]}
                  onChange={date => this.setSelectedExpirationDate(date)}
                  popperClassName="force-on-top"
                  showYearDropdown
                  scrollableYearDropdown
                  dateFormat="MM/DD/YYYY"
                  yearDropdownItemNumber={3}
                  utcOffset={0}
                  placeholderText={this.props.translate('react.outboundReturns.selectExpirationDate.label', 'Select expiration date...')}
                />
              </div>
              &nbsp;
              <Select
                options={bins}
                objectValue
                value={selectedBinLocation || null}
                onChange={value => this.setSelectedBinLocation(value)}
                placeholder={this.props.translate('react.outboundReturns.selectBinLocation.label', 'Select bin location...')}
              />
            </div>
            <form onSubmit={handleSubmit} className="print-mt">
              <div className="table-form">
                {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                  outboundReturnId: this.props.match.params.outboundReturnId,
                  locationId: this.props.locationId,
                  translate: this.props.translate,
                  selectRow: this.selectRow,
                  updateRow: this.updateRow,
                  values,
                }))}
              </div>
              <div className="submit-buttons d-flex justify-content-between">
                <button
                  type="button"
                  onClick={() => this.previousPage(outboundReturn)}
                  className="btn btn-outline-primary btn-form float-right btn-xs"
                ><Translate id="react.replenishment.next.label" defaultMessage="Previous" />
                </button>
                <button
                  type="submit"
                  className="btn btn-outline-primary btn-form float-right btn-xs"
                ><Translate id="react.replenishment.next.label" defaultMessage="Next" />
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
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  outboundReturnsTranslationsFetched: state.session.fetchedTranslations.outboundReturns,
});

export default (connect(mapStateToProps, {
  showSpinner, hideSpinner,
})(AddItemsPage));

AddItemsPage.propTypes = {
  previousPage: PropTypes.func.isRequired,
  nextPage: PropTypes.func.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  outboundReturnsTranslationsFetched: PropTypes.bool.isRequired,
  locationId: PropTypes.string.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  match: PropTypes.shape({
    params: PropTypes.shape({
      outboundReturnId: PropTypes.string,
    }),
  }).isRequired,
};
