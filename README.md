# Android app that can run reflection by parsing string.

## Followings are instructions that can be parsed.

### Instantiate class
```
construct $FULL_CLASS_PATH $ARG_FOR_CONSTRUCTOR_1 $ARG_FOR_CONSTRUCTOR_2 ... assign $VAR_NAME
```
### Invoke method
```
invoke $FULL_CLASS_PATH $METHOD_NAME $ARG_CLASS::$ARG_VAL_OR_VAR_NAME ... with $INSTANCE_NAME assign $VAR_NAME
```
### Set member field
```
set $FULL_CLASS_PATH $FIELD_NAME $CLASS_NAME::$VAL with $VAR_NAME
```
### Instantiate interface with implementing method
```
implement $INTERFACE_PATH $METHOD_NAME
    // ANY INSTRUCTION STRINGS
assign $VAR_NAME
```

## Example video
![Screen_recording_20241018_144527 (4)](https://github.com/user-attachments/assets/6a75891b-1363-4e31-801c-ee7393a43f4e)
