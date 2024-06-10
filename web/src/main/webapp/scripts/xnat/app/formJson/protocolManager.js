/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * @author: Mohana Ramaratnam (mohana@radiologics.com)
 * @since: 07-03-2021
 */
var XNAT = getObject(XNAT || {});

(function(factory) {
    if (typeof define === 'function' && define.amd) {
        define(factory);
    } else if (typeof exports === 'object') {
        module.exports = factory();
    } else {
        return factory();
    }
}(function() {

    var protocolUrl = XNAT.url.dataUrl('protocol');
    var protocolProjectsUrl = XNAT.url.restUrl('xapi/protocols/projects');
    var projectsUrl = XNAT.url.restUrl('xapi/role/projects?format=json');

	XNAT.customFormManager =
        getObject(XNAT.customFormManager || {});


    XNAT.customFormManager.protocolManager = protocolManager = getObject(XNAT.customFormManager.protocolManager || {});
    XNAT.customFormManager.protocolManager.protocols = [];

    protocolManager.init = function() {
        manager = new ProtocolManager();
        return manager;
    }

    function ProtocolManager() {
        XNAT.xhr.get({
            url: protocolUrl,
            dataType: 'json',
            async: false,
            success: function(data) {

                data.forEach(function(item) {
                    let protocol = {};
                    protocol.name = item.name;
                    protocol.protocolId = item.protocolId;
                    protocol.version = item.version;
                    let visits = [];
                    item['visitTypes'].forEach(function(visit) {
                        let visitObj = {};
                        visitObj.name = visit.name;
                        visitObj.id = visit.id;
                        visitObj.datatypesForVisit = visit.datatypesForVisit;
                        let expectedExperiments = [];
                        visit['expectedExperiments'].forEach(function(experiment) {
                            if (expectedExperiments[experiment.type] == undefined) {
                                let expObj = {};
                                expObj.type = experiment.type;
                                expObj.subtype = [];
                                expObj.subtype.push(experiment.subtype);
                                expectedExperiments.push(expObj);
                            } else {
                                let expObj = expectedExperiments[experiment.type];
                                expObj.push[experiment.subtype];
                            }
                        });
                        visitObj.expectedExperiments = expectedExperiments;
                        visits.push(visitObj);
                    });
                    protocol.visits = visits;
                    protocolManager.protocols.push(protocol);
                });
            }
        });
        console.log('Protocols fetched');
        return this;
    }


    protocolManager.filterProtocolsByDatatype = function(dataType) {
        let filteredByDatatype = [];
        //Dont duplicate the protocols is multiple visits within the protocol include the datatype
			protocolManager.protocols.forEach(function(proto) {
				var protoAlreadyIncluded = 0;
				proto.visits.forEach(function(visit) {
					visit.datatypesForVisit.forEach(function(dType) {
						if (dType === dataType) {
							if (protoAlreadyIncluded === 0) {
							    filteredByDatatype.push(proto);
							    protoAlreadyIncluded = 1;
						    }
						}
					});
				});
			});
        return filteredByDatatype;
    }

    protocolManager.getVisitsForProtocol = function(protocolObjects) {
        let visits = [];
        if (protocolObjects == undefined) {
            return visits;
        }
        protocolObjects.forEach(function(protoObj) {
            protocolManager.protocols.forEach(function(proto) {
                if (proto.protocolId === protoObj.value) {
                    proto.visits.forEach(function(visit) {
                        let protoVisit = {};
                        protoVisit.label = visit.name + "[" + proto.name + "]";
                        protoVisit.value = proto.name + ":" + visit.name;
                        visits.push(protoVisit);
                    });
                }
            });
        });
        return visits;
    }

    protocolManager.getSubTypesForVisit = function(datatype, visitObjects) {
        let subTypes = [];

        if (datatype == undefined || visitObjects == undefined) {
            return subTypes;
        }
        visitObjects.forEach(function(visitObj) {
            let valueParts = visitObj.value.split(":");
            let protocolName = valueParts[0];
            let visitName = valueParts[1];
            protocolManager.protocols.forEach(function(proto) {
                if (proto.name === protocolName) {
                    proto.visits.forEach(function(visit) {
                        if (visit.name == visitName) {
                            visit.expectedExperiments.forEach(function(expectedExperiment) {
                                if (expectedExperiment.type == datatype) {
                                    expectedExperiment.subtype.forEach(function(subtype) {
                                        if (subtype != null && subtype != "null") {
											let protoVisitSubType = {};
											protoVisitSubType.label = subtype + "[" + proto.name + "(" + visit.name + ")" + "]";
											protoVisitSubType.value = proto.name + ":" + visit.name + ":" + subtype;
											subTypes.push(protoVisitSubType);
										}
                                    });
                                }
                            });
                        }
                    });
                }
            });
        });
        return subTypes;
    }


    protocolManager.getProjects = function(protocolObjects) {
        let projects = [];
        if (protocolObjects == undefined || protocolObjects.length == 0) {
            //No protocols selected, just get list of all projects
            XNAT.xhr.get({
                url: projectsUrl,
                dataType: 'json',
                async: false,
                success: function(data) {
                    data.forEach(function(item) {
                        var o = {};
                        o['label'] = item.id;
                        o['value'] = item.id;
                        projects.push(o);
                    });
                }
            });
        } else {
            let jsonBody = JSON.stringify(protocolObjects);
            XNAT.xhr.post({
                url: protocolProjectsUrl,
                contentType: 'application/json',
                data: jsonBody,
                async: false,
                success: function(data) {
                    data.forEach(function(item) {
                        let projectsArray = item.projects;
                        projectsArray.forEach(function(project) {
                            var o = {};
                            o['label'] = project + "[" + item.name + "]";
                            o['value'] = item.name + ":" + project;
                            projects.push(o);
                        });
                    });
                }
            });
        }
        return projects;
    }


}));