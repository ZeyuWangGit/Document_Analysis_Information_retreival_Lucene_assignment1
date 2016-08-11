This is the readme file for COMP6490 Document Analysis assignment 1 Lucene code 
produced by Zeyu Wang u5964768
-------------------------------------------------------------------------------

For this submit, I only submit the java code file including FileIndexBuilder.java
SimpleSearchRanker.java FileFinder.java IndexDisplay.java

-------------------------------------------------------------------------------

To test this code, please follow the next several steps:
[1] Reaplace the code java file with the original java code file in folder /DocAnalysisIRLab/src/search
[2] Unzip the documents folder in gov-test-collection to /DocAnalysisIRLab/src folder like /src/documents
[3] Unzip the topics folder in gov-test-collection to /DocAnalysisIRLab/src folder like /src/topics
[4] Running the code in Eclipse
[5] Copy the retrieve.txt in src folder to trec_eval.8.1 folder.
[6] Copy the gov.qrels in topics folder to trec_eval.8.1 folder.
[7] Open Terminal and enter the trec_eval.8.1 folder, type the request
    ./trec_eval -q gov.qrels retrieve.txt


