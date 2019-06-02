package de.eesit.service;

import javax.transaction.Transactional;
import javax.validation.Valid;

import de.eesit.annotations.ValidatedMethod;
import de.eesit.pojo.TestPojo;

public class TestServiceImpl implements TestService {

	@Override
	@ValidatedMethod
	@Transactional
	public void createOrUpdate(@Valid TestPojo name) {
	}

	
	
}
