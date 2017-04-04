
import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

public class MainCV
{
    final static int photoRows = 128; // Number of rows of greyscale numbers
    final static int photoColumns = 120; // Number of columns of greyscale numbers
    final static double eta = 0.1; //learning rate
    final static int numInputNodes = photoRows*photoColumns;
    final static int numHiddenNodes = 10;
    final static double maleTargetOutput = 0.9;
    final static double femaleTargetOutput = 0.35;
    final static int iterations = 50;
    static NeuralNetwork valhallaNN;
    static List<Double> errorRateTraining = new ArrayList<Double>();
    static List<Double> errorRateTest = new ArrayList<Double>();


    static List<Double> totalErrorRateTrainingAvg = new ArrayList<Double>();
    static List<Double> totalErrorRateTestAvg = new ArrayList<Double>();

    static List<Double> totalErrorRateTrainingStdDev = new ArrayList<Double>();
    static List<Double> totalErrorRateTestStdDev = new ArrayList<Double>();

    public static void main(String[] args) 
    {
        File maleFiles;
        File femaleFiles;
        File testFiles;
        ArrayList<File> maleList;
        ArrayList<File> femaleList;
        ArrayList<File> testList;

        if (args.length == 5){
            maleFiles = new File(args[1]);
            femaleFiles = new File(args[2]);
            testFiles = new File(args[4]);
            maleList = new ArrayList<File>(Arrays.asList(maleFiles.listFiles()));
            femaleList = new ArrayList<File>(Arrays.asList(femaleFiles.listFiles()));
            testList = new ArrayList<File>(Arrays.asList(testFiles.listFiles()));

            ArrayList<File> allList = new ArrayList<File>();
            allList.addAll( maleList );
            allList.addAll( femaleList );
            Collections.shuffle(allList);

            // Cross validation stuff
            List<File> setOne = new ArrayList<File>();
            List<File> setTwo = new ArrayList<File>();
            List<File> setThree = new ArrayList<File>();
            List<File> setFour = new ArrayList<File>();
            List<File> setFive = new ArrayList<File>();


            setOne = allList.subList(0, 54);
            setTwo = allList.subList(55, 109);
            setThree = allList.subList(110, 164);
            setFour = allList.subList(165, 219);
            setFive = allList.subList(220, 275);

            List<List<File>> folds = new ArrayList<>();
            folds.add(setOne);
            folds.add(setTwo);
            folds.add(setThree);
            folds.add(setFour);
            folds.add(setFive);

            for(int k = 0; k < 5; k++){
                valhallaNN = new NeuralNetwork();
                crossValidationTraining(folds, k);
            }


        System.out.println("Average error on Training: " + calculateAverage(totalErrorRateTrainingAvg) );
        System.out.println("Average error on Test: " + calculateAverage(totalErrorRateTestAvg) );

        System.out.println("std-dev error on Training: " + calculateAverage(totalErrorRateTrainingStdDev) );
        System.out.println("std-dev error on Test: " + calculateAverage(totalErrorRateTestStdDev) );

        }
        else{
            System.out.println("Wrong number of arguments. Expected: java <teamname> -train <dir> <dir> -test <dir>");
        }
        
    }

    private static void crossValidationTraining(List<List<File>> folds, int k){
        double output;
        for(int j = 0; j < 5; j++){
            if (j == k)
               continue;
            for(int i = 0; i < iterations; i++){
                for(File file : folds.get(j)) {
                    if(file.toString().equals("Male/a") || file.toString().equals("Female/b"))
                        continue;
                    double[] inputVector = readFile(file);

                    if (file.toString().contains("Female"))
                        output = femaleTargetOutput;
                    else
                        output = maleTargetOutput;
                    valhallaNN.train(inputVector, output);
                    }
            }
        }
        for(File file : folds.get(k)) {
            if(file.toString().equals("Male/a") || file.toString().equals("Female/b"))
                continue;
             double[] inputVector = readFile(file);
            if (file.toString().contains("Female"))
                output = femaleTargetOutput;
            else
                output = maleTargetOutput;
            System.out.println(valhallaNN.testCrossValidation(inputVector, output));
        }



        totalErrorRateTrainingAvg.add( calculateAverage(errorRateTraining) );
        totalErrorRateTestAvg.add( calculateAverage(errorRateTest) );

        totalErrorRateTrainingStdDev.add( std_dev(errorRateTraining) );
        totalErrorRateTestStdDev.add( std_dev(errorRateTest) );



        errorRateTraining.clear();
        errorRateTest.clear();

    }


    private static double[] readFile(File file){

        File filename = new File(file.getAbsolutePath()); 
        String name  = filename.toString();
        double[] inputVector = new double[photoRows*photoColumns];
        try {

            Scanner scan = new Scanner(filename); 
            int i = 0;
            while (scan.hasNextLine()) {
                try{
                    int element = scan.nextInt();
                    inputVector[i] = (double)element / 255;
                    i++;
                }
                catch(NoSuchElementException e){}
            }
        } 
        catch (FileNotFoundException e) {}
        return inputVector;
    }

