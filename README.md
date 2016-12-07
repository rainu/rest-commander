# rest-commander
A Rest-Application which can execute and manage processes.

# API

- __Authentication__
    1. [Login](#login)
    2. [Logout](#logout)

- __Process__
    1. [List Process](#list-process)
    2. [Start Process](#start-process)
    3. [Start Process (as admin)](#start-process-as-admin)
    4. [Signal](#signal)
    5. [Input](#input)
    6. [Status](#status)
    7. [Output](#output)

## Login

  _Login as a user._

* **URL**

  _/auth/login_

* **Method:**

  _POST_
  
* **Header:**

  **Required:**
  
  `Content-Type: application/json`

* **Data Params**

  ```json
  {
    "username": "root",
    "password": "toor"
  }
  ```

* **Success Response:**
  
  * **Code:** 200 <br />
    **Content:** 
    ```json
    { 
      "token":"<token>"
    }
    ```
 
* **Error Response:**
  
  * **Code:** 400 Bad Request <br/>
    **Content:** 
    ```json
    {
      "message":"Username or password are incorrect!"
    }
    ```

* **Sample Call:**

  ```bash
  curl -X POST -H 'Content-Type: application/json' http://localhost:8080/auth/login --data '{"username": "root", "password": "toor"}'
  ```

## Logout

  _Logout a user._

* **URL**

  _/auth/logout_

* **Method:**

  _POST_
  
* **Header:**

  **Required:**
  
  `x-auth-token=[token]`

* **Success Response:**
  
  * **Code:** 200 
 
* **Error Response:**
  
  * **Code:** 401 UNAUTHORIZED <br/>
      **Content:** 
      ```json
      {
        "error":"Unauthorized", 
        "message":"Authentication Failed: Invalid token - <token>"
      }
      ```

* **Sample Call:**

  ```bash
  curl -X POST -H 'x-auth-token: <token>' http://localhost:8080/auth/logout
  ```

## List Process

_Gets a list of all running processes._

* **URL**

  _/process_

* **Method:**

  _GET_
  
* **Header:**

  **Required:**
  
  `x-auth-token=[token]`

* **Success Response:**
  
  _A list of all running processes_

  * **Code:** 200 <br />
    **Content:** 
    ```json
    [{ 
      "id": "1",
      "parent": "1",
      "commandline": "/bin/init",
      "user": "root",
      "environment": {
          "env1": "value1",
          "env2": "value2"
      },
      "running": true,
      "returnCode": null
    },{ 
      "id": "2",
      "parent": "1",
      "commandline": "/bin/sh",
      "user": "root",
      "environment": {
          "env1": "value1",
          "env2": "value2"
      },
      "running": true,
      "returnCode": null
    }]
    ```
 
* **Error Response:**
  
  * **Code:** 401 UNAUTHORIZED <br/>
    **Content:** 
    ```json
    {
      "error":"Unauthorized", 
      "message":"Authentication Failed: Invalid token - <token>"
    }
    ```

  * **Code:** 403 FORBIDDEN <br />
    **Content:** 
    ```json
    {
      "error":"Forbidden", 
      "message":"Access Denied"
    }
    ```

* **Sample Call:**

  ```bash
  curl -H 'x-auth-token: <token>' http://localhost:8080/process
  ```

## Start Process

  _Starts a process. The owner of the process is the user which starts it._

* **URL**

  _/process_

* **Method:**
  
  _POST_

* **Header:**

  **Required:**
  
  `x-auth-token=[token]`
  
  `Content-Type=application/json`

* **Data Params**

  ```json
  {
    "environment":{
      "env1":"value1",
      "env2":"value2"
    },
    "workDirectory": "/tmp/",
    "command":"/bin/sh",
    "arguments":[
      "<arg1>", "<arg2>"
    ]
  }
  ```

* **Success Response:**
  
  * **Code:** 200 <br />
    **Content:** 
    ```json
    {
        "pid": "1312",
    	"created": true
    }
    ```
 
* **Error Response:**
  
  * **Code:** 401 UNAUTHORIZED <br/>
    **Content:** 
    ```json
    {
      "error":"Unauthorized", 
      "message":"Authentication Failed: Invalid token - <token>"
    }
    ```

  * **Code:** 403 FORBIDDEN <br />
    **Content:** 
    ```json
    {
      "error":"Forbidden", 
      "message":"Access Denied"
    }
    ```

* **Sample Call:**

    ```bash
    curl -X POST -H 'x-auth-token: <token>' -H 'Content-Type: application/json' http://localhost:8080/process \
      --data '{"environment":{ "env1":"value1" }, "workDirectory": "/tmp/", "command":"/bin/sh", "arguments":[ "-c", "date" ]}'
    ```
    
## Start Process (as admin)

  _Starts a process. The owner of the process is the user which starts the server application._
  
* **URL**

  _/process/admin_

* **Method:**
  
  _POST_

* **Header:**

  **Required:**
  
  `x-auth-token=[token]`
  
  `Content-Type=application/json`

* **Data Params**

  ```json
  {
    "environment":{
      "env1":"value1",
      "env2":"value2"
    },
    "workDirectory": "/tmp/",
    "command":"/bin/sh",
    "arguments":[
      "<arg1>", "<arg2>"
    ]
  }
  ```

* **Success Response:**
  
  * **Code:** 200 <br />
    **Content:** 
    ```json
    {
        "pid": "1312",
    	"created": true
    }
    ```
 
* **Error Response:**
  
  * **Code:** 401 UNAUTHORIZED <br/>
    **Content:** 
    ```json
    {
      "error":"Unauthorized", 
      "message":"Authentication Failed: Invalid token - <token>"
    }
    ```

  * **Code:** 403 FORBIDDEN <br />
    **Content:** 
    ```json
    {
      "error":"Forbidden", 
      "message":"Access Denied"
    }
    ```

* **Sample Call:**

    ```bash
    curl -X POST -H 'x-auth-token: <token>' -H 'Content-Type: application/json' http://localhost:8080/process/admin \
      --data '{"environment":{ "env1":"value1" }, "workDirectory": "/tmp/", "command":"/bin/sh", "arguments":[ "-c", "date" ]}'
    ```

## Signal

 _Send signal to running process. Only the user which created the process can send a signal to them. Expected
 a user which is an admin! If you sends a signal to a foreign process, you will get a 404 (ProcessNotFound)._

* **URL**

  _/process/{pid}/{signal}_
  
  `pid=[string]`
  `signal=[string]`

* **Method:**
  
  _POST_

* **Header:**

  **Required:**
  
  `x-auth-token=[token]`
  
* **Success Response:**
  
  * **Code:** 200 <br />
    **Content:** 
    
    ```json
    {
        "returnCode": 0
    }
    ```
 
* **Error Response:**
  
  * **Code:** 401 UNAUTHORIZED <br/>
    **Content:** 
    ```json
    {
      "error":"Unauthorized", 
      "message":"Authentication Failed: Invalid token - <token>"
    }
    ```

  * **Code:** 403 FORBIDDEN <br />
    **Content:** 
    ```json
    {
      "error":"Forbidden", 
      "message":"Access Denied"
    }
    ```
    
  * **Code:** 404 NOT FOUND <br />
    **Content:** 
    ```json
    {
      "error":"Not Found", 
      "message":"No process found for <pid>"
    }
    ```

* **Sample Call:**

  ```bash
    curl -X POST -H 'x-auth-token: <token>' http://localhost:8080/process/1312/9
  ```
* **Notes:**

  _If you want to know which signals are supported on your system, execute the following command in a shell:_
  ```bash
  kill -l
  ```
  
  _You can use the numeric signal value as well as the name of the signal._
  
## Input

  _Send a input string to a running process. Only the user which created the process can send input to them. Expected
  a user which is an admin! If you sends input to a foreign process, you will get a 404 (ProcessNotFound)._

* **URL**

  _/process/{pid}_
  
  `pid=[string]`

* **Method:**
  
  _POST_

* **Header:**

  **Required:**
  
  `x-auth-token=[token]`
  
  `Content-Type=application/json`
  
* **Data Params**

  ```json
  {
    "input": "Hello World!\n"
  }
  ```
  
  OR
  
  ```json
  {
    "raw": "SGVsbG8gV29ybGQK"
  }
  ```

* **Success Response:**
  
  * **Code:** 200
 
* **Error Response:**
  
  * **Code:** 401 UNAUTHORIZED <br/>
    **Content:** 
    ```json
    {
      "error":"Unauthorized", 
      "message":"Authentication Failed: Invalid token - <token>"
    }
    ```

  * **Code:** 403 FORBIDDEN <br />
    **Content:** 
    ```json
    {
      "error":"Forbidden", 
      "message":"Access Denied"
    }
    ```
    
  * **Code:** 404 NOT FOUND <br />
    **Content:** 
    ```json
    {
      "error":"Not Found", 
      "message":"No process found for <pid>"
    }
    ```

* **Sample Call:**

  ```bash
  curl -X POST -H 'x-auth-token: <token>' -H 'Content-Type: application/json' http://localhost:8080/process/1312 --data '{"input": "Hello World!\n"}'
  ```
  
  OR
  
  ```bash
  curl -X POST -H 'x-auth-token: <token>' -H 'Content-Type: application/json' http://localhost:8080/process/1312 --data '{"raw": "SGVsbG8gV29ybGQK"}'
  ```
* **Notes:**

  _If you are using the "input" special characters will be interpreting. If you are using the "raw" you have to send base64 encoded string.
  In raw-mode no characters will be interpreting._
  
## Status

  _Gets process information._
  
* **URL**

  _/process/{pid}_
  
  `pid=[string]`

* **Method:**
  
  _GET_

* **Header:**

  **Required:**
  
  `x-auth-token=[token]`
  
* **Success Response:**
  
  * **Code:** 200 <br />
    **Content:** 
    
    ```json
    { 
      "id": "1",
      "parent": "1",
      "commandline": "/bin/init",
      "user": "root",
      "environment": {
          "env1": "value1",
          "env2": "value2"
      },
      "running": true,
      "returnCode": null
    }
    ```
 
* **Error Response:**
  
  * **Code:** 401 UNAUTHORIZED <br />
    **Content:** 
    ```json
    {
      "error":"Unauthorized", 
      "message":"Authentication Failed: Invalid token - <token>"
    }
    ```

  * **Code:** 403 FORBIDDEN <br />
    **Content:** 
    ```json
    {
      "error":"Forbidden", 
      "message":"Access Denied"
    }
    ```
    
  * **Code:** 404 NOT FOUND <br />
    **Content:** 
    ```json
    {
      "error":"Not Found", 
      "message":"No process found for <pid>"
    }
    ```

* **Sample Call:**

  ```bash
  curl -H 'x-auth-token: <token>' http://localhost:8080/process/1
  ```

## Output

  _Read the output/error from a process which where started before. Only the user which created the process can read the data. 
  Expected a user which is an admin! If you try to read a foreign process - or an process which where not started from
  the server-application, you will get a 404 (ProcessNotFound)._

* **URL**

  _/process/{pid}/{stream}_
  
  `pid=[string]`
  `stream=[out|err]`

* **Method:**
  
  _GET_
  
* **Header:**

  **Required:**
  
  `x-auth-token=[token]`
  `Accept: application/octet-stream`
  `Range:[range]`
  
  _Range looks like this:_ __&lt;start&gt;-__. Where start is the start index to read.
  
* **Success Response:**
  
  * **Code:** 206 <br />
    **Content:** `the raw output` <br />
    **Header** 
    * `Content-Type: application/octet-stream`
    * `Accept-Ranges: bytes`
    * `Content-Range: <start>-<read>/*`
 
* **Error Response:**
  
  * **Code:** 401 UNAUTHORIZED <br/>
    **Content:** 
    ```json
    {
      "error":"Unauthorized", 
      "message":"Authentication Failed: Invalid token - <token>"
    }
    ```

  * **Code:** 403 FORBIDDEN <br />
    **Content:** 
    ```json
    {
      "error":"Forbidden", 
      "message":"Access Denied"
    }
    ```
    
  * **Code:** 404 NOT FOUND <br />
    **Content:** 
    ```json
    {
      "error":"Not Found", 
      "message":"No process found for <pid>"
    }
    ```
 
  * **Code:** 416 Requested range not satisfiable <br />
    **If** the Range-Header is invalid!
    
* **Sample Call:**

  ```bash
  curl -H 'x-auth-token: <token>' -H 'Accept: application/octet-stream' -H 'Range: 0-' http://localhost:8080/process/1/out
  ```
  
  OR
  
  ```bash
  curl -H 'x-auth-token: <token>' -H 'Accept: application/octet-stream' -H 'Range: 13-' http://localhost:8080/process/1/err
  ```