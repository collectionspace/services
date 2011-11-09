package org.collectionspace.services.common.vocabulary;

public class VocabManager {
	static private final IVocabManager vocabManager = new VocabManagerImpl();
	
	static public void exampleMethod(String someParam) {
		vocabManager.exampleMethod(someParam);
	}
}
