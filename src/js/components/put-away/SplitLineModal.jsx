import _ from 'lodash';
import React, { Component } from 'react';
import { Tooltip } from 'react-tippy';
import update from 'immutability-helper';
import PropTypes from 'prop-types';
import Modal from 'react-modal';
import { confirmAlert } from 'react-confirm-alert';
import { Translate, getTranslate } from 'react-localize-redux';
import connect from 'react-redux/es/connect/connect';

import 'react-confirm-alert/src/react-confirm-alert.css';
import 'react-tippy/dist/tippy.css';

import Input from '../../utils/Input';
import Select from '../../utils/Select';

/**
 * Modal window where user can split put-away's line. It has details of the line
 * at the top, including total quantity to be put away. After clicking "add line",
 * a new split line is added. User can select a bin and fill in the quantity to add to that bin.
*/
class SplitLineModal extends Component {
  constructor(props) {
    super(props);

    this.state = { splitItems: [], showModal: false };

    this.openModal = this.openModal.bind(this);
    this.onSave = this.onSave.bind(this);
    this.closeModal = this.closeModal.bind(this);
    this.isValid = this.isValid.bind(this);
    this.isBinSelected = this.isBinSelected.bind(this);
  }

  /**
   * Checks if there is still stock in the receiving bin and if there is, an error comes up.
   * If user doesn't want to put away the rest of the line, split line is saved and remaining
   * quantity will appear next time someone starts a put away.
   * @public
   */
  onSave() {
    const putAwayQty = this.calculatePutAwayQty();

    if (putAwayQty < this.props.putawayItem.quantity) {
      confirmAlert({
        title: this.props.translate('message.confirmSplitLine.label'),
        message: this.props.translate('confirmSplitLine.label'),
        buttons: [
          {
            label: this.props.translate('default.button.yes.label'),
          },
          {
            label: this.props.translate('default.button.no.label'),
            onClick: () => this.save(),
          },
        ],
      });
    } else {
      this.save();
    }
  }

  /**
   * Saves split items added by user in this modal.
   * @public
   */
  save() {
    this.props.saveSplitItems(_.map(this.state.splitItems, (item) => {
      if (!item.quantity || item.quantity === '0') {
        return { ...item, delete: true };
      }

      return item;
    }));

    this.closeModal();
  }

  /**
   * Loads existing split items(default one or all added by user).
   * @public
   */
  openModal() {
    let splitItems = [];

    if (this.props.splitItems && this.props.splitItems.length > 0) {
      splitItems = [...this.props.splitItems];
    } else {
      splitItems.push({
        quantity: this.props.putawayItem.quantity,
        putawayFacility: {
          id: this.props.putawayItem.putawayFacility
            ? this.props.putawayItem.putawayFacility.id : null,
        },
        putawayLocation: {
          id: this.props.putawayItem.putawayLocation
            ? this.props.putawayItem.putawayLocation.id : null,
        },
        product: { id: this.props.putawayItem.product.id },
        inventoryItem: { id: this.props.putawayItem.inventoryItem.id },
      });
    }

    this.setState({ splitItems, showModal: true });
  }

  /**
   * Changes state of showModal to false so this modal's window is no longer visible.
   * @public
   */
  closeModal() {
    this.setState({ showModal: false });
  }

  /**
   * Returns true if all split items quantities are not higher than original put-away item quantity.
   * It is needed for validation - there is no way to split lines if quantity added by user is
   * higher than available one.
   * @public
   */
  isValid() {
    const qtySum = this.calculatePutAwayQty();

    return qtySum <= _.toInteger(this.props.putawayItem.quantity);
  }

  /**
   * Sums up quantity added by user to each split line.
   * @public
   */
  calculatePutAwayQty() {
    return _.reduce(this.state.splitItems, (sum, val) =>
      (sum + (!val.delete && val.quantity ? _.toInteger(val.quantity) : 0)), 0);
  }

  /**
   * Returns true if bin location has been selected. It is needed for validation - there is no way
   * to split lines if bin location hasn't been chosen for each line.
   * @public
   */
  isBinSelected() {
    return _.every(this.state.splitItems, splitItem =>
      splitItem.putawayLocation.id);
  }

