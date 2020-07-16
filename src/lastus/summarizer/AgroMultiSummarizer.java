/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lastus.summarizer;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Gate;
import gate.creole.ConditionalSerialAnalyserController;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.persist.PersistenceException;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import gate.Factory;
import gate.util.OffsetComparator;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import lastus.reader.ReadJsonResponses;

// summa
import summa.mds.CentroidComputation;

/**
 * This code is an implementation of a query+centroid based multidocument summarization system
 * based on the SUMMA library.
 * 
 * @author horacio
 */
public class AgroMultiSummarizer {
    
    /* the "precompiled"  application which analyses de documents produceing sentences, tokens,
       statistics, vectors for sentences (Vector_Norm) and for the whole document (TEXT_Vector_Norm), terms, etc.
    */
    public static String pathToSUMMAGapp="./resources/gapps/INIA-SUMMARIZER-1.gapp";
    
    
    public void AgroMultiSummarization() {
       
    }
    
    // initialization of the GATE library
    public  static void initGate() {
        
        try {
            Gate.init();
            
        } catch(GateException ge) {
            Logger.getLogger(AgroMultiSummarizer.class.getName()).log(Level.SEVERE, null, ge);
            
        }
    }
    
    // single doc analyser to "hold" the text analysis app
    public static SerialAnalyserController documentPipeline;
    
