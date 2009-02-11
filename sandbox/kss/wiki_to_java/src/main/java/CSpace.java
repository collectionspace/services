/**
 * CSpace.java
 */

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.kuali.student.mojo.*;

/**
 * @author remillet
 *
 */
public class CSpace {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// the contract file to transform
		File contractFile = null;
//		contractFile = new File("C:/dev/src/kss/maven-kscontract-plugin/maven-kscontract-plugin/contracts/ffservice");
		
		// the URL to the contract to transform
		URL contractURL = null;
		try {
//			contractURL = new URL("https://test.kuali.org/confluence/display/KULSTU/Authentication+Service");
			contractURL = new URL("http://wiki.collectionspace.org/display/collectionspace/wikitojavatestservice");
			
			//
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// where to store the transformed output
		File outputDirectory = 
			new File("C:/dev/src/kss/maven-kscontract-plugin/maven-kscontract-plugin/transforms");
		
		// XSLT file describing the transform
		File transformFile = 
			new File("C:/dev/src/kss/maven-kscontract-plugin/maven-kscontract-plugin/src/main/resources/interface.xml");
		
		// XSLT file describing the message transform
		File messageTransformFile = 
			new File("C:/dev/src/kss/maven-kscontract-plugin/maven-kscontract-plugin/src/main/resources/messageInterface.xml");
		
		// the session ID token
		String jsessionId = "53F14F3833498C0A43DA87B273D1DDB7.Kuali3_1Engine";
		
		
		
		KsContractMojo kssContractMojo = new KsContractMojo(
				contractFile,
				contractURL,
				outputDirectory,
				transformFile,
				messageTransformFile,
				jsessionId);
		
		try {
			kssContractMojo.execute();
		}
		catch (MojoExecutionException mee) {
			System.err.print(mee.toString());
		}
		catch (MojoFailureException mfe) {
			System.err.print(mfe.toString());
		}
		
	}

}
