package i5.las2peer.services.story3DSemanticCheck;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;

import javax.ws.rs.Path;

import i5.cae.semanticCheck.SemanticCheckResponse;
import i5.cae.simpleModel.SimpleModel;
import i5.las2peer.api.Service;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.logging.NodeObserver.Event;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.annotations.Version;
import i5.las2peer.restMapper.tools.ValidationResult;
import i5.las2peer.restMapper.tools.XMLCheck;
import i5.las2peer.services.story3DSemanticCheck.x3dst.Story;
import io.swagger.annotations.Api;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;;

@Path("/3DST")
@Version("1.0") // this annotation is used by the XML mapper
@Api
@SwaggerDefinition(info = @Info(title = "3D Storytelling Semantic Check", version = "1.0", description = "Semantic check service for 3D Storytelling", termsOfService = "http://your-terms-of-service-url.com", contact = @Contact(name = "Jan Benscheid", url = "", email = "benscheid@dbis.rwth-aachen.com"), license = @License(name = "", url = "")))

// TODO Your own Serviceclass
public class Story3DSemanticCheck extends Service {

	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(Story3DSemanticCheck.class.getName());

	public Story3DSemanticCheck() {
		// read and set properties values
		// IF THE SERVICE CLASS NAME IS CHANGED, THE PROPERTIES FILE NAME NEED
		// TO BE CHANGED TOO!
		setFieldValues();
		
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	// Service methods.
	// //////////////////////////////////////////////////////////////////////////////////////
	  
	public SemanticCheckResponse doSemanticCheck(Serializable s) {

		try {
			Story story = new Story((SimpleModel) s);		
			SemanticCheckResponse res = story.validate();
			return res;
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	// Methods required by the LAS2peer framework.
	// //////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method for debugging purposes. Here the concept of restMapping validation
	 * is shown. It is important to check, if all annotations are correct and
	 * consistent. Otherwise the service will not be accessible by the
	 * WebConnector. Best to do it in the unit tests. To avoid being
	 * overlooked/ignored the method is implemented here and not in the test
	 * section.
	 * 
	 * @return true, if mapping correct
	 */
	public boolean debugMapping() {
		String XML_LOCATION = "./restMapping.xml";
		String xml = getRESTMapping();

		try {
			RESTMapper.writeFile(XML_LOCATION, xml);
		} catch (IOException e) {
			// write error to logfile and console
			logger.log(Level.SEVERE, e.toString(), e);
			// create and publish a monitoring message
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());
		}

		XMLCheck validator = new XMLCheck();
		ValidationResult result = validator.validate(xml);

		if (result.isValid()) {
			return true;
		}
		return false;
	}

	/**
	 * This method is needed for every RESTful application in LAS2peer. There is
	 * no need to change!
	 * 
	 * @return the mapping
	 */
	public String getRESTMapping() {
		String result = "";
		try {
			result = RESTMapper.getMethodsAsXML(this.getClass());
		} catch (Exception e) {
			// write error to logfile and console
			logger.log(Level.SEVERE, e.toString(), e);
			// create and publish a monitoring message
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());
		}
		return result;
	}
	
}