    // loading of the app
    public void initDocPipeline() {
        
        try {
            // load the GAPP
            documentPipeline =
                    (SerialAnalyserController)
                    PersistenceManager.loadObjectFromFile(new File(pathToSUMMAGapp));
        } catch (PersistenceException ex) {
            Logger.getLogger(AgroMultiSummarizer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AgroMultiSummarizer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ResourceInstantiationException ex) {
            Logger.getLogger(AgroMultiSummarizer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    // apply the app to the whole corpus
    public void analyzeCorpus(Corpus c) {
        documentPipeline.setCorpus(c);
        try {
            documentPipeline.execute();
        } catch (ExecutionException ex) {
            Logger.getLogger(AgroMultiSummarizer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    // the method to compute centroid
    summa.mds.CentroidComputation computeCentroid;
    // the method to compute the similarity betweern sentences and the centroid
    summa.mds.DocumentCentroidSimilarity computeCentroidSimilarity;
    
    // computes the centroid (avg of text vectors of each document)
    public void centroidComputation(Corpus c) {
        
        computeCentroid= new summa.mds.CentroidComputation();
        computeCentroid.setAnnSet("");
        computeCentroid.setVecName("TEXT_Vector_Norm");
        computeCentroid.setCorpus(c);
        computeCentroid.execute();
        
        
    }
    
    // computes the similarity of each sentence to the centroid storing a similarity features in each sentence
    public void centroidSimilarity(Corpus c) {
       computeCentroidSimilarity = new summa.mds.DocumentCentroidSimilarity();
       computeCentroidSimilarity.setCentroid("centroid");
       computeCentroidSimilarity.setAnnSet("");
       computeCentroidSimilarity.setSentAnn("Sentence");
       computeCentroidSimilarity.setSentVec("Vector_Norm");
       computeCentroidSimilarity.setCorpus(c);
       computeCentroidSimilarity.execute();
        
    }
    
    // multidocument "summarizer"
    summa.mds.SimpleMultiDocumentSummarizer multiSumma;
    
    // using the score computed it creates a multi document summary
    // compression set at 10% and redundancy is controlled
    public void multiDocSummarizer(Corpus c) {
        
        try {
            multiSumma=new summa.mds.SimpleMultiDocumentSummarizer();
            multiSumma.setAnnSet("");
            multiSumma.setCompression(10);
            multiSumma.setCorpus(c);
            multiSumma.setNewDocument(Boolean.TRUE);
            multiSumma.setRemoveRedundancy(Boolean.TRUE);
            multiSumma.setSentAnn("Sentence");
            multiSumma.setSentCompression(Boolean.TRUE);
            multiSumma.setSumSetName("MULTI_EXTRACT");
            multiSumma.setThresholdSim(0.05);
            multiSumma.setTokenAnn("Token");
            multiSumma.setVectorName("Vector_Norm");
            multiSumma.setCorpus(c);
            multiSumma.execute();
            
        } catch (ExecutionException ex) {
            Logger.getLogger(AgroMultiSummarizer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // gets the final multidocument summarizer
    public String getMultiSummary() {
        
        return multiSumma.multiDocument.getContent().toString();
    }
    // scorer
    summa.SimpleSummarizer scorer;
    
    // scores sentences (in a feature named "score") based on the features set.
    // Note that in this case  centroid_sim is used but
    // avaiable features are:
    // sem_score
    // first_sim
    // query_sim
    // centroid_sim
    // position_score
    // tf_score
    // paragraph_score
    public void scoring(Document doc) {
        
        try {
            ArrayList features=new ArrayList();
            features.add("centroid_sim");
            ArrayList weights=new ArrayList();
            weights.add(1.00);
            scorer=new summa.SimpleSummarizer();
            scorer.setAnnSetName("");
            scorer.setSentAnn("Sentence");
            scorer.setCompression(10);
            scorer.setScoreOnly(Boolean.TRUE);
            scorer.setNewDocument(Boolean.FALSE);
            scorer.setWordAnn("Token");
            scorer.setSentCompression(Boolean.TRUE);
            scorer.setSumSetName("EXTRACT");
            scorer.setSumFeatures(features);
            scorer.setSumWeigths(weights);
            scorer.setDocument(doc);
            scorer.execute();
        } catch (ExecutionException ex) {
            Logger.getLogger(AgroMultiSummarizer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // scores sentences (in a feature named "score") based on the features set.
    // Note that in this case query_sim and centroid_sim are used but
    // avaiable features are:
    // sem_score
    // first_sim
    // query_sim
    // centroid_sim
    // position_score
    // tf_score
    // paragraph_score
    public void qScoring(Document doc) {
        
        try {
            // features to be used 
            ArrayList features=new ArrayList();
            features.add("centroid_sim");
            features.add("query_sim");
            // weights to be used
            ArrayList weights=new ArrayList();
            weights.add(0.50);
            weights.add(0.50);
            scorer=new summa.SimpleSummarizer();
            scorer.setAnnSetName("");
            scorer.setSentAnn("Sentence");
            // 10 percent compression
            scorer.setCompression(10);
            scorer.setScoreOnly(Boolean.TRUE);
            scorer.setNewDocument(Boolean.FALSE);
            scorer.setWordAnn("Token");
            scorer.setSentCompression(Boolean.TRUE);
            scorer.setSumSetName("EXTRACT");
            scorer.setSumFeatures(features);
            scorer.setSumWeigths(weights);
            scorer.setDocument(doc);
            scorer.execute();
        } catch (ExecutionException ex) {
            Logger.getLogger(AgroMultiSummarizer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    // analyses the query with the same pipeline as the document
    public void analyzeQuery(Document q) {
        
        try {
            Corpus qCorpus=Factory.newCorpus("");
            qCorpus.add(q);
            documentPipeline.setCorpus(qCorpus);
            documentPipeline.execute();
        } catch (ResourceInstantiationException ex) {
            Logger.getLogger(AgroMultiSummarizer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(AgroMultiSummarizer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // query based method
    summa.scorer.QueryMethod queryMethod;
    // computes similarity between query and each sentence
    public void querySimilarity(Document q, Corpus c) {
        Iterator<Document> iteDoc=c.iterator();
        Document doc;
        try {
            queryMethod=new summa.scorer.QueryMethod();
           
            queryMethod.setQuery(q);
            queryMethod.setAnnSet("");
            queryMethod.setQueryFeature("query_sim");
            queryMethod.setQuerySet("");
            queryMethod.setQueryVec("TEXT_Vector_Norm");
            queryMethod.setSentAnn("Sentence");
            queryMethod.setVecName("Vector_Norm");
            while(iteDoc.hasNext()) {
                
                doc=iteDoc.next();
                queryMethod.setDocument(doc);
                queryMethod.execute();
            }
            
           
        } catch (ExecutionException ex) {
            Logger.getLogger(AgroMultiSummarizer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    
    // now the main which should be adapted at will
    // the directory used contains a json with all docs 
    // the query should be specified as parameter 0 of the main
    
    public static String fname="C:\\work\\data\\"
                + "INIA-data\\respuestas_82_pregs\\respuestas\\6.json";
    // Query is argument 0
    public static void main(String[] args) {
        
        String[] docs;
        Document[] documents;
        Document query;
        String queryText=args[0];
        System.out.println(queryText);
        try {
            // documents
            docs=ReadJsonResponses.readJson(fname);
            documents=new Document[docs.length];
           
            
            // init the resources
            AgroMultiSummarizer.initGate();
            for(int i=0;i<docs.length;i++) {
                documents[i]=Factory.newDocument(docs[i]);
            }
            // query
            query=Factory.newDocument(queryText);
            AgroMultiSummarizer summarizer = new AgroMultiSummarizer();
            summarizer.initDocPipeline();
            Corpus corpus=Factory.newCorpus("");
            //Document doc1=Factory.newDocument(new URL("file:///"+floc1), "UTF-8");
            //Document doc2=Factory.newDocument(new URL("file:///"+floc2), "UTF-8");
            // corpus.add(doc1);
            // corpus.add(doc2);
            for(int i=0;i<docs.length;i++) {
                corpus.add(documents[i]);
            }
            summarizer.analyzeCorpus(corpus);
            summarizer.analyzeQuery(query);
            System.out.println(query);
            summarizer.querySimilarity(query, corpus);
            
            summarizer.centroidComputation(corpus);
            summarizer.centroidSimilarity(corpus);
            Document doc;
            Iterator<Document> iteC=corpus.iterator();
            while(iteC.hasNext()) {
                doc=iteC.next();
                // only centroid 
                // summarizer.scoring(doc);
                
                // query + centroid based
                summarizer.qScoring(doc);
                
            }
            
            // some printing.... remove
            // for(int i=0;i<docs.length;i++) {
            //    System.out.println("doc "+i+" sentences");
            //    System.out.println(documents[i].getAnnotations().get("Sentence"));
            //}
            
            summarizer.multiDocSummarizer(corpus);
           // some printing .... remove
            System.out.println("doc1 multi");
            for(int i=0;i<docs.length;i++) {
                System.out.println("multi extract"+i);
                System.out.println(documents[i].getAnnotations("MULTI_EXTRACT"));
            }
            
            System.out.println("multi summary");    
            String summary=summarizer.getMultiSummary();
            // some printing .... remove
      
            System.out.println(summary);
            
            
            } catch (ResourceInstantiationException ex) {
                            Logger.getLogger(AgroMultiSummarizer.class.getName()).log(Level.SEVERE, null, ex);
            } 
        
        
    }
    
    
}
