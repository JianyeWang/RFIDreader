package com.rfid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.lazy.IBk;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;
import weka.classifiers.*;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Attribute;
 
public class WekaDemo {
	
	public static BufferedReader readDataFile(String filename) {
		BufferedReader inputReader = null;
 
		try {
			inputReader = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException ex) {
			System.err.println("File not found: " + filename);
		}
 
		return inputReader;
	}
 
	public static Evaluation classify(Classifier model,
			Instances trainingSet, Instances testingSet) throws Exception {
		Evaluation evaluation = new Evaluation(trainingSet);
 
		model.buildClassifier(trainingSet);
		evaluation.evaluateModel(model, testingSet);
 
		return evaluation;
	}
 
	//calculate the models accuracy 
	public static double calculateAccuracy(FastVector predictions) {
		double correct = 0;
 
		for (int i = 0; i < predictions.size(); i++) {
			NominalPrediction np = (NominalPrediction) predictions.elementAt(i);
			if (np.predicted() == np.actual()) {
				correct++;
			}
		}
 
		return 100 * correct / predictions.size();
	}
	
	// calculate the final location prediction's accuracy
	public static void calculateResult(Instances unlabeled) {
		 // set class attribute
		 unlabeled.setClassIndex(unlabeled.numAttributes() - 1);

		 // create copy
		 Instances labeled = new Instances(unlabeled);

		 Classifier clsmodel;
		try {
			clsmodel = (Classifier) weka.core.SerializationHelper.read("src/res/permodel.model");
			int numyes,numno;
			 numyes = 0;
			 numno = 0;
			 double probability;
			 // label instances
			 for (int i = 0; i < unlabeled.numInstances(); i++) {
				 double clsLabel = clsmodel.classifyInstance(unlabeled.instance(i));
				 labeled.instance(i).setClassValue(clsLabel);
				 System.out.println(clsLabel + " -> " + unlabeled.classAttribute().value((int) clsLabel));
				 
				 if(clsLabel ==0.0){
					 numyes++;
				 }else{
					 numno++;
				 }
			 }
			 if(numyes>numno){
				 probability=100 * numyes/(numyes+numno);
				 System.out.println("yes "+numyes+" P:"+probability+"%");
			 }else{
				 probability=100 * numno/(numyes+numno);
				 System.out.println("no "+numno+" P: "+probability+"%");
			 }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
	}
 
	public static Instances createInstances(){
		
		 // Declare two numeric attributes
		 Attribute Attribute1 = new Attribute("rssi");
		 Attribute Attribute2 = new Attribute("frequency");
		 Attribute Attribute3 = new Attribute("phase");
		 
		 FastVector fvClassVal = new FastVector(2);
		 fvClassVal.addElement("yes");
		 fvClassVal.addElement("no");
		 Attribute ClassAttribute = new Attribute("incell", fvClassVal);
		 
		 FastVector fvWekaAttributes = new FastVector(4);
		 fvWekaAttributes.addElement(Attribute1);
		 fvWekaAttributes.addElement(Attribute2);
		 fvWekaAttributes.addElement(Attribute3);
		 fvWekaAttributes.addElement(ClassAttribute);
		 
		 Instances isTestset = new Instances("rfid", fvWekaAttributes, 20);
		 // Set class index
		 isTestset.setClassIndex(3);
		
		return isTestset;
		
	}
	
	public static void addMoreInstances(Instances isTestset,int rssi,int frequency, int phase){
		Instance iExample = new Instance(4);
		FastVector fvWekaAttributes = new FastVector(4);
		 iExample.setValue((Attribute)fvWekaAttributes.elementAt(0), rssi);
		 iExample.setValue((Attribute)fvWekaAttributes.elementAt(1), frequency);
		 iExample.setValue((Attribute)fvWekaAttributes.elementAt(2), phase);
		 iExample.setValue((Attribute)fvWekaAttributes.elementAt(3), "?");
		 // add the instance
		 isTestset.add(iExample);
	}
	
	public static Instances[][] crossValidationSplit(Instances data, int numberOfFolds) {
		Instances[][] split = new Instances[2][numberOfFolds];
 
		for (int i = 0; i < numberOfFolds; i++) {
			split[0][i] = data.trainCV(numberOfFolds, i);
			split[1][i] = data.testCV(numberOfFolds, i);
		}
 
		return split;
	}
 
	public static void main(String[] args) throws Exception {
		BufferedReader datafile = readDataFile("src/res/cell1-training data.arff");
 
		Instances data = new Instances(datafile);
		data.setClassIndex(data.numAttributes() - 1);
 
		// Do 2-split cross validation
		Instances[][] split = crossValidationSplit(data, 2);
 
		// Separate split into training and testing arrays
		Instances[] trainingSplits = split[0];
		Instances[] testingSplits = split[1];
 
		// Use a set of classifiers
		Classifier[] models = { 
				new J48(), // a decision tree
				new PART(), 
				new IBk(),
				new DecisionTable(),//decision table majority classifier
				new MultilayerPerceptron() 
		};

		// Run for each model
		for (int j = 0; j < models.length; j++) {
 
			// Collect every group of predictions for current model in a FastVector
			FastVector predictions = new FastVector();
 
			// For each training-testing split pair, train and test the classifier
			for (int i = 0; i < trainingSplits.length; i++) {
				Evaluation validation = classify(models[j], trainingSplits[i], testingSplits[i]);
 
				predictions.appendElements(validation.predictions());
 
				// Uncomment to see the summary for each training-testing pair.
				//System.out.println(models[j].toString());
			}
 
			// Calculate overall accuracy of current classifier on all splits
			double accuracy = calculateAccuracy(predictions);
 
			// Print current classifier's name and accuracy in a complicated,
			// but nice-looking way.
			System.out.println("Accuracy of " + models[j].getClass().getSimpleName() + ": "
					+ String.format("%.2f%%", accuracy)
					+ "\n---------------------------------");

			//this is for recording data to a txt file
//			String pathname = "/Users/Ben/Desktop/dataoutput.txt";
//			File writename = new File(pathname); 
//			writename.createNewFile();
//			DataWriter datawriter = new DataWriter();
//			datawriter.DataOutput(pathname, models[j].getClass().getSimpleName()+"\n");
//			datawriter.readFileByLines(pathname);
			
			}
		 Instances unlabeled = new Instances(
                 new BufferedReader(
                   new FileReader("src/res/cell1-testdata.arff")));

		 calculateResult(unlabeled);
	}
}
