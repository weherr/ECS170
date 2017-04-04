
import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

public class Main
{
    final static int photoRows = 128; // Number of rows of greyscale numbers
    final static int photoColumns = 120; // Number of columns of greyscale numbers
    final static double eta = 0.1; //learning rate
    final static int numInputNodes = photoRows*photoColumns;
    final static int numHiddenNodes = 10;
    final static double maleTargetOutput = 0.9;
    final static double femaleTargetOutput = 0.35;
    final static int iterations = 60;

    public static void main(String[] args) 
    {
        File maleFiles;
        File femaleFiles;
        File testFiles;
        ArrayList<File> maleList;
        ArrayList<File> femaleList;
        ArrayList<File> testList;

        int a = 0;
        while(a < args.length)
        {
            if(args[a].equalsIgnoreCase("-train")){
                maleFiles = new File(args[a+1]);
                femaleFiles = new File(args[a+2]);
                maleList = new ArrayList<File>(Arrays.asList(maleFiles.listFiles()));
                femaleList = new ArrayList<File>(Arrays.asList(femaleFiles.listFiles()));
                NeuralNetwork valhallaNN = new NeuralNetwork();

                ArrayList<File> allList = new ArrayList<File>();
                allList.addAll( maleList );
                allList.addAll( femaleList );
                Collections.shuffle(allList);
                double output;

                for(int i = 0; i < iterations; i++){
                    System.out.println("All iteration:" + i);
                    for(File file : allList) {
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
                valhallaNN.saveWeights();
            }
            else if (args[a].equalsIgnoreCase("-test"))
            {  
                testFiles = new File(args[a+1]);
                testList = new ArrayList<File>(Arrays.asList(testFiles.listFiles()));
                File savedInputWeights = new File("savedInputWeights.txt");
                File savedOutputWeights = new File("savedOutputWeights.txt");
                NeuralNetwork valhallaNN = new NeuralNetwork(savedInputWeights, savedOutputWeights);

                for(File file : testList) {
                    double[] inputVector = readFile(file);
                    System.out.println(file.toString() + valhallaNN.test(inputVector));
                }
            }
        }
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

        public NeuralNetwork(File savedInputWeights, File savedOutputWeights) {
            //number of edges between input and hidden layer 1536 partially connected

            setToWeights = new double[numHiddenNodes][numInputNodes/10];
            hiddenToOutWeights = new double[numHiddenNodes];
            try{ 
                Scanner scanIn = new Scanner(savedInputWeights); 
                Scanner scanOut = new Scanner(savedOutputWeights);
                for (int k=0; k < 10; k++){
                    for (int i=0; i < numInputNodes/10; i++){
                        try{
                            setToWeights[k][i] = (double)scanIn.nextInt();
                        }
                        catch(NoSuchElementException e){}
                    }
                }
                for (int h=0; h < hiddenToOutWeights.length; h++){
                    try{
                        hiddenToOutWeights[h] = (double)scanOut.nextInt();
                    }
                    catch(NoSuchElementException e){}
                }
            }
            catch (FileNotFoundException e) {}
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

        public void saveWeights(){
            try{
                FileWriter fw1;
                BufferedWriter bw1;
                FileWriter fw2;
                BufferedWriter bw2;
                fw1 = new FileWriter("savedInputWeights.txt");
                bw1 = new BufferedWriter(fw1);
                fw2 = new FileWriter("savedOutputWeights.txt");
                bw2 = new BufferedWriter(fw2);

                int k=0;
                String inputWeights = "";
                String outputWeights= "";
                for(int i = 0; i < photoRows*photoColumns; i++){
                    inputWeights += Double.toString(setToWeights[k][i%(numInputNodes/10)]) + " ";
                    if (i != 0 && i % (numInputNodes/10) == 0){
                        k++;
                    }
                }
                for (int h=0; h < hiddenToOutWeights.length; h++){
                    outputWeights += Double.toString(hiddenToOutWeights[h]) + " ";
                }
                bw1.write(inputWeights);
                fw1.close();
                bw1.close();
                bw2.write(outputWeights);
                fw2.close();
                bw2.close();
            }
            catch(IOException e){}
        }

    }
}



