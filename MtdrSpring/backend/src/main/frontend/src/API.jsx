/*
## MyToDoReact (Springboot) version 1.0.
##
## Copyright (c) 2022 Oracle, Inc.
## Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
*/
/*
 * The swagger definition of the APIs can be found here:
 * https://objectstorage.eu-frankfurt-1.oraclecloud.com/n/oracleonpremjava/b/todolist/o/swagger_APIs_definition.json
 *
 * You can view it in swagger-ui by going to the following petstore swagger ui page and
 * pasting the URL above that points to the definitions of the APIs that are used in this app:
 * https://petstore.swagger.io/
 * @author  jean.de.lavarene@oracle.com
 */
// Copy from the endpoint from the API Gateway Deployment
// Example: const API_LIST = 'https://di2eyonlz5s7kmuektcddaw5zq.apigateway.<region>.oci.customer-oci.com/todolist';
// const API_LIST = 'https://di2eyonlz5s7kmuektcddaw5zq.apigateway.eu-frankfurt-1.oci.customer-oci.com/todolist';
import axiosInstance from "./utils/axiosConfig";
import { getBackendUrl } from "./utils/getBackendUrl";

// API Endpoints
export const API_LIST = "/todolist";
export const API_LOGIN = "/usuarios/login";
export const API_SIGNUP = "/usuarios/signup";
export const API_TAREAS = "/tareas";
export const API_SPRINTS = "/sprints";

// Auth functions
export const login = async (credentials) => {
  const backendUrl = await getBackendUrl();
  try {
    const response = await axiosInstance.post(
      `${backendUrl}${API_LOGIN}`,
      credentials
    );
    return response.data;
  } catch (error) {
    throw new Error(error.response?.data?.message || "Login failed");
  }
};

export const signup = async (userData) => {
  const backendUrl = await getBackendUrl();
  try {
    const response = await axiosInstance.post(
      `${backendUrl}${API_SIGNUP}`,
      userData
    );
    return response.data;
  } catch (error) {
    throw new Error(error.response?.data?.message || "Signup failed");
  }
};

// Tasks functions
export const getTareas = async () => {
  const backendUrl = await getBackendUrl();
  try {
    const response = await axiosInstance.get(`${backendUrl}${API_TAREAS}`);
    return response.data;
  } catch (error) {
    throw new Error("Failed to fetch tasks");
  }
};

export const createTarea = async (tarea) => {
  const backendUrl = await getBackendUrl();
  try {
    const response = await axiosInstance.post(
      `${backendUrl}${API_TAREAS}`,
      tarea
    );
    return response.data;
  } catch (error) {
    throw new Error("Failed to create task");
  }
};

export const updateTarea = async (id, tarea) => {
  const backendUrl = await getBackendUrl();
  try {
    const response = await axiosInstance.put(
      `${backendUrl}${API_TAREAS}/${id}`,
      tarea
    );
    return response.data;
  } catch (error) {
    throw new Error("Failed to update task");
  }
};

export const deleteTarea = async (id) => {
  const backendUrl = await getBackendUrl();
  try {
    const response = await axiosInstance.delete(
      `${backendUrl}${API_TAREAS}/${id}`
    );
    return response.data;
  } catch (error) {
    throw new Error("Failed to delete task");
  }
};

// Sprints functions
export const getSprints = async () => {
  const backendUrl = await getBackendUrl();
  try {
    const response = await axiosInstance.get(`${backendUrl}${API_SPRINTS}`);
    return response.data;
  } catch (error) {
    throw new Error("Failed to fetch sprints");
  }
};

export const createSprint = async (sprint) => {
  const backendUrl = await getBackendUrl();
  try {
    const response = await axiosInstance.post(
      `${backendUrl}${API_SPRINTS}`,
      sprint
    );
    return response.data;
  } catch (error) {
    throw new Error("Failed to create sprint");
  }
};

export const updateSprint = async (id, sprint) => {
  const backendUrl = await getBackendUrl();
  try {
    const response = await axiosInstance.put(
      `${backendUrl}${API_SPRINTS}/${id}`,
      sprint
    );
    return response.data;
  } catch (error) {
    throw new Error("Failed to update sprint");
  }
};

export const deleteSprint = async (id) => {
  const backendUrl = await getBackendUrl();
  try {
    const response = await axiosInstance.delete(
      `${backendUrl}${API_SPRINTS}/${id}`
    );
    return response.data;
  } catch (error) {
    throw new Error("Failed to delete sprint");
  }
};
