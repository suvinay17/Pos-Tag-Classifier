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
static double transitionCounts[][] = new double[11][11];
static double emmisionCounts[][] = new double[11][36];
static String one = "<s> <p> show NOUN your PRON light NOUN when ADV nothing NOUN is VERB shining NOUN </s> </p>";
static String two = "<s> <p> show VERB your PRON light NOUN when ADV nothing NOUN is VERB shining VERB </s> </p>";
static String three = "<s> <p> show VERB your PRON light NOUN when ADV nothing NOUN is VERB shining NOUN </s> </p>";


public static void main(String args[]){
String inputFile = args[0]; // path of training txt file
ArrayList<String> lines = readFile(inputFile); //reads training file and separates into lines
ArrayList<String> tokenizedLine = tokenize(lines); // puts tokens on lines, and removes classification and stores it in an trainingLabels
getCounts(lines);
 //
System.out.println("Transition Probabilities: ");
double transitionProb[][] = getProbabilities(transitionCounts, 0);
// System.out.println("First"+transitionProb[9][5]);
// System.out.println("Second"+transitionProb[10][1]);
System.out.println("Emission Probabilities: ");
double emissionProb[][] = getProbabilities(emmisionCounts, 1);
generateSentenceProbability(transitionProb, emissionProb, one);
generateSentenceProbability(transitionProb, emissionProb, two);
generateSentenceProbability(transitionProb, emissionProb, three);
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
  posId.put("<p>", 0);
  posId.put("</p>", 1);
  int pId = 2;
  int wId = 2;
  for(String line : lines){
    wordCount.put(0, wordCount.getOrDefault(0, 0)+ 1);
    wordCount.put(1, wordCount.getOrDefault(1, 0)+ 1);
    posCount.put(0, posCount.getOrDefault(0, 0)+ 1);
    posCount.put(1, posCount.getOrDefault(1, 0)+ 1);
    String tokens[] = line.split(" ");
    // wordCount.put(0, wordCount.getOrDefault(0, 0)+ 1;
    ArrayList<String> token = new ArrayList<>();
    token.add("<s>");
    token.add("<p>");
    for(String tok : tokens){
      String arr[] = tok.split("/");
      if(arr.length == 2){
        token.add(arr[0].trim());
        token.add(arr[1].trim());
      }
    }
    token.add("</s>");
    token.add("</p>");
    int i = 0;
    for(i = 2; i < token.size(); i = i + 2){
      //System.out.println(token.size());
      //System.out.println(i+" "+(i+1)+" "+(i+3));
         if(!wordId.containsKey(token.get(i)))
            wordId.put(token.get(i), wId++);
         if(!posId.containsKey(token.get(i+1)))
            posId.put(token.get(i+1), pId++);
        //System.out.println("before");
        posCount.put(posId.get(token.get(i+1)), posCount.getOrDefault(posId.get(token.get(i + 1)), 0) + 1);
        wordCount.put(wordId.get(token.get(i)), wordCount.getOrDefault(wordId.get(token.get(i)), 0) + 1);
        transitionCounts[posId.get(token.get(i-1))][posId.get(token.get(i+1))] += 1;
        emmisionCounts[posId.get(token.get(i+1))][wordId.get(token.get(i))] += 1;
        //System.out.println("after");
        //emmisionCounts[posId.get(token.get(i+1))][wordId.get(token.get(i))] += 1;
       }

  }
   //System.out.println(posId.entrySet());
   //System.out.println(wordId.entrySet());
   System.out.println("Smoothed Transition Counts: ");
   smoothCounts(transitionCounts);
   System.out.println("Emission Counts");
   smoothCounts(emmisionCounts);
}


public static void smoothCounts(double[][] counts){
  for(int i = 0; i < counts.length; i++){
    System.out.print("[");
    for(int j = 0; j < counts[0].length; j++){
      counts[i][j] += 1;
      System.out.print(counts[i][j]+ ", ");
    }
    System.out.print("]");
  }
  System.out.println();
}

public static double[][] getProbabilities(double[][] table, int part){
  //System.out.println("Table 9,5: "+table[9][5]);
  //System.out.println("Map : "+posCount.get(9));
  double [][] prob = new double[table.length][table[0].length];
  for(int i = 0; i < table.length; i++){
    System.out.println("[");
    for(int j = 2; j < table[0].length; j++){
        int x = (part == 0) ? (posCount.get(i) + 11) : (posCount.get(i) + 34);
        prob[i][j] = table[i][j] / x;
        System.out.print(prob[i][j]+ ", ");
    }
    System.out.println("]");
  }
  return prob;
}


public static void generateSentenceProbability(double[][] tprob, double[][] wprob, String sent){
  String tokens[] = sent.split(" ");
  double prob = 1.0d;
  int i = 2;
  for(i = 2; i < tokens.length - 2; i = i + 2)
     prob *= tprob[posId.get(tokens[i-1])][posId.get(tokens[i+1])] * wprob[posId.get(tokens[i+1])][wordId.get(tokens[i])];
  System.out.println("first: "+ prob);
  prob *= tprob[posId.get(tokens[i-1])][1];
  System.out.println("second "+ prob);
  System.out.println("Prob for "+ sent);
  System.out.println(prob);
}



}
