package core;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import javax.json.Json;
import javax.json.JsonObject;

public class MessageConverter {
    //example message in years: {"ssn":"160578-9787","creditScore":598,"loanAmount":10.0,"loanDuration":6}
    // has to be turned in: {"ssn":1605789787,"creditScore":598,"loanAmount":10.0,"loanDuration":72}
    //because we need the loan duration in months

    //public static void main(args[]) throws JSONException {
    public String processMessage(String mes) throws JSONException {
        //mes = "{\"ssn\":\"160578-9787\",\"creditScore\":598,\"loanAmount\":10.0,\"loanDuration\":6}";
        JSONObject loan_json = new JSONObject(mes);
        //strip it from the dash and make it an integer
        int ssn = Integer.parseInt(loan_json.getString("ssn").replace("-", ""));
        System.out.println("SSN stripped: " + ssn );

        Double loanAmount = loan_json.getDouble("loanAmount");
        //convert years in months
        String loanDuration = loan_json.getString("loanDuration");
        int loanDurationFinal = Integer.parseInt(loanDuration)*12;
        System.out.println("LoanDuration: " + loanDuration);

        int creditScore = loan_json.getInt("creditScore");

        //add LoanRequest rapper so you have the correct format
        JsonObject message = Json.createObjectBuilder()
                        .add("ssn", ssn)
                        .add("creditScore", creditScore)
                        .add("loanAmount", loanAmount)
                        .add("loanDuration", loanDurationFinal)
                .build();

        System.out.println("Message processed: " + message);

        return message.toString();
    }

    private int processLoanDuration(String loanDuration){
        //int inYears = Integer.parseInt(loanDuration);
        return Integer.parseInt(loanDuration)*12;
    }

}
