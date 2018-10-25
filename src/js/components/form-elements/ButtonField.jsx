import React from 'react';
import PropTypes from 'prop-types';

const ButtonField = (props) => {
  const {
    fieldName,
    fieldConfig: { buttonLabel: ButtonLabel, getDynamicAttr, attributes = { className: 'btn-outline-primary' } },
    fieldPreview,
  } = props;
  const dynamicAttr = getDynamicAttr ? getDynamicAttr(props) : {};
  const attr = { disabled: fieldPreview, ...attributes, ...dynamicAttr };

  return (
    <button type="button" key={fieldName} {...attr} className={`btn btn-xs ${attr.className}`} >
      {
        typeof ButtonLabel === 'string' ? ButtonLabel : <ButtonLabel />
      }
    </button>
  );
};

export default ButtonField;

ButtonField.propTypes = {
  fieldName: PropTypes.string.isRequired,
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
  fieldPreview: PropTypes.bool,
};

ButtonField.defaultProps = {
  fieldPreview: false,
};
