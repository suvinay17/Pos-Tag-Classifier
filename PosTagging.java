/* Class written by Suvinay Bothra on September 23rd
* cmsc395 (NLP) by Dr. Park
*/
import java.util.ArrayList; // Data Structures
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.io.BufferedReader; // Input Output Resources
import java.io.FileReader;
import java.io.FileNotFoundException;

public class PosTagging{

static double K = 0.5; // change K as you please for add K smoothing

static HashMap<String, Integer> wordId = new HashMap<>(); // Associates a word with an integer id
static HashMap<String, Integer> posId = new HashMap<>();  // Associates a POS tag with an integer id
static int pId = 2; // stores the next id to be assigned for the next unique POS tags
static int wId = 2; // ^ stores the next ids for the next unique word

static HashMap<Integer, Integer> wordCount = new HashMap<>(); // Stores counts of specific words, used to calculate probabilities
static HashMap<Integer, Integer> posCount = new HashMap<>(); // stores counts of specific POS tags, used to calculate probabilities
static int transitionCounts[][] = new int[11][11]; // Matrix for transitionCounts
static int emmisionCounts[][] = new int[11][36]; // matrix for emmisionCounts

static String one = "<s> <p> show NOUN your PRON light NOUN when ADV nothing NOUN is VERB shining NOUN </s> </p>"; // Three sentences with their POS tags, provided by the question
static String two = "<s> <p> show VERB your PRON light NOUN when ADV nothing NOUN is VERB shining VERB </s> </p>";
static String three = "<s> <p> show VERB your PRON light NOUN when ADV nothing NOUN is VERB shining NOUN </s> </p>";



/* Main is the driver method that calls all other functions,
* Command line arguments: args[0] : filePath for txt file to read the data
*/
public static void main(String args[]){
String inputFile = args[0]; // path of training txt file
ArrayList<String> lines = readFile(inputFile); //reads training file and separates into lines

getCounts(lines); // Goes through all lines and fills the transitionCounts and emmisionCounts

int smoothedTc[][] = smoothCounts(transitionCounts); // Add 1 smoothing to transitionCounts
int smoothedEc[][] = smoothCounts(emmisionCounts); // Add 1 smoothing to transitionProb

System.out.println("Use these pos tag ids to navigate row column"+ posId);
System.out.println("Smoothed Transition Counts: ");
printMatrix(smoothedTc); // print
System.out.println("Use these words ids to navigate row column"+ wordId);
System.out.println("Smoothed Emission Counts: ");
printMatrix(smoothedEc);

System.out.println("Transisiton Probabilities: ");
double transitionProb[][] = getProbabilities(smoothedTc, 0);

System.out.println("Emission Probabilities: ");
double emissionProb[][] = getProbabilities(smoothedEc, 1);

generateSentenceProbability(transitionProb, emissionProb, one); // generates the POS tag probability for the provided 3 sentences
generateSentenceProbability(transitionProb, emissionProb, two);
generateSentenceProbability(transitionProb, emissionProb, three);

String v_input = "show your light when nothing is shining";
viterbi(v_input);
}


/*
* This method reads the file for the corpus
* Parameters:
* String path: Stores the file path of the input text file
* Returns the text file as an ArrayList<String> of each line of the test file
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


/* This iterates through the lines from input and stores transitionCounts and emmisionCounts using method updateCounts()
* Input : ArrayList<String> lines
*/
public static void getCounts(ArrayList<String> lines){
  wordId.put("<s>", 0); // Giving fixed integers id's  to start/ end word/POS tokens
  wordId.put("</s>", 1);
  posId.put("<p>", 0);
  posId.put("</p>", 1);

  for(String line : lines){

    wordCount.put(0, wordCount.getOrDefault(0, 0)+ 1); // For every line, increment Start, End Word tokens and Start, END pos token
    wordCount.put(1, wordCount.getOrDefault(1, 0)+ 1);
    posCount.put(0, posCount.getOrDefault(0, 0)+ 1);
    posCount.put(1, posCount.getOrDefault(1, 0)+ 1);

    String tokens[] = line.split(" "); // split line based on space
    ArrayList<String> token = new ArrayList<>(); // THis list will store every token of type WORD/POS

    token.add("<s>"); // NOTE: token and tokens are different variables
    token.add("<p>"); // token.get(i) will contain word, token.get(i+1) will contain posTag

    for(String tok : tokens){
      String arr[] = tok.split("/");
      if(arr.length == 2){
        token.add(arr[0].trim());
        token.add(arr[1].trim());
      }
    }

    token.add("</s>"); // adding end of sentence tokens both word and POS
    token.add("</p>");

    updateCounts(token);

  }
}

/* This method updates the transitionCounts and emmisionCounts, assigns ids to words and POS tags
* Input ArrayList<String> token : stores words and pos tokens at alternate indexes.
*/
public static void updateCounts(ArrayList<String> token){
  int i = 0;

  for(i = 2; i < token.size()-2; i = i + 2){ // i = 2 because first two are <s> and <p>, i < token.size()-2 because last two elements are </s> and </p>
      if(!wordId.containsKey(token.get(i))) // giving IDs to words
          wordId.put(token.get(i), wId++); // incrementing next id to be given
      if(!posId.containsKey(token.get(i+1))) // giving ids to Pos Tags
          posId.put(token.get(i+1), pId++);

      posCount.put(posId.get(token.get(i+1)), posCount.getOrDefault(posId.get(token.get(i + 1)), 0) + 1); // incrementing the count of PosTag at i+1
      wordCount.put(wordId.get(token.get(i)), wordCount.getOrDefault(wordId.get(token.get(i)), 0) + 1); // incrementing the count of Word at i
      transitionCounts[posId.get(token.get(i-1))][posId.get(token.get(i+1))] += 1; // Setting transitionCounts
      emmisionCounts[posId.get(token.get(i+1))][wordId.get(token.get(i))] += 1; // Setting emmisionCounts
     }

     transitionCounts[posId.get(token.get(i-1))][posId.get(token.get(i+1))] += 1; // transisiton for end, given last POS tag
}


/*Performs Add 1 smoothing on both transitionCounts and emmisionCounts
* Input: int[][] counts
* Output: int[][] counts (smoothed)
*/
public static int[][] smoothCounts(int[][] counts){

  for(int i = 0; i < counts.length; i++){
    for(int j = 0; j < counts[0].length; j++)
      counts[i][j] += 1;
    }

  return counts;
}


/* Method for computing the probability matrix given counts for both emission and transisiton
* Input: int[][] table : 2d array of counts (transition if part == 0, else emission)
* int part : specifies which probability is being calculated: emission vs transition
* Output: doube[][] prob with transition/emission probability depending on the part
*/
public static double[][] getProbabilities(int[][] table, int part){
  double [][] prob = new double[table.length][table[0].length];

  for(int i = 0; i < table.length; i++){
    System.out.println("["); // for formatting

    for(int j = (part == 0) ? 0 : 2; j < table[0].length; j++){
        int x = (part == 0) ? (posCount.get(i) + 11) : (posCount.get(i) + 34); // Number of occurences of 11 POS Tags including <p> and </p>, 34 Words excluding <s> </s>
        prob[i][j] = (double)table[i][j] / x; // computes probability
        System.out.print(prob[i][j]+ ", ");
    }

    System.out.println(); // for formatting
    System.out.println("]");
  }
  return prob;
}


/* Prints the probability of getting the given tag for the word
* Input : double tprob[][] : stores transitionProbability
* double wprob[][] : stores emission probability
* String sent: Stores input sentence with words and pos alternating.
*/
public static void generateSentenceProbability(double[][] tprob, double[][] wprob, String sent){

  String tokens[] = sent.split(" "); //split the sentence provided based on spaces
  double prob = 1.0d;
  int i = 2;

  for(i = 2; i < tokens.length - 2; i = i + 2)
     prob *= tprob[posId.get(tokens[i-1])][posId.get(tokens[i+1])] * wprob[posId.get(tokens[i+1])][wordId.get(tokens[i])]; // brute force expression

  prob *= tprob[posId.get(tokens[i-1])][1]; // [1] is the id of end of sentence POS tag.

  System.out.println();
  System.out.println("Prob for "+ sent);
  System.out.println(prob);

}


/* Prints input matrix
* Input: int arr[][]
*/
public static void printMatrix(int[][] arr){
  for(int i = 0; i < arr.length; i++){
    System.out.println("[");
    for(int j = 0; j < arr[0].length; j++){
      System.out.print(arr[i][j]+", ");
    }
    System.out.println();
    System.out.println("]");
  }
}


public static void viterbi(String str){
  double v[][] = new double[11][str.length()];
  int backpointer[][] = new double[11][str.length()];
  String words[] = str.split(" ");
  //initialization step
  for(int s = 0; s < 11; s++){
    v[s][0] =  transitionProb[0][s] * emissionProb[s][wordId.get(words[0])];
    backpointer[e.getValue()][0] = 0;
  }

  double temp = 0;
  //recursion step
  for(int t = 1; i < str.length(); t++){
    for(int s = 0; s < 11; s++ ){
      double max = -1;
      int pointer = -1;
      for(int n = 0; n < 11; n++){
      temp = v[s][t-1] * transitionProb[n][s] * emissionProb[s][t];
      if(temp > max){pointer = n;}
      max = Math.max(temp, max);
     }
     v[s][t] = max;
     backpointer[s][t] = pointer;
    }
  }
  //termination step
  String path[] = new String[11];
  double max = -1.0;
  int pointer = -1.0;
  for(int s = 0; s < 11; s++){
      if(v[s][str.length()] > max){
        pointer = s;
        max = v[s][str.length];
      }
  }
  System.out.println(" Max Probability for this sentence is :" + max);

}



}
