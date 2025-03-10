import React, { useState } from "react";
import Button from '@mui/material/Button';


function NewItem(props) {
  const [item, setItem] = useState('');
  function handleSubmit(e) {
    // console.log("NewItem.handleSubmit("+e+")");
    if (!item.trim()) {
      return;
    }
    // addItem makes the REST API call:
    props.addItem(item);
    setItem("");
    e.preventDefault();
  }
  function handleChange(e) {
    setItem(e.target.value);
  }
  return (
    <div id="newinputform">
      <form>
        <input
          id="newiteminput"
          placeholder="New item"
          type="text"
          autoComplete="off"
          value={item}
          onChange={handleChange}
          className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 p-1 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
          // No need to click on the "ADD" button to add a todo item. You
          // can simply press "Enter":
          onKeyDown={event => {
            if (event.key === 'Enter') {
              handleSubmit(event);
            }
          }}
        />
        <span>&nbsp;&nbsp;</span>
        <Button
          className="AddButton"
          variant="contained"
          disabled={props.isInserting}
          onClick={!props.isInserting ? handleSubmit : null}
          size="small"
        >
          {props.isInserting ? 'Adding…' : 'Add'}
        </Button>
      </form>
    </div>
  );
}

export default NewItem;