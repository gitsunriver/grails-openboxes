import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { defaults } from 'react-chartjs-2';
import { connect } from 'react-redux';
import { SortableContainer } from 'react-sortable-hoc';
import 'react-table/react-table.css';
import {
  addToIndicators,
  fetchIndicators,
  reorderIndicators,
  reloadIndicator,
  fetchNumbersData,
  resetIndicators,
} from '../../actions';
import GraphCard from './GraphCard';
import LoadingNumbers from './LoadingNumbers';
import NumberCard from './NumberCard';
import UnarchiveIndicator from './UnarchivePopout';
import './tablero.scss';

// Disable charts legends by default.
defaults.global.legend = false;
defaults.scale.ticks.beginAtZero = true;

// eslint-disable-next-line no-shadow
const SortableCards = SortableContainer(({ data, reloadIndicator }) => (
  <div className="cardComponent">
    {data.map((value, index) =>
      (value.archived ? null : (
        <GraphCard
          key={`item-${value.id}`}
          index={index}
          cardId={value.id}
          cardMethod={value.method}
          cardTitle={value.title}
          cardType={value.type}
          cardLink={value.link}
          data={value.data}
          reloadIndicator={reloadIndicator}
        />
      )))}
  </div>
));

const NumberCardsRow = ({ data }) => {
  if (data) {
    return (
      <div className="cardComponent">
        {data.map((value, index) => (
          <NumberCard
            key={`item-${value.id}`}
            index={index}
            cardTitle={value.title}
            cardNumber={value.number}
            cardSubtitle={value.subtitle}
            cardLink={value.link}
          />
        ))}
      </div>
    );
  }
  return (
    <LoadingNumbers />
  );
};


const ArchiveIndicator = ({ hideArchive }) => (
  <div className={hideArchive ? 'archiveDiv hideArchive' : 'archiveDiv'}>
    <span>
      Archive indicator <i className="fa fa-archive" />
    </span>
  </div>
);


class Tablero extends Component {
  constructor(props) {
    super(props);

    this.state = {
      isDragging: false,
      showPopout: false,
    };
  }

  componentDidMount() {
    this.fetchData();
  }

  componentDidUpdate(prevProps) {
    const prevLocation = prevProps.currentLocation;
    const newLocation = this.props.currentLocation;
    if (prevLocation !== '' && prevLocation !== newLocation) {
      this.fetchData();
    }
  }
  dataFetched = false;

  fetchData() {
    this.props.resetIndicators();
    this.props.fetchIndicators();
    this.props.fetchNumbersData();
  }

  sortStartHandle = () => {
    this.setState({ isDragging: true });
  };

  sortEndHandle = ({ oldIndex, newIndex }, e) => {
    const maxHeight = window.innerHeight - (((6 * window.innerHeight) / 100) + 80);
    if (e.clientY > maxHeight) {
      e.target.id = 'archive';
    }
    this.props.reorderIndicators({ oldIndex, newIndex }, e);
    this.setState({ isDragging: false });
  };

  unarchiveHandler = () => {
    const size = this.props.indicatorsData.filter(data => data.archived).length;
    if (size) this.setState({ showPopout: !this.state.showPopout });
    else this.setState({ showPopout: false });
  };

  handleAdd = (index) => {
    this.props.addToIndicators(index);
    const size = this.props.indicatorsData.filter(data => data.archived).length - 1;
    if (size) this.setState({ showPopout: true });
    else this.setState({ showPopout: false });
  };

  render() {
    return (
      <div className="cardsContainer">
        <NumberCardsRow data={this.props.numberData} />
        <SortableCards
          data={this.props.indicatorsData}
          onSortStart={this.sortStartHandle}
          onSortEnd={this.sortEndHandle}
          reloadIndicator={this.props.reloadIndicator}
          axis="xy"
          useDragHandle
        />
        <ArchiveIndicator hideArchive={!this.state.isDragging} />
        <UnarchiveIndicator
          data={this.props.indicatorsData}
          showPopout={this.state.showPopout}
          unarchiveHandler={this.unarchiveHandler}
          handleAdd={this.handleAdd}
        />
      </div>
    );
  }
}

const mapStateToProps = state => ({
  indicatorsData: state.indicators.data,
  numberData: state.indicators.numberData,
  currentLocation: state.session.currentLocation.id,
});

export default connect(mapStateToProps, {
  fetchIndicators,
  reloadIndicator,
  addToIndicators,
  reorderIndicators,
  fetchNumbersData,
  resetIndicators,
})(Tablero);

Tablero.defaultProps = {
  currentLocation: '',
  indicatorsData: null,
  numberData: [],
};

Tablero.propTypes = {
  fetchIndicators: PropTypes.func.isRequired,
  reorderIndicators: PropTypes.func.isRequired,
  indicatorsData: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  numberData: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  addToIndicators: PropTypes.func.isRequired,
  reloadIndicator: PropTypes.func.isRequired,
  fetchNumbersData: PropTypes.func.isRequired,
  resetIndicators: PropTypes.func.isRequired,
  currentLocation: PropTypes.string.isRequired,
};

NumberCardsRow.defaultProps = {
  data: null,
};

NumberCardsRow.propTypes = {
  data: PropTypes.arrayOf(PropTypes.shape({})),
};

ArchiveIndicator.propTypes = {
  hideArchive: PropTypes.bool.isRequired,
};
