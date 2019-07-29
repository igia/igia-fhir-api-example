/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v.
 * 2.0 with a Healthcare Disclaimer.
 * A copy of the Mozilla Public License, v. 2.0 with the Healthcare Disclaimer can
 * be found under the top level directory, named LICENSE.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * If a copy of the Healthcare Disclaimer was not distributed with this file, You
 * can obtain one at the project website https://github.com/igia.
 *
 * Copyright (C) 2018-2019 Persistent Systems, Inc.
 */
package io.igia.example.fhir.fhir.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import io.igia.example.fhir.fhir.camel.RouteConstants;

@Component
public class PatientResourceProvider implements IResourceProvider {
    private ProducerTemplate producerTemplate;

    public PatientResourceProvider(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }
    
    @Read()
    public Patient getResourceById(@IdParam IdType theId) {
        if (null == theId || StringUtils.isEmpty(theId.getIdPart())) {
            throw new InvalidRequestException("Missing id.");
        }
        
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(Exchange.HTTP_PATH, "/" + theId.getIdPart());
        Patient patient = producerTemplate.requestBodyAndHeaders(RouteConstants.PATIENT_READ_ROUTE, null, headers, Patient.class);
    	    
    	return patient;
    }

    @Search()
    public List<Patient> search(@OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
    		@OptionalParam(name = Patient.SP_GIVEN) StringParam first,
    		@OptionalParam(name = Patient.SP_FAMILY) StringParam last,
    		@OptionalParam(name = Patient.SP_GENDER) TokenParam gender,
    		@OptionalParam(name = Patient.SP_BIRTHDATE) DateParam birthdate) {
        return findPatientByIdentifier(identifier, first, last, gender, birthdate);
    }
    
    private List<Patient> findPatientByIdentifier(final TokenParam identifier,
    		final StringParam first,
    		final StringParam last,
    		final TokenParam gender,
    		final DateParam birthdate) {

        validateRequestParams(identifier, first, last, gender, birthdate);
        Map<String, Object> headers = new HashMap<String, Object>();              
        List<String> query = new ArrayList<String>();
        
        String value = validateValue(identifier);
        if(!value.isEmpty()) { query.add("mrn=" + value);}
        value = validateValue(first);
        if(!value.isEmpty()) { query.add("first=" + value);}
        value = validateValue(last);
        if(!value.isEmpty()) { query.add("last=" + value);}
        value = validateValue(gender);
        if(!value.isEmpty()) { query.add("gender=" + value);}
        value = validateValue(birthdate);
        if(!value.isEmpty()) { query.add("birthdate=" + value);}
        
        headers.put(Exchange.HTTP_QUERY, String.join("&", query));
        List<Patient> retVal = producerTemplate.requestBodyAndHeaders(RouteConstants.PATIENT_SEARCH_ROUTE, null, headers, List.class);
        
        return (List<Patient>) retVal;
    }

    private void validateRequestParams(final TokenParam identifier,
    		final StringParam first,
    		final StringParam last,
    		final TokenParam gender,
    		final DateParam birthdate) {
        if (null == identifier
        		&& null == first
        		&& null == last
        		&& null == gender
        		&& null == birthdate) {
            throw new InvalidRequestException("Missing search parameters.");
        }
    }
    
    private String validateValue(final TokenParam param) {
    	if(null == param) {
    		return "";
    	}
        if (param.getValue().isEmpty()) {
            throw new InvalidRequestException("Invalid parameter. Missing value.");
        }
        
        return param.getValue();
    }
    
    private String validateValue(final StringParam param) {
    	if(null == param) {
    		return "";
    	}
        if (param.getValue().isEmpty()) {
            throw new InvalidRequestException("Invalid parameter. Missing value.");
        }
        
        return param.getValue();
    }
    
    private String validateValue(final DateParam param) {
    	if(null == param) {
    		return "";
    	}
        if (null == param.getValue()) {
            throw new InvalidRequestException("Invalid parameter. Missing value.");
        }
        
        return param.getValueAsDateTimeDt().getYear().toString() + 
        		"-" + ((Integer)(param.getValueAsDateTimeDt().getMonth() + 1)).toString() +
        		"-" + param.getValueAsDateTimeDt().getDay().toString();
    }
}
