package edu.cuny.qc.perceptron.core;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

import edu.cuny.qc.ace.acetypes.AceMention;
import edu.cuny.qc.perceptron.types.FeatureVector;
import edu.cuny.qc.perceptron.types.SentenceAssignment;
import edu.cuny.qc.perceptron.types.SentenceInstance;
import edu.cuny.qc.perceptron.types.SentenceInstance.InstanceAnnotations;
import edu.cuny.qc.util.TokenAnnotations;
import edu.cuny.qc.util.TypeConstraints;
import edu.cuny.qc.util.Utils;

/**
 * In this beam search, we use strict beam to search trigger and arguments
 * in each state:
 * 		(1) determine trigger labeling in beam
 * 		(2) determine argument labeling in beam for each entity candidate
 * Hopefully, this beam search can be more time efficient than BeamSearch class
 * @author che
 *
 */
public class BeamSearch
{
	Perceptron model;
	boolean isTraining = true;
	private PrintStream b;
	
	private static final boolean PRINT_BEAM = false;
	private static final boolean PRINT_BEAM_DETAILED = false;
	
	static {
		System.err.println("??? BeamSearch: Calcing target's features (node, edge, global), multiple times here, even though they are needed only when there's a violation, Qi approves. Consider changing.");
		System.err.println("??? BeamSearch: In ScoreComparator, can try to use the compareTo() of both BigDecimals (I temporarily converted back to working on doubles, due to consistency problems with master).");
	}
	
	protected FeatureVector getWeights()
	{
		if(!isTraining && model.controller.avgArguments)
		{
			return model.getAvg_weights();
		}
		else
		{
			return model.getWeights();
		}
	}
	
