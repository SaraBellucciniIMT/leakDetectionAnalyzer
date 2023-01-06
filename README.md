# leakDetectionAnalyzer
The leak detection analyzer is a tool that provides verfication for BPMN models enhanced with privacy technologies (PETs).

## Prerequisite
mCRL2 tool an java version 1.8.0 installed.

## Using
The current version of the analyzer is "leakdetect_v3.1(22012021).jar". It is integrated in Pleak ("https://pleak.io/home"), that is the modelling environment for PE-BPMN models. To use it outside of it, write the following sentence in the command line
```
java -jar leakdetect_v3.1(22012021).jar
```
The input to the tool is a ".bpmn" file.
```
Insert file path
C:\Users\...\...\Model.bpmn
```
The menu shows all the verification types that a user can perform over the model.
```
->1 Check if a <SELECT> task knows a set of <Data1,...,Datan> data
->2 Check if a <SELECT> task participant knows a set of <Data1,...,Datan> data
->3 Verify (SS/AddSS/FunSS/PK/SK) violation
->4 Reconstruction checking
->5 Verify MPC
->6 Deadlock freedom
->7 exit
```
Point **1** checks if a selected task gets to know a selected set of data objects. If the property is satisfied (true) then a path that shows when it happens is printed out.

Point **2** checks if a selected participant gets to know a selected set of data objects. As above, the path is printed out if the property is satisfied.

Point **3** verifies if the secret-sharing or encryption protocols with their specialization are violated or not.
If they are violated, a message that specifies the type of violation is printed (SECRET SHARING VIOLATED/ENCRYPTION VIOLATED) and the path leading to the violation is also printed out and in a json file.
The last action of the path, that is the "VIOLATION" action, contains the set of data objects that generates the violation.

Point **4** verifies a property of the secret-sharing protocol, that is the possibility to always reconstruct the secret. It the property is not satisfied then the path leading to the violation is printed out.

Point **5** checks the property of the MPC PET (Multi-party computation) and it verifies that all the tasks in the same MPC are executed synchronously. 
If that is not the case then a path showing where the deadlock emerges is generated.

Point **6** checks for deadlock freedom. If exists a path where a participant cannot reach its end event then a deadlock exists in the model. In that case, a path is shown.



