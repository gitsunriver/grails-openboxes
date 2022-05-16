import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import moment from 'moment';

import CreateStockMovement from './CreateStockMovement';
import AddItemsPage from './AddItemsPage';
import EditPage from './EditPage';
import PickPage from './PickPage';
import SendMovementPage from './SendMovementPage';
import WizardSteps from '../form-elements/WizardSteps';
import apiClient from '../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../actions';

/** Main stock movement form's wizard component. */
class StockMovements extends Component {
  constructor(props) {
    super(props);

    this.state = {
      page: 1,
      prevPage: 1,
      values: this.props.initialValues,
    };

    this.nextPage = this.nextPage.bind(this);
    this.previousPage = this.previousPage.bind(this);
    this.goToPage = this.goToPage.bind(this);
  }

  componentDidMount() {
    this.fetchInitialValues();
  }

  /**
   * Returns array of form steps.
   * @public
   */
  static getStepList() {
    return ['Create', 'Add items', 'Edit', 'Pick', 'Send'];
  }

  /**
   * Returns array of form's components.
   * @public
   */
  getFormList() {
    return [
      <CreateStockMovement
        initialValues={this.state.values}
        onSubmit={this.nextPage}
      />,
      <AddItemsPage
        initialValues={this.state.values}
        previousPage={this.previousPage}
        goToPage={this.goToPage}
        onSubmit={this.nextPage}
      />,
      <EditPage
        initialValues={this.state.values}
        previousPage={this.previousPage}
        onSubmit={this.nextPage}
      />,
      <PickPage
        initialValues={this.state.values}
        previousPage={this.previousPage}
        onSubmit={this.nextPage}
      />,
      <SendMovementPage
        initialValues={this.state.values}
        previousPage={this.previousPage}
      />,
    ];
  }

  /**
   * Returns shipment's name containing shipment's origin, destination, requisition date,
   * tracking number given by user on the last step, description and stock list if chosen.
   * @public
   */
  getShipmentName() {
    if (this.state.values.trackingNumber) {
      const {
        origin, destination, dateRequested, stockList, trackingNumber, description,
      } = this.state.values;
      const stocklistPart = stockList && stockList.name ? `${stockList.name}.` : '';
      const dateReq = moment(dateRequested, 'MM/DD/YYYY').format('DDMMMYYYY');
      const newName = `${origin.name}.${destination.name}.${dateReq}.${stocklistPart}${trackingNumber}.${description}`;
      return newName.replace(/ /gi, '');
    }
    return this.state.values.shipmentName;
  }

  /**
   * Fetches initial values from API.
   * @public
   */
  fetchInitialValues() {
    if (this.props.match.params.stockMovementId) {
      this.props.showSpinner();
      const url = `/openboxes/api/stockMovements/${this.props.match.params.stockMovementId}`;

      apiClient.get(url)
        .then((response) => {
          const resp = response.data.data;
          const originType = resp.origin.locationType;
          const destinationType = resp.destination.locationType;
          const values = {
            ...resp,
            stockMovementId: resp.id,
            movementNumber: resp.identifier,
            shipmentName: resp.name,
            origin: {
              id: resp.origin.id,
              type: originType ? originType.locationTypeCode : null,
              name: resp.origin.name,
              label: `${resp.origin.name} [${originType ? originType.description : null}]`,
            },
            destination: {
              id: resp.destination.id,
              type: destinationType ? destinationType.locationTypeCode : null,
              name: resp.destination.name,
              label: `${resp.destination.name} [${destinationType ? destinationType.description : null}]`,
            },
            requestedBy: {
              id: resp.requestedBy.id,
              name: resp.requestedBy.name,
              label: resp.requestedBy.name,
            },
          };

          let page = 1;
          let prevPage = 1;
          switch (values.statusCode) {
            case 'CREATED':
              page = 2;
              prevPage = 1;
              break;
            case 'VERIFYING':
              page = 3;
              prevPage = 2;
              break;
            case 'PICKING':
              page = 4;
              prevPage = 3;
              break;
            default:
              page = 5;
              if (values.origin.type === 'SUPPLIER') {
                prevPage = 2;
              } else {
                prevPage = 4;
              }
          }
          this.setState({ values, page, prevPage });
          this.fetchBins();
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  /**
   * Sets current page state as a previous page and takes user to the next page.
   * @param {object} values
   * @public
   */
  nextPage(values) {
    this.setState({ prevPage: this.state.page, page: this.state.page + 1, values });
  }

  /**
   * Returns user to the previous page.
   * @param {object} values
   * @public
   */
  previousPage(values) {
    this.setState({ prevPage: this.state.prevPage - 1, page: this.state.prevPage, values });
  }

  /**
   * Sets current page state as a previous page and takes user to the given number page.
   * @param {object} values
   * @param {number} page
   * @public
   */
  goToPage(page, values) {
    this.setState({ prevPage: this.state.page, page, values });
  }

  render() {
    const { page, values } = this.state;

    const formList = this.getFormList();

    return (
      <div>
        <div>
          <WizardSteps steps={StockMovements.getStepList()} currentStep={page} />
        </div>
        <div className="panel panel-primary">
          <div className="panel-heading movement-number">
            {(values.movementNumber && values.shipmentName && !values.trackingNumber) &&
              <span>{`${values.movementNumber} - ${values.shipmentName}`}</span>
            }
            {values.trackingNumber &&
              <span>{`${values.movementNumber} - ${this.getShipmentName()}`}</span>
            }
          </div>
          <div className="panelBody px-1">
            {formList[page - 1]}
          </div>
        </div>
      </div>
    );
  }
}

export default connect(null, { showSpinner, hideSpinner })(StockMovements);

StockMovements.propTypes = {
  /** React router's object which contains information about url varaiables and params */
  match: PropTypes.shape({
    params: PropTypes.shape({ stockMovementId: PropTypes.string }),
  }).isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Initial components' data */
  initialValues: PropTypes.shape({}),
};

StockMovements.defaultProps = {
  initialValues: {},
};
