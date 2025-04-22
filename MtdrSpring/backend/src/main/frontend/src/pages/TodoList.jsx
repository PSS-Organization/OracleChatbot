import React, { useState, useEffect } from "react";
import NewItem from "../NewItem";
import DeleteIcon from "@mui/icons-material/Delete";
import { Button, TableBody, CircularProgress } from "@mui/material";
import Moment from "react-moment";
import { API_LIST } from "../API";
import axiosInstance from "../utils/axiosConfig";

function TodoList() {
  const [isLoading, setLoading] = useState(false);
  const [isInserting, setInserting] = useState(false);
  const [items, setItems] = useState([]);
  const [error, setError] = useState();

  const deleteItem = async (deleteId) => {
    try {
      await axiosInstance.delete(`${API_LIST}/${deleteId}`);
      const remainingItems = items.filter((item) => item.id !== deleteId);
      setItems(remainingItems);
    } catch (error) {
      setError(error);
    }
  };

  const toggleDone = async (event, id, description, done) => {
    event.preventDefault();
    try {
      await modifyItem(id, description, done);
      await reloadOneItem(id);
    } catch (error) {
      setError(error);
    }
  };

  const reloadOneItem = async (id) => {
    try {
      const response = await axiosInstance.get(`${API_LIST}/${id}`);
      const items2 = items.map((x) =>
        x.id === id
          ? {
              ...x,
              description: response.data.description,
              done: response.data.done,
            }
          : x
      );
      setItems(items2);
    } catch (error) {
      setError(error);
    }
  };

  const modifyItem = async (id, description, done) => {
    const data = { description, done };
    try {
      const response = await axiosInstance.put(`${API_LIST}/${id}`, data);
      if (!response.data) {
        throw new Error("Something went wrong ...");
      }
      return response.data;
    } catch (error) {
      throw error;
    }
  };

  const addItem = async (text) => {
    console.log("addItem(" + text + ")");
    setInserting(true);
    const data = { description: text };

    try {
      const response = await axiosInstance.post(API_LIST, data);
      const id = response.headers.location;
      const newItem = { id: id, description: text };
      setItems([newItem, ...items]);
      setInserting(false);
    } catch (error) {
      setError(error);
      setInserting(false);
    }
  };

  useEffect(() => {
    setLoading(true);
    const fetchItems = async () => {
      try {
        const response = await axiosInstance.get(API_LIST);
        setItems(response.data);
      } catch (error) {
        setError(error);
      } finally {
        setLoading(false);
      }
    };

    fetchItems();
  }, []);

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
              {items.map(
                (item) =>
                  !item.done && (
                    <tr
                      key={item.id}
                      className="border-b border-gray-700 hover:bg-gray-800"
                    >
                      <td className="py-2 px-4 w-full">{item.description}</td>
                      <td className="py-2 px-4 text-gray-400">
                        <Moment format="MMM Do hh:mm:ss">
                          {item.createdAt}
                        </Moment>
                      </td>
                      <td className="py-2 px-4">
                        <Button
                          variant="contained"
                          className="bg-green-500 text-white hover:bg-green-600"
                          color="success"
                          onClick={(event) =>
                            toggleDone(
                              event,
                              item.id,
                              item.description,
                              !item.done
                            )
                          }
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
          <h2 className="text-xl font-semibold text-gray-300 mb-2">
            Done items
          </h2>
          <table className="w-full">
            <tbody>
              {items.map(
                (item) =>
                  item.done && (
                    <tr
                      key={item.id}
                      className="border-b border-gray-700 hover:bg-gray-800"
                    >
                      <td className="py-2 px-4 w-full">{item.description}</td>
                      <td className="py-2 px-4 text-gray-400">
                        <Moment format="MMM Do hh:mm:ss">
                          {item.createdAt}
                        </Moment>
                      </td>
                      <td className="py-2 px-4">
                        <Button
                          variant="contained"
                          className="bg-yellow-500 text-white hover:bg-yellow-600"
                          onClick={(event) =>
                            toggleDone(
                              event,
                              item.id,
                              item.description,
                              !item.done
                            )
                          }
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
