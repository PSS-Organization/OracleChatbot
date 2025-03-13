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
import { getBackendUrl } from "./utils/getBackendUrl";

export const API_LIST = '/todolist';
export const API_SIGNUP = `${await getBackendUrl()}/usuarios/signup`; // Ahora usa la URL correcta
export const API_USERS = '/usuarios/all';

export const API_TAREAS = `${await getBackendUrl()}/tareas`;  
// import { API_LIST, API_USERS } from './API.jsx';

// // Usar API_LIST y API_USERS en tu código
// fetch(API_LIST)
//     .then(response => response.json())
//     .then(data => console.log(data));

// fetch(API_USERS)
//     .then(response => response.json())
//     .then(data => console.log(data));
