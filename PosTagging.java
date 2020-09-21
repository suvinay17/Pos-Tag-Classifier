/* Class written by Suvinay Bothra on September 16th to classify sentences using language model/ naive bayes classifier
* Assignment for cmsc395 (NLP) by Dr. Park
*/
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
public class NGrams{

/*
* Command Line input :
* int part: the question number, int k: k most frequent counts/elements needed
* int n : specifies n grams, String filePath: specifies the file path to be read.
* This method is the driver function and calls all other methods that compute N grams.
*/

/* Main is the driver method that calls all other functions,
* Command line arguments: args[0] : training file filePath
* args[1] : test file filePath
*/
HashMap<String, Integer> wordId = new HashMap<>();
HashMap<String, Integer> posId = new HashMap<>();
HashMap<String, Integer> wordCount = new HashMap<>();
HashMap<String, Integer> posCount = new HashMap<>();
String transitionCounts[][] = new String[7][7];
String emmisionCounts[][] = new String[7][36];


public static void main(String args[]){
String inputFile = args[0]; // path of training txt file
ArrayList<String> lines = readFile(trainingData); //reads training file and separates into lines
ArrayList<String> tokenizedLine = tokenize(lines); // puts tokens on lines, and removes classification and stores it in an trainingLabels
getCounts(tokenizedLine); //
getProbabilities(transitionCounts);
getProbabilities(emmisionCounts);
}

/*
* This method reads the file for the corpus
* Parameters:
* String path: Stores the file path of the input text file
* Returns the text file as a String
*/
public static ArrayList<String> readFile(String trainingData){
  int i = 0; // variable used to point to which part of the arraylist we are in
  ArrayList<String> lines = new ArrayList<String>();

  try{
    BufferedReader br = new BufferedReader(new FileReader(trainingData));
    String line;

    while( (line = br.readLine()) != null){
      line = new String(line.getBytes(), "UTF-8"); //making sure the line is UTF-8
      line = line.replace("\uFEFF", ""); // dealing with BOM
      if(!((line.charAt(0) == 'p' || line.charAt(0) == 'r') && line.charAt(1) == ':'))
        lines.add(i, lines.get(i)+line);
      else{
        i++;
        lines.add(line);
      }
    }

    br.close();
}
 catch(Exception e){
   System.out.println("IO exception found, re-run code");
 }
  return lines;
}


/*
* This method puts start and end of line tokens into arraylist of sentences and removes the preceding p: or r: and sets the trainingLabels
* Parameters:
* ArrayList<String> lines: stores the lines read from input files
* Returns the String with start and end tokens added and p: r: removed from the start
*/
public static ArrayList<String> tokenize(ArrayList<String> vines){
  ArrayList<String> lines = new ArrayList<>(vines);
  for(int i = 0; i < lines.size(); i++){
    StringBuilder sb = new StringBuilder();
    sb.append("<s> ");
    sb.append(lines.get(i).substring(2,lines.get(i).length()).trim());
    sb.append(" </s> ");
    String s = sb.toString();
    lines.set(i, s);
  }
  return lines;
}


public static void getCounts(ArrayList<String> lines){
  int pId = 0;
  int wId = 0;
  for(String line : lines){
    String tokens[] = line.split(" ");
    for(String token: tokens){
       String items[] = token.split("/");
       wordCount.put(items[0], 1);
       for(int i = 2; i < items.length + 2; i = i + 2){
         posCount.put(pId, posCount.getOrDefault(items[i], 0) + 1);
         wordCount.put(wId, wordCount.getOrDefault(items[i-1], 0) + 1);
         if(!wordId.containsKey)
            wId.put(items[i-1], wId++);
         if(!posId.containsKey(items[i]))
            pId.put(items[i], pId++);
         if(!posId.containsKey(items[i+2]))
            pId.put(items[i+2], pId++);
        transitionCounts[pId.get(items[i])][pId.get(items[i+2])] += 1;
        emmisionCounts[pId.get(items[i])][wId.get(items[i-1])] += 1;
       }
    }
  }
    smoothCounts(transitionCounts);
    printTable(emmisionCounts);

}


public static void smoothCounts(String[][] transitionCounts){
  for(int i = 0; i < transitionCounts.length; i++){
    for(int j = 0; j < transitionCounts[0].length; j++){
      transitionCounts[i][j] += 1;
      System.out.print(transitionCounts[i][j]+ ", ")
    }
  }
  System.out.println();
}

public static void printTable(String[][] table){
  for(int i = 0; i < table.length; i++){
    for(int j = 0; j < table[0].length; j++)
      System.out.print(transitionCounts[i][j]+ ", ")
  }
  System.out.println();
}


public static String[][] getProbabilities(String[][] table){
  String [][] prob = new String[table.length][table[0].length]
  for(int i = 0; i < table.length; i++){
    for(int j = 0; j < table[0].length; j++){
        prob[i][j] = table[i][j] / posCount.get(i);
    }
  }
  return prob;
}


}
