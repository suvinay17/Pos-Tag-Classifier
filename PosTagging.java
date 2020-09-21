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
static double transitionCounts[][] = new double[7][7];
static double emmisionCounts[][] = new double[7][36];


public static void main(String args[]){
String inputFile = args[0]; // path of training txt file
ArrayList<String> lines = readFile(inputFile); //reads training file and separates into lines
ArrayList<String> tokenizedLine = tokenize(lines); // puts tokens on lines, and removes classification and stores it in an trainingLabels
getCounts(tokenizedLine); //
System.out.println("Transition Probabilities: ");
double transitionProb[][] = getProbabilities(transitionCounts);
System.out.println("Emission Probabilities: ");
double emissionProb[][] = getProbabilities(emmisionCounts);
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
  int wId = 1;
  for(String line : lines){
    String tokens[] = line.split(" ");
    for(String token: tokens){
       String items[] = token.split("/");
       wordCount.put(0, 1);
       for(int i = 2; i < items.length + 2; i = i + 2){
         posCount.put(pId, posCount.getOrDefault(pId, 0) + 1);
         wordCount.put(wId, wordCount.getOrDefault(wId, 0) + 1);
         if(!wordId.containsKey(items[i-1]))
            wordId.put(items[i-1], wId++);
         if(!posId.containsKey(items[i]))
            posId.put(items[i], pId++);
         if(!posId.containsKey(items[i+2]))
            posId.put(items[i+2], pId++);
        transitionCounts[posId.get(items[i])][posId.get(items[i+2])] += 1;
        emmisionCounts[posId.get(items[i])][wordId.get(items[i-1])] += 1;
       }
    }
  }
    System.out.println("Smoothed Transition Counts: ");
    smoothCounts(transitionCounts);
    System.out.println("Emission Counts");
    printTable(emmisionCounts);

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


public static void printTable(double[][] table){
  for(int i = 0; i < table.length; i++){
    for(int j = 0; j < table[0].length; j++)
      System.out.print(transitionCounts[i][j]+ ", ");
  }
  System.out.println();
}


public static double[][] getProbabilities(double[][] table){
  double [][] prob = new double[table.length][table[0].length];
  for(int i = 0; i < table.length; i++){
    for(int j = 0; j < table[0].length; j++){
        prob[i][j] = table[i][j] / posCount.get(i);
        System.out.print(prob[i][j]+ ", ");
    }
  }
  return prob;
}


}
