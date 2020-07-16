# AgroSummarizer
Query-based Summarization for INIA



The code implements a query/centroid based summarization system implemented with the SUMMA library

http://www.taln.upf.edu/pages/summa.upf/index.htm

The system uses as infrastructure the GATE system 

https://gate.ac.uk/

For which we use version 8.0

In order to use this code you'll need to install GATE (8.0) in your computer and install the SUMMA plug-in as specified in the 
SUMMA web page.


The code uses a precompiled GATE application stored in directory resources/gapp. This application is a pipeline which includes
a few GATE components and several SUMMA components. Overall the app is able to compute tokens., sentences, perform statistical analysis,
create sentence and text vectors, compute several features for each sentence.

The code contains several methods to implement centroid computation and query based features.

The code is meant to compute a multi-document summary from a set of documents retrieved using a query

The steps involved in the computation are:

1) load the documents and the query 
2) analyse each document and query (compute tokens, sentences, vectors, features, etc.)
3) compute similarity between each sentence in each document and the query
4) conmpute centroid
5) compute similarity between each sentence in each document and the centroid
6) score sentences based on features and weights
7) compute the summary by extracting the "best" sentences from each document and avoiding redundancy


