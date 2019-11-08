package TextGenerator;

import java.util.Scanner;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.lang.*;

public class TextGenerator {

	private double[][] output;
	private double[][][] weights;
	private double[][] bias;

	private double[][] errorSignal;
	private double[][] outputDeriv;

	public final int[] networkLayerSizes;
	public final int inputSize;
	public final int networkSize;
	public final int outputSize;

	public TextGenerator(int... networkLayerSizes) {
		this.networkLayerSizes = networkLayerSizes;
		this.inputSize = networkLayerSizes[0];
		this.networkSize = networkLayerSizes.length;
		this.outputSize = networkLayerSizes[networkSize - 1];

		this.output = new double[networkSize][];
		this.weights = new double[networkSize][][];
		this.bias = new double[networkSize][];

		this.errorSignal = new double[networkSize][];
		this.outputDeriv = new double[networkSize][];

		for (int i = 0; i < networkSize; i++) {
			this.output[i] = new double[networkLayerSizes[i]];
			this.bias[i] = new double[networkLayerSizes[i]];

			this.errorSignal[i] = new double[networkLayerSizes[i]];
			this.outputDeriv[i] = new double[networkLayerSizes[i]];

			if (i > 0) {
				weights[i] = new double[networkLayerSizes[i]][networkLayerSizes[i - 1]];
			}
		}
	}

	public double[] calculate(double... input) {
		if (input.length != inputSize)
			return null;

		this.output[0] = input;

		for (int layer = 1; layer < networkSize; layer++) {
			for (int neuron = 0; neuron < networkLayerSizes[layer]; neuron++) {

				double sum = bias[layer][neuron];

				for (int prevNeuron = 0; prevNeuron < networkLayerSizes[layer - 1]; prevNeuron++) {
					sum += output[layer - 1][prevNeuron] * weights[layer][neuron][prevNeuron];
				}

				output[layer][neuron] = sigma(sum);
				outputDeriv[layer][neuron] = output[layer][neuron] * (1 - output[layer][neuron]);
			}
		}

		return output[networkSize - 1];
	}

	public void train(double[] input, double[] target, double eta) {// eta =
																	// learning
																	// rate
		if (input.length != inputSize || target.length != outputSize)
			return;

		calculate(input);
		backpropError(target);
		updateWeights(eta);
	}

	public void backpropError(double[] target) {
		for (int neuron = 0; neuron < networkLayerSizes[networkSize - 1]; neuron++) {
			errorSignal[networkSize - 1][neuron] = (output[networkSize - 1][neuron] - target[neuron])
					* outputDeriv[networkSize - 1][neuron];
		}

		for (int layer = networkSize - 2; layer > 0; layer--) {
			for (int neuron = 0; neuron < networkLayerSizes[layer]; neuron++) {
				double sum = 0;
				for (int nextNeuron = 0; nextNeuron < networkLayerSizes[layer + 1]; nextNeuron++) {
					sum += weights[layer + 1][nextNeuron][neuron] * errorSignal[layer + 1][nextNeuron];
				}
				this.errorSignal[layer][neuron] = sum * outputDeriv[layer][neuron];
			}
		}
	}

	public void updateWeights(double eta) {
		for (int layer = 1; layer < networkSize; layer++) {
			for (int neuron = 0; neuron < networkLayerSizes[layer]; neuron++) {
				for (int prevNeuron = 0; prevNeuron < networkLayerSizes[layer - 1]; prevNeuron++) {
					double delta = -eta * output[layer - 1][prevNeuron] * errorSignal[layer][neuron];
					weights[layer][neuron][prevNeuron] += delta;
				}
				double delta = -eta * errorSignal[layer][neuron];
				bias[layer][neuron] += delta;
			}
		}
	}

	public double sigma(double input) {
		return 1d / (1 + Math.pow(Math.E, (-input)));
	}

	public static char[] startArr(int history, FileReader read) throws IOException {
		char charArr[] = new char[history];
		int c;

		for (int i = 0; i < history; i++)
			charArr[i] = Character.toLowerCase((char) (c = read.read()));

		return charArr;
	}

	public static double[] calcOccurPercent(int occurrenceArr[]) {
		double occurPercent[] = new double[occurrenceArr.length];
		int length = occurrenceArr.length;

		for (int i = 0; i < length; i++)
			occurPercent[i] = occurrenceArr[i] / length;

		return occurPercent;
	}

	private static int[] characterCount(char charArr[], int totalChar) {
		int charCount[] = new int[totalChar];

		for(int i = 0; i < charArr.length; i++){
			int ASCIIEq = (int)charArr[i];
			//32 = space, 46 = period, 10 = /n
			switch(ASCIIEq){
			case 10:
				charArr[25]++;
			case 32:
				charArr[26]++;
			case 46:
				charArr[27]++;
			default:
				charArr[ASCIIEq-97]++;
			}	
		}
		
		return charCount;
	}
	
	public static double[] nextCharArr(char c, int location, int length){
		double outputArr[] = new double[length];
		
		for(int i = 0; i < length; i++){
			if(i == location) outputArr[i] = 1.0;
			else outputArr[i] = 0;		
		}
		
		return outputArr;
	}

	public static char[] shiftVals(char charArr[], char newChar) {
		char newCharArr[] = new char[charArr.length];

		for (int i = 0; i < charArr.length - 1; i++) {
			newCharArr[i] = charArr[i + 1];
		}

		newCharArr[charArr.length - 1] = newChar;

		return newCharArr;
	}

	public static void main(String[] args) throws IOException {
		Scanner scan = new Scanner(System.in);

		int TOTAL_CHAR_COUNT = 289;

		TextGenerator net = new TextGenerator(TOTAL_CHAR_COUNT, 30, 30, 30, TOTAL_CHAR_COUNT);

		// input is just 26 characters, space, nextline and period
		// output is the same as input

		int history = 10;
		FileReader inputStream = new FileReader("./src/data.txt");
		int c;

		char historyArr[] = startArr(history, inputStream);

		// characterCount(historyArr);

		while ((c = inputStream.read()) != -1) {
			System.out.println(Arrays.toString(historyArr));
			
			// convert to array of times char occurred
			// convert to array of percentage of char in history
			double input[] = calcOccurPercent(characterCount(historyArr, TOTAL_CHAR_COUNT));

			//double target[] = nextCharArr(c, loc, TOTAL_CHAR_COUNT);
			
			// net.train(input, target, 0.3);
			historyArr = shiftVals(historyArr, Character.toLowerCase((char) c));
		}

		/*
		 * for(int i = 0; i < amount; i++){ if(i%10000 == 0)
		 * System.out.println(((float)i/amount)*100 + "%"); }
		 */

		// }

	}

}
