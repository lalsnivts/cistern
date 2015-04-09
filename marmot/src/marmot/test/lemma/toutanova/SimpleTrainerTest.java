package marmot.test.lemma.toutanova;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import junit.framework.AssertionFailedError;
import marmot.lemma.Instance;
import marmot.lemma.Lemmatizer;
import marmot.lemma.Lemmatizer.Result;
import marmot.lemma.LemmatizerTrainer;
import marmot.lemma.SimpleLemmatizerTrainer;
import marmot.lemma.SimpleLemmatizerTrainer.Options;
import marmot.morph.io.SentenceReader;
import marmot.util.Numerics;

import org.junit.Test;

public class SimpleTrainerTest {

	@Test
	public void moderateTest() {
		Options options = SimpleLemmatizerTrainer.Options.newInstance();
		runModerateTest(new SimpleLemmatizerTrainer(options), 98.41, 64.47);
	}
	
	@Test
	public void moderateUnseenTest() {
		Options options = SimpleLemmatizerTrainer.Options.newInstance();
		options.setHandleUnseen(true);
		runModerateTest(new SimpleLemmatizerTrainer(options), 98.41, 86.19);
	}
	
	@Test
	public void moderateUnseenPosTest() {
		Options options = SimpleLemmatizerTrainer.Options.newInstance();
		options.setHandleUnseen(true);
		options.setUsePos(true);
		runModerateTest(new SimpleLemmatizerTrainer(options), 99.84, 86.82);
	}
	
	protected String getResourceFile(String name) {
		return String.format("res:///%s/%s", "marmot/test/lemma", name);
	}
	
	protected List<Instance> getCopyInstances(List<Instance> instances) {
		List<Instance> new_instances = new LinkedList<>();
		for (Instance instance : instances) {
			if (instance.getForm().equals(instance.getLemma())) {
					new_instances.add(instance);
			}
		}
		return new_instances;
	}
	
	protected void runSmallTest(LemmatizerTrainer trainer, double train_acc, double test_acc) {
		runTest(trainer, train_acc, test_acc, "trn_sml.tsv");
	}
	
	protected void runModerateTest(LemmatizerTrainer trainer, double train_acc, double test_acc) {
		runTest(trainer, train_acc, test_acc, "trn_mod.tsv");
	}
	
	protected void runTest(LemmatizerTrainer trainer, double train_acc, double test_acc, String trainfile_name) {	
		String indexes = "form-index=4,lemma-index=5,tag-index=2,";
		String trainfile = indexes+ getResourceFile(trainfile_name);
		String testfile = indexes + getResourceFile("dev.tsv");
		
		List<Instance> training_instances = Instance.getInstances(new SentenceReader(trainfile));
		Lemmatizer lemmatizer = trainer.train(training_instances, null);
			
		assertAccuracy(lemmatizer, training_instances, train_acc);
		List<Instance> instances = Instance.getInstances(new SentenceReader(testfile));
		assertAccuracy(lemmatizer, instances, test_acc);
	}

	protected void assertAccuracy(Lemmatizer lemmatizer, Collection<Instance> instances, double min_accuracy) {
		Result result = Lemmatizer.Result.test(lemmatizer, instances);
		
		double accuracy = result.getTokenAccuracy();
		
		result.logAccuracy();
		//result.logErrors(100);
		
		if (!Numerics.approximatelyGreaterEqual(accuracy, min_accuracy)) {
			throw new AssertionFailedError(String.format("%g > %g", accuracy, min_accuracy));
		}		
	}
	
}