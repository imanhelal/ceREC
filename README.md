# ceREC
CEP-based Runtime Event Correlation (ceREC) Implementation and Evaluation
The source code associates the paper "Online Correlation for Unlabeled Process Events: A Flexible CEP-based Approach" By: Iman Helal, Ahmed Hany

To run our code first:

(1) run GenerateEsperEPL package to generate Esper statements 
    Using Eclipse Modeling Tools - Version: 2019-09 R (4.13.0)
    inputRules in (.txt) format
    Select Heuristic data in (.csv) format (if any)
Specify the output folder inside the eg.cu.fci.is.correlator package in (2) to generate Runner file.
(2) run the generated Runner file in eg.cu.fci.is.correlator modify details accordingly
   Ranking score threshold
The output is stored in two files one for the correlated events and another for the failed events inside /CEPForEventCorrelation folder.

This research work is copy righted to: Iman Helal, Ahmed Hany @2020-2021
