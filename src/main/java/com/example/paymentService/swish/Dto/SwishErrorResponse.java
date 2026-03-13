package com.example.paymentService.swish.Dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * DTO för felhantering från Swish API
 * 
 * När Swish returnerar 422 Unprocessable Entity får vi en lista med fel.
 * Varje fel har en errorCode och ett errorMessage.
 * 
 * Exempel på felkoder:
 * - FF08: Invalid payeePaymentReference
 * - RP03: Callback URL missing or not HTTPS
 * - BE18: Invalid payer alias
 * - PA02: Amount missing or invalid
 * - AM06: Amount below minimum
 * - ACMT03: Payer not enrolled
 * - RP09: InstructionUUID already exists
 * - TM01: Timeout
 * 
 * Enligt Swish dokumentation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SwishErrorResponse {

    private List<SwishError> errors;

    public List<SwishError> getErrors() {
        return errors;
    }

    public void setErrors(List<SwishError> errors) {
        this.errors = errors;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SwishError {
        private String errorCode; // Error code (FF08, RP03, etc.)
        private String errorMessage; // Human-readable error message
        private String additionalInformation; // Extra info

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getAdditionalInformation() {
            return additionalInformation;
        }

        public void setAdditionalInformation(String additionalInformation) {
            this.additionalInformation = additionalInformation;
        }

        @Override
        public String toString() {
            return "SwishError{" +
                    "errorCode='" + errorCode + '\'' +
                    ", errorMessage='" + errorMessage + '\'' +
                    ", additionalInformation='" + additionalInformation + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "SwishErrorResponse{" +
                "errors=" + errors +
                '}';
    }
}
