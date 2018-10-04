package org.nrg.xnat.services.mapping;

import java.util.ArrayList;
import java.util.List;

import org.nrg.xnat.restlet.util.SecureResourceParameterMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SecureResourceParameterMapperService{
	
	@Autowired(required=false)
	private List<SecureResourceParameterMapper> _mappers = new ArrayList<>();
	
	public List<SecureResourceParameterMapper> getParameterMappers() {
		return this._mappers;
	}
}