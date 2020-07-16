/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lastus.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;



/**
 *
 * @author horacio
 */
public class ReadJsonResponses {
    
    public static String fname="C:\\work\\data\\"
                + "INIA-data\\respuestas_82_pregs\\respuestas\\6.json"; 
    public static String outdir="C:\\work\\data\\"
                + "INIA-data\\respuestas_82_pregs\\respuestas\\6";
    public static void main(String[] args) {
        
        readJson(fname);
        
    }
    
    public static String[] readJson(String fname) {
    
        String[] docs=null;
        JSONParser jsonParser = new JSONParser();
        OutputStreamWriter outputStreamWriter;
        PrintWriter out;
        try (FileReader reader = new FileReader(fname))
        {
                
            // read into String UPF-8
            BufferedReader oReader = new BufferedReader(new InputStreamReader(new 
                             FileInputStream(fname),"UTF-8"));
            String strUTF8 = "[";
            String line;
            while((line=oReader.readLine())!=null) {
                strUTF8=strUTF8+line;
            }
            strUTF8=strUTF8+"]";
                
                
            
            
            //Read JSON file
            JSONArray obj = (JSONArray) jsonParser.parse(strUTF8);
            
 
            JSONObject obj1 = (JSONObject) obj.get(0);
         //   System.out.println(list);
           
           
         System.out.println(obj1.keySet());
         JSONObject obj2=(JSONObject)obj1.get("hits");
         
         JSONArray obj3=(JSONArray)obj2.get("hits");
         
         int top=obj1.size();
               
         int i=0;
         
         docs=new String[top];
         
         while(i<top) {
             
            JSONObject obj4=(JSONObject)obj3.get(i);

            JSONObject obj5=(JSONObject) obj4.get("_source");


            String txt=(String) obj5.get("texto");
            
       //     System.out.println(txt);
            docs[i]=txt;
            i++;
         }
                
                       
            
 
        } catch (FileNotFoundException e) {
            
        } catch (IOException e) {
            
        } catch (org.json.simple.parser.ParseException ex) {
           
        } 
        return docs;
    }
}