  render() {
    return (
      <div>
        <button
          type="button"
          className="btn btn-outline-success btn-xs"
          onClick={() => this.openModal()}
        ><Translate id="stockMovement.splitLine.label" />:
        </button>
        <Modal
          isOpen={this.state.showModal}
          onRequestClose={this.closeModal}
          className="modal-content-custom"
          shouldCloseOnOverlayClick={false}
        >
          <div>
            <h3 className="font-weight-bold">{`${this.props.putawayItem.product.productCode} ${this.props.putawayItem.product.name}`}</h3>
            <div className="font-weight-bold"><Translate id="stockMovement.expiry.label" />: {this.props.putawayItem.inventoryItem.expirationDate}</div>
            <div className="font-weight-bold"><Translate id="putAway.totalQty.label" />: {this.props.putawayItem.quantity}</div>
            <div className="font-weight-bold"><Translate id="putAway.putAwayQty.label" />: {this.calculatePutAwayQty()}</div>
          </div>
          <hr />

          <div className="text-center">
            <table className="table table-striped text-center border">
              <thead>
                <tr>
                  <th className="py-1"><Translate id="putAway.putAwayBin.label" />:</th>
                  <th className="py-1"><Translate id="stockMovement.quantity.label" />:</th>
                  <th className="py-1"><Translate id="default.button.delete.label" />:</th>
                </tr>
              </thead>
              <tbody>
                { _.map(this.state.splitItems, (item, index) => (
                  !item.delete &&
                  <tr
                    // eslint-disable-next-line react/no-array-index-key
                    key={index}
                  >
                    <td className={`py-1 ${_.isEmpty(item.putawayLocation.id) ? 'has-error align-middle' : 'align-middle'}`}>
                      <Select
                        options={this.props.bins}
                        objectValue
                        value={item.putawayLocation}
                        onChange={value => this.setState({
                          splitItems: update(this.state.splitItems, {
                            [index]: {
                              putawayLocation: { $set: value },
                            },
                          }),
                        })}
                        className="select-xs"
                      />
                    </td>
                    <td className="py-1 align-middle">
                      <Tooltip
                        // eslint-disable-next-line max-len
                        html={(<div><Translate id="putAway.sumOfAll.label" /></div>)}
                        disabled={this.isValid()}
                        theme="transparent"
                        arrow="true"
                        delay="150"
                        duration="250"
                        hideDelay="50"
                      >
                        <div className={!this.isValid() ? 'has-error' : ''}>
                          <Input
                            type="number"
                            value={item.quantity}
                            onChange={value => this.setState({
                              splitItems: update(this.state.splitItems, {
                                [index]: { quantity: { $set: value } },
                              }),
                            })}
                          />
                        </div>
                      </Tooltip>
                    </td>
                    <td width="120px" className="py-1">
                      <button
                        className="btn btn-outline-danger btn-xs"
                        onClick={() => {
                          if (this.state.splitItems[index].id) {
                            this.setState({
                              splitItems: update(this.state.splitItems, {
                                [index]: { delete: { $set: true } },
                              }),
                            });
                          } else {
                            this.setState({
                              splitItems: update(this.state.splitItems, {
                                $splice: [
                                  [index, 1],
                                ],
                              }),
                            });
                          }
                        }}
                      ><Translate id="default.button.delete.label" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <button
              className="btn btn-outline-success btn-xs"
              onClick={() => this.setState({
                  splitItems: update(this.state.splitItems, {
                    $push: [{
                      quantity: '',
                      putawayFacility: {
                        id: this.props.putawayItem.putawayFacility
                          ? this.props.putawayItem.putawayFacility.id : null,
                      },
                      putawayLocation: { id: null },
                      product: { id: this.props.putawayItem.product.id },
                      inventoryItem: { id: this.props.putawayItem.inventoryItem.id },
                    }],
                  }),
                })}
            ><Translate id="default.button.addLine.label" />
            </button>
          </div>

          <hr />
          <div className="btn-group float-right" role="group">
            <button
              type="button"
              className="btn btn-outline-success btn-sm"
              disabled={!this.isValid() || !this.isBinSelected()}
              onClick={() => this.onSave()}
            ><Translate id="default.button.save.label" />
            </button>
            <button
              type="button"
              className="btn btn-outline-secondary btn-sm"
              onClick={() => this.closeModal()}
            ><Translate id="default.button.cancel.label" />
            </button>
          </div>
        </Modal>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  translate: getTranslate(state.localize),
});

export default connect(mapStateToProps)(SplitLineModal);

SplitLineModal.propTypes = {
  /** Function saving split line's items */
  saveSplitItems: PropTypes.func.isRequired,
  /** Put-away items' data */
  putawayItem: PropTypes.shape({
    /** Product's data */
    product: PropTypes.shape({
      id: PropTypes.string,
      productCode: PropTypes.string,
      name: PropTypes.string,
    }),
    /** Inventory's data */
    inventoryItem: PropTypes.shape({
      id: PropTypes.string,
      expirationDate: PropTypes.string,
    }),
    /** Item's quantity to put away. Can be either string or number. */
    quantity: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number,
    ]),
    /** Facility of put-away's item */
    putawayFacility: PropTypes.shape({
      id: PropTypes.string,
    }),
    /** Location of put-away item's bin */
    putawayLocation: PropTypes.shape({
      id: PropTypes.string,
    }),
  }),
  /** An array of items to split */
  splitItems: PropTypes.arrayOf(PropTypes.shape({})),
  /** An array of available bin locations */
  bins: PropTypes.arrayOf(PropTypes.shape({})),
  translate: PropTypes.func.isRequired,
};

SplitLineModal.defaultProps = {
  putawayItem: {},
  splitItems: [],
  bins: [],
};
