/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lastus.summarizer;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import lastus.reader.ReadJsonResponses;

// summa
import summa.mds.CentroidComputation;

/**
 *
 * @author horacio
 */
public class AgroMultiSummarizer {
    
    public static String pathToSUMMAGapp="./resources/gapps/INIA-SUMMARIZER-1.gapp";
    public void AgroMultiSummarization() {
       
    }
    
    public  static void initGate() {
        
        try {
            Gate.init();
            
        } catch(GateException ge) {
            Logger.getLogger(AgroMultiSummarizer.class.getName()).log(Level.SEVERE, null, ge);
            
        }
    }
    
    // single doc analyser
    public static SerialAnalyserController documentPipeline;
    
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
    
    
    public void analyzeCorpus(Corpus c) {
        documentPipeline.setCorpus(c);
        try {
            documentPipeline.execute();
        } catch (ExecutionException ex) {
            Logger.getLogger(AgroMultiSummarizer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    summa.mds.CentroidComputation computeCentroid;
    summa.mds.DocumentCentroidSimilarity computeCentroidSimilarity;
    
    public void centroidComputation(Corpus c) {
        
        computeCentroid= new summa.mds.CentroidComputation();
        computeCentroid.setAnnSet("");
        computeCentroid.setVecName("TEXT_Vector_Norm");
        computeCentroid.setCorpus(c);
        computeCentroid.execute();
        
        
    }
    
    public void centroidSimilarity(Corpus c) {
       computeCentroidSimilarity = new summa.mds.DocumentCentroidSimilarity();
       computeCentroidSimilarity.setCentroid("centroid");
       computeCentroidSimilarity.setAnnSet("");
       computeCentroidSimilarity.setSentAnn("Sentence");
       computeCentroidSimilarity.setSentVec("Vector_Norm");
       computeCentroidSimilarity.setCorpus(c);
       computeCentroidSimilarity.execute();
        
    }
    
    // multidocument summarizer
    
    summa.mds.SimpleMultiDocumentSummarizer multiSumma;
    
    
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
    
    
    public String getMultiSummary() {
        
        return multiSumma.multiDocument.getContent().toString().replaceAll("\n"," ");
    }
    // scorer
    summa.SimpleSummarizer scorer;
    
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
    
    
     public static String fname="C:\\work\\data\\"
                + "INIA-data\\respuestas_82_pregs\\respuestas\\6.json";  
    public static void main(String[] args) {
        
        String[] docs;
        Document[] documents;
        try {
            
            docs=ReadJsonResponses.readJson(fname);
            documents=new Document[docs.length];
            
            AgroMultiSummarizer.initGate();
            for(int i=0;i<docs.length;i++) {
                documents[i]=Factory.newDocument(docs[i]);
            }
            
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
            summarizer.centroidComputation(corpus);
            summarizer.centroidSimilarity(corpus);
            Document doc;
            Iterator<Document> iteC=corpus.iterator();
            while(iteC.hasNext()) {
                doc=iteC.next();
                summarizer.scoring(doc);
                
            }
            
             for(int i=0;i<docs.length;i++) {
                System.out.println("doc "+i+" sentences");
                System.out.println(documents[i].getAnnotations().get("Sentence"));
            }
            
            summarizer.multiDocSummarizer(corpus);
            System.out.println("doc1 multi");
            for(int i=0;i<docs.length;i++) {
                System.out.println("multi extract"+i);
                System.out.println(documents[i].getAnnotations("MULTI_EXTRACT"));
            }
            
            System.out.println("multi summary");        
            System.out.println(summarizer.getMultiSummary());
            
            } catch (ResourceInstantiationException ex) {
                            Logger.getLogger(AgroMultiSummarizer.class.getName()).log(Level.SEVERE, null, ex);
            } 
        
        
    }
    
    
}