    private static double calculateAverage(List<Double> marks) {
        double sum = 0;
        if(!marks.isEmpty()) {
            for (double mark : marks) {
                sum += mark;
            }
            return sum / marks.size();
        }
        return sum;
    }


    private static double std_dev( List<Double> a) {
        int n = a.size();
        if(n == 0)
            return 0.0;
        double sum = 0;
        double sq_sum = 0;
        for(int i = 0; i < n; ++i) {
           sum += a.get(i);
           sq_sum += a.get(i) * a.get(i);
        }
        double mean = sum / n;
        double variance = sq_sum / n - mean * mean;
        return Math.sqrt(variance);
    }

    private static class NeuralNetwork {

        private double[] hiddenToOutWeights;//single dimension since 1 output node
        private double[][] setToWeights;

        public NeuralNetwork() {
            //number of edges between input and hidden layer 1536 partially connected

            setToWeights = new double[numHiddenNodes][numInputNodes/10];
            hiddenToOutWeights = new double[numHiddenNodes];
            for (int k=0; k < 10; k++){
                for (int i=0; i < numInputNodes/10; i++){
                    setToWeights[k][i] = randWeight();
                }
            }
            for (int h=0; h < hiddenToOutWeights.length; h++)
                hiddenToOutWeights[h] = randWeight();
        }        

        private double randWeight(){
            double randD = Math.random();
            Random rand = new Random();
            return (randD * 0.05) * Math.pow((-1), rand.nextInt());
        }

        public void train(double[] inputVector, final double targetOutput){

            /* Propagate the inputs forward to compute the outputs */
            double[] hiddenNodes = new double[numHiddenNodes];


            double[] orginalInputVector= new double[photoRows*photoColumns];
            for(int i = 0; i < photoRows*photoColumns; i++){
                orginalInputVector[i] = inputVector[i];
            }

            double outputNode = feedForward(inputVector, hiddenNodes);

            /* Propagate deltas backward from output layer to input layer */
            double error = outputNode*(1 - outputNode) * (targetOutput - outputNode); //delta in book algorithm
            errorRateTraining.add(error);

            //backprop error on hidden nodes from output to hidden layer
            double[] originalhiddenNodes = new double[numHiddenNodes];
            double[] errorHiddenNodes = new double[numHiddenNodes];
            System.arraycopy(hiddenNodes, 0, originalhiddenNodes, 0, originalhiddenNodes.length);
            for (int i = 0; i < numHiddenNodes; i++){
                errorHiddenNodes[i] = hiddenNodes[i]*(1-hiddenNodes[i]) * hiddenToOutWeights[i] * error;
            }

            //backprop error on weights from output to hidden layer
            for (int i = 0; i < hiddenToOutWeights.length; i++){
                hiddenToOutWeights[i] += eta * originalhiddenNodes[i] * error;
            }

            //backprop error on input to hidden weights
            int k = 0;
            for(int i = 0; i < photoRows*photoColumns; i++){
                if (i != 0 && i % (numInputNodes/10) == 0)
                    k++;
                setToWeights[k][i%(numInputNodes/10)] += eta * orginalInputVector[i] * errorHiddenNodes[k];
            }
        }


        public double activationFunction(double z){
            return 1 / (1 + Math.exp(-z));
        }


        public double feedForward(double[] inputVector, double[] hiddenNodes){
            /* Propagate the inputs forward to compute the outputs */

            double outputNode = 0.0;

            int k=0;//set and hidden node
            for(int i = 0; i < photoRows*photoColumns; i++){
                inputVector[i] *= setToWeights[k][i%(numInputNodes/10)];
                if (i != 0 && i % (numInputNodes/10) == 0){
                    k++;
                }
            }

            double[] in = new double[hiddenNodes.length];
            k=0;
            for(int i = 0; i < photoRows*photoColumns; i++){
                in[k] += inputVector[i];
                if (i != 0 && i % (numInputNodes/10) == 0)
                {
                    hiddenNodes[k] = activationFunction(in[k]);
                    k++;
                }
            }

            //Hidden layer to Output
            for (int i = 0; i < hiddenNodes.length; i++){
                outputNode += hiddenNodes[i] * hiddenToOutWeights[i];
            }

            outputNode = activationFunction(outputNode);

            return outputNode;
        }


        public double test(double[] inputVector){
            double[] hiddenNodes = new double[numHiddenNodes];
            return feedForward(inputVector, hiddenNodes);
        }

        public double testCrossValidation(double[] inputVector,  final double targetOutput){
            double[] hiddenNodes = new double[numHiddenNodes];
            double outputNode = feedForward(inputVector, hiddenNodes);
            double error = outputNode*(1 - outputNode) * (targetOutput - outputNode); 
            errorRateTest.add(error);
            return outputNode;
        }

        public void printWeights(){
            int k=0;
            for(int i = 0; i < photoRows*photoColumns; i++){
                System.out.println(setToWeights[k][i*(numInputNodes/10)] + " ");
                if (i != 0 && i % (numInputNodes/10) == 0){
                    k++;
                }
            }
        }

    }
}



