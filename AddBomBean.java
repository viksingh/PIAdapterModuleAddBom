package com.adapter.usermodule;
//Classes for EJB
import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
// Classes for Module development & Trace
import com.sap.aii.af.lib.mp.module.*;
import com.sap.engine.interfaces.messaging.api.*;
import com.sap.engine.interfaces.messaging.api.auditlog.*;
import com.sap.engine.interfaces.messaging.api.exception.InvalidParamException;
import com.sap.tc.logging.*;
// XML parsing and transformation classes
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

//
//java ciphering
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;
public class AddBomBean implements SessionBean, Module {
  public static final String UTF8_BOM = "\uFEFF";	
	public static final String VERSION_ID = "$Id://tc/aii/30_REL/src/_adapters/_module/java/user/module/XMLElementEncrypt.java#1 $";
	static final long serialVersionUID = 7435850550539048631L;
	private SessionContext myContext;
	public void ejbRemove() {
	}
	public void ejbActivate() {
	}
	public void ejbPassivate() {
	}
	public void setSessionContext(SessionContext context) {
		myContext = context;
	}
	public void ejbCreate() throws CreateException {
	}

	public ModuleData process(ModuleContext moduleContext, ModuleData inputModuleData) throws ModuleException {

		Object obj = null;
		Message msg = null;
		MessageKey key = null;
		AuditAccess audit = null;		
		try {

			obj = inputModuleData.getPrincipalData();
			msg = (Message) obj;

			
			if (msg.getMessageDirection().equals(MessageDirection.OUTBOUND))
				key = new MessageKey(msg.getMessageId(), MessageDirection.OUTBOUND);
			else
				key = new MessageKey(msg.getMessageId(), MessageDirection.INBOUND);
			audit = PublicAPIAccessFactory.getPublicAPIAccess().getAuditAccess();
			audit.addAuditLogEntry(key, AuditLogStatus.SUCCESS, "AddBom: Module called");

		}
		catch (Exception e) {
			ModuleException me = new ModuleException(e);
			throw me;
		}

		obj = inputModuleData.getPrincipalData();
		msg = (Message) obj;
		XMLPayload xmlpayload = msg.getDocument();
		InputStream inputStream = (InputStream)xmlpayload.getInputStream();

		String stringFromStream = null;
		try {
			stringFromStream = IOUtils.toString(inputStream, "UTF-8");					

			if (stringFromStream.startsWith(UTF8_BOM)) {
				audit.addAuditLogEntry(key, AuditLogStatus.SUCCESS, "AddBom: Ignored - BOM Already Present");
			}
			else{
				stringFromStream = UTF8_BOM + stringFromStream; 					
				audit.addAuditLogEntry(key, AuditLogStatus.SUCCESS, "AddBom: BOM Added");            
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}				


		byte[] docContent = stringFromStream.getBytes();

		if(docContent != null) {
			try {
				xmlpayload.setContent(docContent);
			} catch (InvalidParamException e) {
				e.printStackTrace();
			}
			inputModuleData.setPrincipalData(msg);
			audit.addAuditLogEntry(key, AuditLogStatus.SUCCESS, "BOMAdd : Processing ending ****");								
		}

		return inputModuleData;
	}
}
