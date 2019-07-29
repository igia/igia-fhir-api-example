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
package io.igia.example.fhir.fhir.camel.patient;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.camel.Converter;
import org.apache.camel.TypeConverters;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.DateType;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.exceptions.FHIRException;
import org.springframework.stereotype.Component;

import io.igia.example.fhir.fhir.domain.MockPatient;

@Component
@Converter
public class PatientConverter implements TypeConverters {

    /**
     * Convert Patient response to corresponding FHIR Patient model.
     *
     * @param reply
     * @return
     * @throws FHIRException 
     */
    @Converter
    public List<Patient> mapToPatientList(MockPatient[] patientResponse) throws ParseException, FHIRException {
        List<Patient> patients = new ArrayList<>();
        List<MockPatient> patientList = new ArrayList<>(Arrays.asList(patientResponse));
        if (patientList != null) {
            for (MockPatient patientRes : patientList) {
                Patient patient = new Patient();

                mapIdentifiers(patientRes, patient);
                mapPatientName(patientRes, patient);
                mapGender(patientRes, patient);
                mapBirthdate(patientRes, patient);

                patients.add(patient);
            }
        }
        return patients;
    }
    
    @Converter
    public Patient mapToPatient(MockPatient patientResponse) throws ParseException, FHIRException {
    	Patient patient = new Patient();

        mapIdentifiers(patientResponse, patient);
        mapPatientName(patientResponse, patient);
        mapGender(patientResponse, patient);
        mapBirthdate(patientResponse, patient);

        return patient;
    }

    private void mapIdentifiers(final MockPatient patientRes, final Patient patient) {
        if (StringUtils.isNotBlank(patientRes.getId())) {
        	patient.setIdElement(new IdType("Patient", patientRes.getId()));
        }
        if (patientRes.getMrn() != null
        		&& StringUtils.isNotBlank(patientRes.getMrn())) {
            patient.addIdentifier().setValue(patientRes.getMrn());                           
        }
    }

    private void mapPatientName(final MockPatient patientRes, final Patient patient) {

    	final HumanName outName = new HumanName();

    	if (StringUtils.isNotBlank(patientRes.getLast())) {
    		outName.setFamily(patientRes.getLast());
    	}

    	if (StringUtils.isNotBlank(patientRes.getFirst())) {
    		outName.addGiven(patientRes.getFirst());
    	}
    	patient.addName(outName);

    }

    private void mapGender(final MockPatient patientRes, final Patient patient) throws FHIRException {
        if (patientRes.getGender() != null && !patientRes.getGender().isEmpty()) {        	
			if(Enumerations.AdministrativeGender.fromCode(patientRes.getGender()) != null) {
				patient.setGender(Enumerations.AdministrativeGender.fromCode(patientRes.getGender()));	
			}
        } else {
            patient.setGender(Enumerations.AdministrativeGender.NULL);
        }
    }

    private void mapBirthdate(final MockPatient patientRes, final Patient patient) throws ParseException {
    	System.out.println(patientRes.getBirthdate());
        if (patientRes.getBirthdate() != null) {
        	DateType birthdate = new DateType(patientRes.getBirthdate().getYear(),
        		patientRes.getBirthdate().getMonth().getValue() - 1,
        		patientRes.getBirthdate().getDayOfMonth());

            patient.setBirthDateElement(birthdate);
        }
    }
}
