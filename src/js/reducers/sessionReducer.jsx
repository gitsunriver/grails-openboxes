import _ from 'lodash';

import { FETCH_SESSION_INFO, CHANGE_CURRENT_LOCATION } from '../actions/types';

const initialState = {
  currentLocation: {
    id: '',
    name: '',
    hasBinLocationSupport: true,
    locationType: { description: '', locationTypeCode: '' },
  },
  isSuperuser: false,
  isUserAdmin: false,
  supportedActivities: [],
  menuConfig: {},
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_SESSION_INFO:
      return {
        ...state,
        currentLocation: _.get(action, 'payload.data.data.location'),
        isSuperuser: _.get(action, 'payload.data.data.isSuperuser'),
        isUserAdmin: _.get(action, 'payload.data.data.isUserAdmin'),
        supportedActivities: _.get(action, 'payload.data.data.supportedActivities'),
        menuConfig: _.get(action, 'payload.data.data.menuConfig'),
      };
    case CHANGE_CURRENT_LOCATION:
      return { ...state, currentLocation: action.payload };
    default:
      return state;
  }
}
