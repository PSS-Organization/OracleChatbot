import React, { useState, useEffect } from 'react';
import NewItem from '../NewItem';
import DeleteIcon from '@mui/icons-material/Delete';
import { Button, TableBody, CircularProgress } from '@mui/material';
import Moment from 'react-moment';
import { API_LIST } from '../API';


function TodoList() {
    // isLoading is true while waiting for the backend to return the list
    // of items. We use this state to display a spinning circle:
    const [isLoading, setLoading] = useState(false);
    // Similar to isLoading, isInserting is true while waiting for the backend
    // to insert a new item:
    const [isInserting, setInserting] = useState(false);
    // The list of todo items is stored in this state. It includes the "done"
    // "not-done" items:
    const [items, setItems] = useState([]);
    // In case of an error during the API call:
    const [error, setError] = useState();

    function deleteItem(deleteId) {
        // console.log("deleteItem("+deleteId+")")
        fetch(API_LIST + "/" + deleteId, {
            method: 'DELETE',
        })
            .then(response => {
                // console.log("response=");
                // console.log(response);
                if (response.ok) {
                    // console.log("deleteItem FETCH call is ok");
                    return response;
                } else {
                    throw new Error('Something went wrong ...');
                }
            })
            .then(
                (result) => {
                    const remainingItems = items.filter(item => item.id !== deleteId);
                    setItems(remainingItems);
                },
                (error) => {
                    setError(error);
                }
            );
    }
    function toggleDone(event, id, description, done) {
        event.preventDefault();
        modifyItem(id, description, done).then(
            (result) => { reloadOneIteam(id); },
            (error) => { setError(error); }
        );
    }
    function reloadOneIteam(id) {
        fetch(API_LIST + "/" + id)
            .then(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    throw new Error('Something went wrong ...');
                }
            })
            .then(
                (result) => {
                    const items2 = items.map(
                        x => (x.id === id ? {
                            ...x,
                            'description': result.description,
                            'done': result.done
                        } : x));
                    setItems(items2);
                },
                (error) => {
                    setError(error);
                });
    }
    function modifyItem(id, description, done) {
        // console.log("deleteItem("+deleteId+")")
        var data = { "description": description, "done": done };
        return fetch(API_LIST + "/" + id, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        })
            .then(response => {
                // console.log("response=");
                // console.log(response);
                if (response.ok) {
                    // console.log("deleteItem FETCH call is ok");
                    return response;
                } else {
                    throw new Error('Something went wrong ...');
                }
            });
    }
    /*
    To simulate slow network, call sleep before making API calls.
    const sleep = (milliseconds) => {
      return new Promise(resolve => setTimeout(resolve, milliseconds))
    }
    */
    useEffect(() => {
        setLoading(true);
        // sleep(5000).then(() => {
        fetch(API_LIST)
            .then(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    throw new Error('Something went wrong ...');
                }
            })
            .then(
                (result) => {
                    setLoading(false);
                    setItems(result);
                },
                (error) => {
                    setLoading(false);
                    setError(error);
                });

        //})
    },
        // https://en.reactjs.org/docs/faq-ajax.html
        [] // empty deps array [] means
        // this useEffect will run once
        // similar to componentDidMount()
    );
    function addItem(text) {
        console.log("addItem(" + text + ")")
        setInserting(true);
        var data = {};
        console.log(data);
        data.description = text;
        fetch(API_LIST, {
            method: 'POST',
            // We convert the React state to JSON and send it as the POST body
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data),
        }).then((response) => {
            // This API doens't return a JSON document
            console.log(response);
            console.log();
            console.log(response.headers.location);
            // return response.json();
            if (response.ok) {
                return response;
            } else {
                throw new Error('Something went wrong ...');
            }
        }).then(
            (result) => {
                var id = result.headers.get('location');
                var newItem = { "id": id, "description": text }
                setItems([newItem, ...items]);
                setInserting(false);
            },
            (error) => {
                setInserting(false);
                setError(error);
            }
        );
    }
    return (
        <div className="bg-gray-900 text-white flex flex-col items-center p-6 rounded-lg shadow-lg w-full max-w-3xl mx-auto mt-8">
            <h1 className="text-2xl font-bold mb-4">MY TODO LIST</h1>
            <NewItem addItem={addItem} isInserting={isInserting} />
            {error && <p className="text-red-500">Error: {error.message}</p>}
            {isLoading && <CircularProgress className="text-white" />}
            {!isLoading && (
                <div className="w-full">
                    <table className="w-full mb-6">
                        <tbody>
                            {items.map((item) =>
                                !item.done && (
                                    <tr key={item.id} className="border-b border-gray-700 hover:bg-gray-800">
                                        <td className="py-2 px-4 w-full">{item.description}</td>
                                        <td className="py-2 px-4 text-gray-400">
                                            <Moment format="MMM Do hh:mm:ss">{item.createdAt}</Moment>
                                        </td>
                                        <td className="py-2 px-4">
                                            <Button
                                                variant="contained"
                                                className="bg-green-500 text-white hover:bg-green-600"
                                                onClick={(event) => toggleDone(event, item.id, item.description, !item.done)}
                                                size="small"
                                            >
                                                Done
                                            </Button>
                                        </td>
                                    </tr>
                                )
                            )}
                        </tbody>
                    </table>
                    <h2 className="text-xl font-semibold text-gray-300 mb-2">Done items</h2>
                    <table className="w-full">
                        <tbody>
                            {items.map((item) =>
                                item.done && (
                                    <tr key={item.id} className="border-b border-gray-700 hover:bg-gray-800">
                                        <td className="py-2 px-4 w-full">{item.description}</td>
                                        <td className="py-2 px-4 text-gray-400">
                                            <Moment format="MMM Do hh:mm:ss">{item.createdAt}</Moment>
                                        </td>
                                        <td className="py-2 px-4">
                                            <Button
                                                variant="contained"
                                                className="bg-yellow-500 text-white hover:bg-yellow-600"
                                                onClick={(event) => toggleDone(event, item.id, item.description, !item.done)}
                                                size="small"
                                            >
                                                Undo
                                            </Button>
                                        </td>
                                        <td className="py-2 px-4">
                                            <Button
                                                startIcon={<DeleteIcon />}
                                                variant="contained"
                                                className="bg-red-500 text-white hover:bg-red-600"
                                                onClick={() => deleteItem(item.id)}
                                                color="error"
                                                size="small"
                                            >
                                                Delete
                                            </Button>
                                        </td>
                                    </tr>
                                )
                            )}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}
export default TodoList;