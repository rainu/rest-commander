# rest-commander
DRAFT: A Rest-Application which can execute and manage processes.

# Draft: API

## Create
* Method: POST
* Endpoint: /process/
* Data:
```json
{
  "environment": {
    "env1": "value",
    "envN": "valueN"
  },
  "workdir": "/working/directory/",
  "cmd": "command",
  "args": ["arg1", "argN"]
}
```
* Response
```json
{
  "id": "processId"
}
```

## Status
* Method: GET
* Endpoint: /process/{id}
* Response
```json
{
  "out": "process Output...",
  "rc": 15,
  "exit": false
}
```

## Signal
* Method: PATCH
* Endpoint: /process/{id}/{signal}
* Description: Sends a signal to the given process

## Kill
* Method: DELETE
* Endpoint: /process/{id}
* Description: Kills the given process hardly (SIGTERM)

## Input
* Method: POST
* Endpoint: /process/{id}
* Data:
```json
{
  "input": "the input for process..."
}
```