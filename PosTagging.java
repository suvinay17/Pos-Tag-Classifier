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
public class PosTagging{

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
static HashMap<String, Integer> wordId = new HashMap<>();
static HashMap<String, Integer> posId = new HashMap<>();
static HashMap<Integer, Integer> wordCount = new HashMap<>();
static HashMap<Integer, Integer> posCount = new HashMap<>();
static double transitionCounts[][] = new double[10][10];
static double emmisionCounts[][] = new double[10][30];


public static void main(String args[]){
String inputFile = args[0]; // path of training txt file
ArrayList<String> lines = readFile(inputFile); //reads training file and separates into lines
ArrayList<String> tokenizedLine = tokenize(lines); // puts tokens on lines, and removes classification and stores it in an trainingLabels
getCounts(tokenizedLine);
 //
System.out.println("Transition Probabilities: ");
//double transitionProb[][] = getProbabilities(transitionCounts);
System.out.println("Emission Probabilities: ");
//double emissionProb[][] = getProbabilities(emmisionCounts);
}

/*
* This method reads the file for the corpus
* Parameters:
* String path: Stores the file path of the input text file
* Returns the text file as a String
*/
public static ArrayList<String> readFile(String trainingData){
  ArrayList<String> lines = new ArrayList<String>();

  try{
    BufferedReader br = new BufferedReader(new FileReader(trainingData));
    String line;

    while( (line = br.readLine()) != null){
      line = new String(line.getBytes(), "UTF-8"); //making sure the line is UTF-8
      line = line.replace("\uFEFF", ""); // dealing with BOM
      lines.add(line);
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
    sb.append(lines.get(i).trim());
    sb.append(" </s> ");
    String s = sb.toString();
    lines.set(i, s);
  }
  return lines;
}


public static void getCounts(ArrayList<String> lines){
  wordId.put("<s>", 0);
  wordId.put("</s>", 1);
  int pId = 0;
  int wId = 2;
  for(String line : lines){
    wordCount.put(0, wordCount.getOrDefault(0, 0)+ 1);
    wordCount.put(1, wordCount.getOrDefault(1, 0)+ 1);
    String tokens[] = line.split(" ");
    // wordCount.put(0, wordCount.getOrDefault(0, 0)+ 1;
    ArrayList<String> token = new ArrayList<>();
    for(String tok : tokens){
      String arr[] = tok.split("/");
      if(arr.length == 2){
        token.add(arr[0]);
        token.add(arr[1]);
      }
    }
    int i = 0;
    for(i = 0; i < token.size() - 3; i = i + 2){
      //System.out.println(token.size());
      //System.out.println(i+" "+(i+1)+" "+(i+3));
         if(!wordId.containsKey(token.get(i)))
            wordId.put(token.get(i), wId++);
         if(!posId.containsKey(token.get(i+1)))
            posId.put(token.get(i+1), pId++);
         if(!posId.containsKey(token.get(i+3)))
            posId.put(token.get(i+3), pId++);
        //System.out.println("before");
        posCount.put(posId.get(token.get(i+1)), posCount.getOrDefault(posId.get(token.get(i + 1)), 0) + 1);
        wordCount.put(wordId.get(token.get(i)), wordCount.getOrDefault(wordId.get(token.get(i)), 0) + 1);
        transitionCounts[posId.get(token.get(i+1))][posId.get(token.get(i+3))] += 1;
        //System.out.println("after");
        //emmisionCounts[posId.get(token.get(i+1))][wordId.get(token.get(i))] += 1;
       }
       posCount.put(posId.get(token.get(i+1)), posCount.getOrDefault(posId.get(token.get(i + 1)), 0) + 1);
       wordCount.put(wordId.get(token.get(i)), wordCount.getOrDefault(wordId.get(token.get(i)), 0) + 1);
        if(!wordId.containsKey(token.get(i)))
           wordId.put(token.get(i), wId++);
        if(!posId.containsKey(token.get(i+1)))
           posId.put(token.get(i+1), pId++);
       emmisionCounts[posId.get(token.get(i+1))][wordId.get(token.get(i))] += 1;
  }
   System.out.println(wordId.entrySet());
   System.out.println(wordCount.entrySet());
    System.out.println("Smoothed Transition Counts: ");
  //  smoothCounts(transitionCounts);
    System.out.println("Emission Counts");
  //  printTable();

}


public static void smoothCounts(double[][] transitionCounts){
  for(int i = 0; i < transitionCounts.length; i++){
    for(int j = 0; j < transitionCounts[0].length; j++){
      transitionCounts[i][j] += 1;
      System.out.print(transitionCounts[i][j]+ ", ");
    }
  }
  System.out.println();
}


public static void printTable(){
  for(int i = 0; i < emmisionCounts.length; i++){
    for(int j = 0; j < emmisionCounts[0].length; j++)
      System.out.print(emmisionCounts[i][j]+ ", ");
  }
  System.out.println();
}


public static double[][] getProbabilities(double[][] table){
  double [][] prob = new double[table.length][table[0].length];
  for(int i = 0; i < table.length; i++){
    for(int j = 0; j < table[0].length; j++){
        prob[i][j] = table[i][j] / posCount.getOrDefault(i, 1);
        System.out.print(prob[i][j]+ ", ");
    }
  }
  return prob;
}



}
