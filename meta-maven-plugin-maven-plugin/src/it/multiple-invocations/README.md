This integration test verifies the following:

1. Multiple executions of the meta-maven-plugin-maven-plugin generates unique mojos for each invocation.
2. Those mojos derive from uniquely generated abstract base classes that contain the correct embedded plugin configuration.
3. All the generated mojos can be executed successfully.
