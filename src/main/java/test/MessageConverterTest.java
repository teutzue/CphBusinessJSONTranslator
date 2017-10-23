package test;

import static org.junit.Assert.*;

import org.json.JSONException;
import org.junit.Test;

import core.MessageConverter;

public class MessageConverterTest {

	@Test
	public void testConvertion() throws JSONException {
		MessageConverter mc = new MessageConverter();
		String mes = "{\"ssn\":\"160578-9787\",\"creditScore\":598,\"loanAmount\":10.0,\"loanDuration\":6}";
		String processedMessage="{\"ssn\":1605789787,\"creditScore\":598,\"loanAmount\":10.0,\"loanDuration\":72}";
		assertEquals(processedMessage, mc.processMessage(mes));
	}

}