	public BeamSearch(Perceptron model, boolean isTraining)
	{
		this.model = model;
		this.isTraining = isTraining;
		
		if (this.isTraining) {
			String beamsOutputFilePath = Pipeline.modelFile.getParent() + "/AllBeams-master.tsv";
			this.b = null;
			try {
				this.b = new PrintStream(beamsOutputFilePath);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			Utils.print(b, "", "\n", "|",		
					//general
					"Iter",
					"SentenceNo",
					"exit",
					"beam-size",
					
					//assignment
					"pos",
					"assignment", //toString()
					"score",
					"state",
					
					//token
					"i",
					"Lemma",
					"target-label",
					"assn-label",
					"partial-score",
					
					//feature
					"Feature",
					"Weight",
					"AvgWeight"
			);
		}
	}
	
	public void printBeam(PrintStream out, SentenceInstance instance, List<SentenceAssignment> beam, String exit) {
		if (this.isTraining) {
			for (int pos=0; pos<beam.size(); pos++) {
				SentenceAssignment assn = beam.get(pos);
				
				String posStr = Integer.toString(pos);
				if (pos == 0) {
					posStr = "0(Best)";
				}
				else if (pos == beam.size()-1) {
					posStr = "" + pos + "(Worst)";
				}
	
				List<Map<Class<?>, Object>> tokens = (List<Map<Class<?>, Object>>) instance.get(InstanceAnnotations.Token_FEATURE_MAPs);
				for (int j=0; j<=assn.getState(); j++) {
					String lemma = (String) tokens.get(j).get(TokenAnnotations.LemmaAnnotation.class);
					
					Map<Object, BigDecimal> mapAssn = assn.getFeatureVectorSequence().get(j).getMap();
					List<String> allFeaturesList = new ArrayList<String>(mapAssn.size());
					for (Object o : mapAssn.keySet()) {
						allFeaturesList.add((String) o);
					}
					Collections.sort(allFeaturesList);
	
					for (String s : allFeaturesList) {						
						Utils.print(b, "", "\n", "|",		
								//general
								Perceptron.iter,//"Iter",
								Perceptron.i,//"SentenceNo",
								exit,//"exit",
								beam.size(),//"beam-size",
								
								//assignment
								posStr, //"pos",
								assn,//"assignment", //toString()
								assn.getScore(),//"score",
								assn.getState(),//"state",
								
								//token
								j,//"i",
								lemma,//"Lemma",
								instance.target.getLabelAtToken(j),//"target-label",
								assn.getLabelAtToken(j),//"assn-label",
								assn.getPartialScores().get(j),//"partial-score",
								
								//feature
								Perceptron.feature(s),//"Feature",
								Perceptron.str(model.getWeights(), s),//"Weight",
								Perceptron.str(model.getAvg_weights(), s)//"AvgWeight"
						);
					}
					
					
					Utils.print(b, "", "\n", "|",		
							//general
							Perceptron.iter,//"Iter",
							Perceptron.i,//"SentenceNo",
							exit,//"exit",
							beam.size(),//"beam-size",
							
							//assignment
							posStr, //"pos",
							assn,//"assignment", //toString()
							assn.getScore(),//"score",
							assn.getState(),//"state",
							
							//token
							j,//"i",
							lemma,//"Lemma",
							instance.target.getLabelAtToken(j),//"target-label",
							assn.getLabelAtToken(j),//"assn-label",
							assn.getPartialScores().get(j),//"partial-score",
							
							//feature
							"",//"Feature",
							"",//"Weight",
							""//"AvgWeight"
					);
				}
				Utils.print(b, "", "\n", "|",		
						//general
						Perceptron.iter,//"Iter",
						Perceptron.i,//"SentenceNo",
						exit,//"exit",
						beam.size(),//"beam-size",
						
						//assignment
						posStr, //"pos",
						assn,//"assignment", //toString()
						assn.getScore(),//"score",
						assn.getState(),//"state",
						
						//token
						"",//"i",
						"",//"Lemma",
						"",//"target-label",
						"",//"assn-label",
						"",//"partial-score",
						
						//feature
						"",//"Feature",
						"",//"Weight",
						""//"AvgWeight"
				);

			}
		}
	}
	
	public SentenceAssignment beamSearch(SentenceInstance problem, int beamSize, boolean isLearning)
	{
		List<SentenceAssignment> beam = new ArrayList<SentenceAssignment>();
		SentenceAssignment initial = new SentenceAssignment(problem.nodeTargetAlphabet, problem.edgeTargetAlphabet, problem.featureAlphabet, problem.controller);
		beam.add(initial);
		
		// clear the feature vector of ground-truth assignment
		problem.target.clearFeatureVectors();
		
		for(int i=0; i<problem.size(); i++)
		{
			// create a container for sucessor 
			List<SentenceAssignment> successor = new ArrayList<SentenceAssignment>();
			// go through each partial assignment in previous beam to get successors for each one
			for(SentenceAssignment assn : beam)
			{
				List<SentenceAssignment> partial_successor = expandTrigger(problem, assn, i, isLearning);
				if(partial_successor != null)
				{
					successor.addAll(partial_successor);
				}
			}		
			// evaluate all successors only consider trigger labeling, and then select beam
			problem.target.makeNodeFeatures(problem, i, false, model.controller.addNeverSeenFeatures, model);
			if(problem.controller.useGlobalFeature)
			{
				problem.target.makeGlobalFeaturesTrigger(problem, i, false, model.controller.addNeverSeenFeatures);
			}
			for(SentenceAssignment assn : successor)
			{
				// make basic bigram features for event trigger
				assn.makeNodeFeatures(problem, i, false, model.controller.addNeverSeenFeatures, model);
				// evaluate the score of the assignment
				if(problem.controller.useGlobalFeature)
				{
					assn.makeGlobalFeaturesTrigger(problem, i, false, model.controller.addNeverSeenFeatures);
				}
				evaluate(assn, getWeights());
				
				// DEBUG
//				List<Token> textAnnos = (List<Token>) problem.get(InstanceAnnotations.TokenAnnotations);
//				Token token = textAnnos.get(i);
//				System.out.printf("\n- Lemma[%d]: %s [%s]\n", i, token.getLemma().getValue(), problem.text.replace('\n', ' '));
//				System.out.printf("- Weights: [%s]\n", getWeights().toStringFull(true));
//				System.out.printf("- Assn: %s\n", assn);
//				System.out.printf("  * assn.fv: [%s]\n", assn.getCurrentFV().toStringFull(true));
//				System.out.printf("  * assn.score: %s\n", assn.getScore());
				////
			}
			
			//DEBUG
			if (PRINT_BEAM_DETAILED) {
				System.out.printf("*** i=%s, pre-sort:\n", i);
				for (int q=0; q<successor.size(); q++) {
					SentenceAssignment a = successor.get(q);
					System.out.printf("  %d. score=%s : %s\n", q, a.getScore(), a);
				}
			}
			//////////
			
			// rank according to score
			Collections.sort(successor, new ScoreComparator());

			//DEBUG
			if (PRINT_BEAM_DETAILED) {
				System.out.printf("******** i=%s, post-sort:\n", i);
				for (int q=0; q<successor.size(); q++) {
					SentenceAssignment a = successor.get(q);
					System.out.printf("  %d. score=%s : %s\n", q, a.getScore(), a);
				}
			}
			//////////
			beam = successor.subList(0, Math.min(successor.size(), beamSize));
			
			// check early violation
			if(isLearning)
			{	
				boolean violation = problem.violateGoldStandard(beam, -1); // only consider the trigger labeling
				if(violation)
				{
					beam.get(0).setViolate(true);
					printBeam(b, problem, beam, "trigger violation");
					return beam.get(0);
				}
			}
			
			// expand the arguments for assignments in beam
			for(int k=0; k<problem.eventArgCandidates.size(); k++)
			{
				successor = new ArrayList<SentenceAssignment>();
				for(SentenceAssignment assn : beam)
				{
					if(SentenceAssignment.isArgumentable(assn.getCurrentNodeLabel()))
					{
						List<SentenceAssignment> partial_successor = expandArg(problem, assn, i, k, isLearning);
						if(partial_successor != null)
						{
							successor.addAll(partial_successor);
						}
						else
						{
							// if there is no sucessor on this entity, then add the assn to the successor
							successor.add(assn);
						}
					}
					else
					{
						successor.add(assn);
					}
				}
				problem.target.makeEdgeLocalFeature(problem, i, false, k, model.controller.addNeverSeenFeatures, model);
				if(problem.controller.useGlobalFeature)
				{
					// in each step of argument expansion, feed global feature if exists
					problem.target.makeGlobalFeaturesProgress(problem, i, k, false, model.controller.addNeverSeenFeatures);
					if(k == problem.eventArgCandidates.size() - 1)
					{
						problem.target.makeGlobalFeaturesComplete(problem, i, false, model.controller.addNeverSeenFeatures);
					}
				}
				
				for(SentenceAssignment assn : successor)
				{
					// fill in local edge feature for the new argument
					assn.makeEdgeLocalFeature(problem, i, false, k, model.controller.addNeverSeenFeatures, model);
					if(problem.controller.useGlobalFeature)
					{
						assn.makeGlobalFeaturesProgress(problem, i, k, false, model.controller.addNeverSeenFeatures);
						if(k == problem.eventArgCandidates.size() - 1)
						{
							assn.makeGlobalFeaturesComplete(problem, i, false, model.controller.addNeverSeenFeatures);
						}
					}
					// evaluate the score of the assignment
					evaluate(assn, getWeights());
				}
				
				//DEBUG
				if (PRINT_BEAM_DETAILED) {
					System.out.printf("*** i=%s, k=%s, pre-sort:\n", i, k);
					for (int q=0; q<successor.size(); q++) {
						SentenceAssignment a = successor.get(q);
						System.out.printf("  %d. score=%s : %s\n", q, a.getScore(), a);
					}
				}
				//////////
				
				// rank according to score
				Collections.sort(successor, new ScoreComparator());

				//DEBUG
				if (PRINT_BEAM_DETAILED) {
					System.out.printf("******** i=%s, k=%s, post-sort:\n", i, k);
					for (int q=0; q<successor.size(); q++) {
						SentenceAssignment a = successor.get(q);
						System.out.printf("  %d. score=%s : %s\n", q, a.getScore(), a);
					}
				}
				//////////

				beam = successor.subList(0, Math.min(successor.size(), beamSize));
				// System.out.println("sucessor size 2: " + successor.size());
				if(isLearning)
				{	
					boolean violation = problem.violateGoldStandard(beam, k); // only consider the trigger labeling and k-th argument labeling
					if(violation)
					{
						beam.get(0).setViolate(true);
						printBeam(b, problem, beam, "arg violation");
						return beam.get(0);
					}
				}
			} // end of expanding args
		}
		
		// check if final result is correct
		if(isLearning && problem.violateGoldStandard(beam.get(0)))
		{
			beam.get(0).setViolate(true);
		}
		
		if (PRINT_BEAM) {
			System.out.printf("Beam at the end:\n");
			for (int i=0; i<beam.size(); i++) {
				System.out.printf("%d. [%f] %s\n", i, beam.get(i).getScore(), beam.get(i));
			}
		}
		printBeam(b, problem, beam, "no violation");
		return beam.get(0);
	}
	
	/**
	 * given a assignment in the beam, expand the 1-best argument labeling for i-th token and k-th entity in the sentence
	 * @param problem
	 * @param assn
	 * @param i
	 * @param k
	 * @param isLearning
	 * @return
	 */
	protected List<SentenceAssignment> expandArg(SentenceInstance problem, SentenceAssignment assn, int i, int k, boolean isLearning)
	{
		String currentNodeLabel = assn.getCurrentNodeLabel();
		// make sucessors of assn by expanding the possible argument links
		List<SentenceAssignment> sucessor = new ArrayList<SentenceAssignment>();
		
		AceMention mention = problem.eventArgCandidates.get(k);
		//TODO DEBUG
		if (mention.getType().toLowerCase().contains("time")) {
			//System.err.println("We've got a time arg candidate!");
		}
		//////////
		if(!TypeConstraints.isEntityTypeEventCompatible(currentNodeLabel, mention.getType()))
		{
			return null;
		}
		// for a compatible mention, create an individual labeling
		for(int l=0; l<problem.edgeTargetAlphabet.size(); l++)
		{	
			String edgeLabel = (String) problem.edgeTargetAlphabet.lookupObject(l);
			// skip the mentions that is not compatible with the current node label (trigger type)
			if(!isRoleCompatible(mention.getType(), currentNodeLabel, edgeLabel))
			{
				continue;
			}
			// make local score for (k,l) link
			SentenceAssignment child = assn.clone();
			child.setCurrentEdgeLabel(k, l);
			sucessor.add(child);
		}
		return sucessor;
	}

	/**
	 * given the current state, expand its sucessors for trigger labeling
	 * @param problem 
	 * @param root_assn
	 * @param successor
	 * @param i the index of the current token to be processed
	 */
	protected List<SentenceAssignment> expandTrigger(SentenceInstance problem, SentenceAssignment root_assn, int i, boolean addIfNotPresent)
	{	
		List<SentenceAssignment> successor = new ArrayList<SentenceAssignment>();
		
		// in this function, rules out words that are not one of (Verb, Noun, Adj)
		if(!TypeConstraints.isPossibleTriggerByPOS(problem, i) || !TypeConstraints.isPossibleTriggerByEntityType(problem, i))
		{
			SentenceAssignment assn = root_assn.clone();
			assn.incrementState();
			assn.setCurrentNodeLabel(SentenceAssignment.Default_Trigger_Label);
			successor.add(assn);
			return successor;
		}
		
		String previousLabel = root_assn.getCurrentNodeLabel();
		List<String> nextLabels = nextLabels(previousLabel);
		
		// traverse all possible target alphabet (trigger labels) for the current token
		for(int j=0; nextLabels != null && j<nextLabels.size(); j++)
		{
			String outcome = nextLabels.get(j);
			// create a new assn as a successor 
			SentenceAssignment assn = root_assn.clone();
			assn.incrementState();
			assn.setCurrentNodeLabel(outcome);
			successor.add(assn);
		}
		// traverse all possible target alphabet (trigger labels) for the current token
		/*
		for(int j=0; j<model.nodeTargetAlphabet.size(); j++)
		{
			// create a new assn as a successor 
			SentenceAssignment assn = root_assn.clone();
			assn.incrementState();
			assn.setCurrentNodeLabel(j);
			successor.add(assn);
		}
		*/	
		return successor;
	}
	
	/**
	 * this is to evaluate the cost (credits/score/probability) of a partial results in the beam search
	 * @param partial
	 * @param problem
	 * @return
	 */
	static protected BigDecimal evaluate(SentenceAssignment partial, FeatureVector weights)
	{
		partial.updateScoreForNewState(weights);
		return partial.getScore();
	}
	
	/**
	 * get a list of possible next trigger label, this is learnt from all training data
	 * @param previousLabel
	 * @return
	 */
	protected List<String> nextLabels(String previousLabel)
	{
		return model.getLabelBigram().get(previousLabel);
	}
	
	/**
	 * check if ace mention is compatible with the event type and argument role in the
	 * current hypothesis
	 * @param edgeLabel 
	 * @param type
	 * @param currentNodeLabel
	 * @return
	 */
	protected static boolean isRoleCompatible(String mention_type, String triggerType, String edgeLabel)
	{
		if(edgeLabel.equals(SentenceAssignment.Default_Argument_Label) || 
				(TypeConstraints.isRoleCompatible(triggerType, edgeLabel) && TypeConstraints.isEntityTypeCompatible(edgeLabel, mention_type)))
		{
			return true;
		}
		return false;
	}
	
	public static class ScoreComparator implements Comparator<SentenceAssignment>
	{
		@Override
		public int compare(SentenceAssignment assn1, SentenceAssignment assn2)
		{
			double score1 = assn1.getScore().doubleValue();
			double score2 = assn2.getScore().doubleValue();
			if(Math.abs(score1 - score2) < 0.00001)
			{
				return 0;
			}
			if(score1 > score2)
			{
				return -1;
			}
			else 
			{
				return 1;
			}
		}	
	}
}
