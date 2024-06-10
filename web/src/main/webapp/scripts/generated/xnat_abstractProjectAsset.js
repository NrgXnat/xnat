/*
 * web: xnat_abstractProjectAsset.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2020, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*
 * GENERATED FILE
 * Created on Fri Feb 12 15:43:50 CST 2016
 *
 */

/**
 * @author XDAT
 *
 */

function xnat_abstractProjectAsset() {
    this.xsiType = "xnat:abstractProjectAsset";

    this.getSchemaElementName = function () {
        return "abstractProjectAsset";
    }

    this.getFullSchemaElementName = function () {
        return "xnat:abstractProjectAsset";
    }

    this.extension = dynamicJSLoad('xnat_genericData', 'generated/xnat_genericData.js');

    this.Subjects_subject = new Array();

    function getSubjects_subject() {
        return this.Subjects_subject;
    }

    this.getSubjects_subject = getSubjects_subject;


    function addSubjects_subject(v) {
        this.Subjects_subject.push(v);
    }

    this.addSubjects_subject = addSubjects_subject;

    this.Experiments_experiment = new Array();

    function getExperiments_experiment() {
        return this.Experiments_experiment;
    }

    this.getExperiments_experiment = getExperiments_experiment;


    function addExperiments_experiment(v) {
        this.Experiments_experiment.push(v);
    }

    this.addExperiments_experiment = addExperiments_experiment;

    this.getProperty = function (xmlPath) {
        if (xmlPath.startsWith(this.getFullSchemaElementName())) {
            xmlPath = xmlPath.substring(this.getFullSchemaElementName().length + 1);
        }
        if (xmlPath == "genericData") {
            return this.Genericdata;
        } else if (xmlPath.startsWith("genericData")) {
            xmlPath = xmlPath.substring(11);
            if (xmlPath == "") return this.Genericdata;
            if (xmlPath.startsWith("[")) {
                if (xmlPath.indexOf("/") > -1) {
                    var optionString = xmlPath.substring(0, xmlPath.indexOf("/"));
                    xmlPath = xmlPath.substring(xmlPath.indexOf("/") + 1);
                } else {
                    var optionString = xmlPath;
                    xmlPath = "";
                }

                var options = loadOptions(optionString);//omUtils.js
            } else {
                xmlPath = xmlPath.substring(1);
            }
            if (this.Genericdata != undefined) return this.Genericdata.getProperty(xmlPath);
            else return null;
        } else if (xmlPath == "subjects/subject") {
            return this.Subjects_subject;
        } else if (xmlPath.startsWith("subjects/subject")) {
            xmlPath = xmlPath.substring(14);
            if (xmlPath == "") return this.Subjects_subject;
            if (xmlPath.startsWith("[")) {
                if (xmlPath.indexOf("/") > -1) {
                    var optionString = xmlPath.substring(0, xmlPath.indexOf("/"));
                    xmlPath = xmlPath.substring(xmlPath.indexOf("/") + 1);
                } else {
                    var optionString = xmlPath;
                    xmlPath = "";
                }

                var options = loadOptions(optionString);//omUtils.js
            } else {
                xmlPath = xmlPath.substring(1);
            }
            var index = 0;
            if (options) {
                if (options.index) index = options.index;
            }

            var whereArray;
            if (options.where) {

                whereArray = new Array();

                for (var whereCount = 0; whereCount < this.Subjects_subject.length; whereCount++) {

                    var tempValue = this.Subjects_subject[whereCount].getProperty(options.where.field);

                    if (tempValue != null) if (tempValue.toString() == options.where.value.toString()) {

                        whereArray.push(this.Subjects_subject[whereCount]);

                    }

                }
            } else {

                whereArray = this.Subjects_subject;
            }

            var typeArray;
            if (options.xsiType) {

                typeArray = new Array();

                for (var typeCount = 0; typeCount < whereArray.length; typeCount++) {

                    if (whereArray[typeCount].getFullSchemaElementName() == options.xsiType) {

                        typeArray.push(whereArray[typeCount]);

                    }

                }
            } else {

                typeArray = whereArray;
            }
            if (typeArray.length > index) {
                return typeArray[index].getProperty(xmlPath);
            } else {
                return null;
            }
        } else if (xmlPath == "experiments/experiment") {
            return this.Experiments_experiment;
        } else if (xmlPath.startsWith("experiments/experiment")) {
            xmlPath = xmlPath.substring(14);
            if (xmlPath == "") return this.Experiments_experiment;
            if (xmlPath.startsWith("[")) {
                if (xmlPath.indexOf("/") > -1) {
                    var optionString = xmlPath.substring(0, xmlPath.indexOf("/"));
                    xmlPath = xmlPath.substring(xmlPath.indexOf("/") + 1);
                } else {
                    var optionString = xmlPath;
                    xmlPath = "";
                }

                var options = loadOptions(optionString);//omUtils.js
            } else {
                xmlPath = xmlPath.substring(1);
            }
            var index = 0;
            if (options) {
                if (options.index) index = options.index;
            }

            var whereArray;
            if (options.where) {

                whereArray = new Array();

                for (var whereCount = 0; whereCount < this.Experiments_experiment.length; whereCount++) {

                    var tempValue = this.Experiments_experiment[whereCount].getProperty(options.where.field);

                    if (tempValue != null) if (tempValue.toString() == options.where.value.toString()) {

                        whereArray.push(this.Experiments_experiment[whereCount]);

                    }

                }
            } else {

                whereArray = this.Experiments_experiment;
            }

            var typeArray;
            if (options.xsiType) {

                typeArray = new Array();

                for (var typeCount = 0; typeCount < whereArray.length; typeCount++) {

                    if (whereArray[typeCount].getFullSchemaElementName() == options.xsiType) {

                        typeArray.push(whereArray[typeCount]);

                    }

                }
            } else {

                typeArray = whereArray;
            }
            if (typeArray.length > index) {
                return typeArray[index].getProperty(xmlPath);
            } else {
                return null;
            }
        } else if (xmlPath == "meta") {
            return this.Meta;
        } else {
            return this.extension.getProperty(xmlPath);
        }
    }

    this.setProperty = function (xmlPath, value) {
        if (xmlPath.startsWith(this.getFullSchemaElementName())) {
            xmlPath = xmlPath.substring(this.getFullSchemaElementName().length + 1);
        }
        if (xmlPath == "genericData") {
            this.Genericdata = value;
        } else if (xmlPath.startsWith("genericData")) {
            xmlPath = xmlPath.substring(11);
            if (xmlPath == "") return this.Genericdata;
            if (xmlPath.startsWith("[")) {
                if (xmlPath.indexOf("/") > -1) {
                    var optionString = xmlPath.substring(0, xmlPath.indexOf("/"));
                    xmlPath = xmlPath.substring(xmlPath.indexOf("/") + 1);
                } else {
                    var optionString = xmlPath;
                    xmlPath = "";
                }

                var options = loadOptions(optionString);//omUtils.js
            } else {
                xmlPath = xmlPath.substring(1);
            }
            if (this.Genericdata != undefined) {
                this.Genericdata.setProperty(xmlPath, value);
            } else {
                if (options && options.xsiType) {
                    this.Genericdata = instanciateObject(options.xsiType);//omUtils.js
                } else {
                    this.Genericdata = instanciateObject("xnat:genericData");//omUtils.js
                }
                if (options && options.where) this.Genericdata.setProperty(options.where.field, options.where.value);
                this.Genericdata.setProperty(xmlPath, value);
            }
        } else if (xmlPath == "subjects/subject") {
            this.Subjects_subject = value;
        } else if (xmlPath.startsWith("subjects/subject")) {
            xmlPath = xmlPath.substring(14);
            if (xmlPath == "") return this.Subjects_subject;
            if (xmlPath.startsWith("[")) {
                if (xmlPath.indexOf("/") > -1) {
                    var optionString = xmlPath.substring(0, xmlPath.indexOf("/"));
                    xmlPath = xmlPath.substring(xmlPath.indexOf("/") + 1);
                } else {
                    var optionString = xmlPath;
                    xmlPath = "";
                }

                var options = loadOptions(optionString);//omUtils.js
            } else {
                xmlPath = xmlPath.substring(1);
            }
            var index = 0;
            if (options) {
                if (options.index) index = options.index;
            }

            var whereArray;
            if (options && options.where) {

                whereArray = new Array();

                for (var whereCount = 0; whereCount < this.Subjects_subject.length; whereCount++) {

                    var tempValue = this.Subjects_subject[whereCount].getProperty(options.where.field);

                    if (tempValue != null) if (tempValue.toString() == options.where.value.toString()) {

                        whereArray.push(this.Subjects_subject[whereCount]);

                    }

                }
            } else {

                whereArray = this.Subjects_subject;
            }

            var typeArray;
            if (options && options.xsiType) {

                typeArray = new Array();

                for (var typeCount = 0; typeCount < whereArray.length; typeCount++) {

                    if (whereArray[typeCount].getFullSchemaElementName() == options.xsiType) {

                        typeArray.push(whereArray[typeCount]);

                    }

                }
            } else {

                typeArray = whereArray;
            }
            if (typeArray.length > index) {
                typeArray[index].setProperty(xmlPath, value);
            } else {
                var newChild;
                if (options && options.xsiType) {
                    newChild = instanciateObject(options.xsiType);//omUtils.js
                } else {
                    newChild = instanciateObject("xnat:subjectData");//omUtils.js
                }
                this.addSubjects_subject(newChild);
                if (options && options.where) newChild.setProperty(options.where.field, options.where.value);
                newChild.setProperty(xmlPath, value);
            }
        } else if (xmlPath == "experiments/experiment") {
            this.Experiments_experiment = value;
        } else if (xmlPath.startsWith("experiments/experiment")) {
            xmlPath = xmlPath.substring(14);
            if (xmlPath == "") return this.Experiments_experiment;
            if (xmlPath.startsWith("[")) {
                if (xmlPath.indexOf("/") > -1) {
                    var optionString = xmlPath.substring(0, xmlPath.indexOf("/"));
                    xmlPath = xmlPath.substring(xmlPath.indexOf("/") + 1);
                } else {
                    var optionString = xmlPath;
                    xmlPath = "";
                }

                var options = loadOptions(optionString);//omUtils.js
            } else {
                xmlPath = xmlPath.substring(1);
            }
            var index = 0;
            if (options) {
                if (options.index) index = options.index;
            }

            var whereArray;
            if (options && options.where) {

                whereArray = new Array();

                for (var whereCount = 0; whereCount < this.Experiments_experiment.length; whereCount++) {

                    var tempValue = this.Experiments_experiment[whereCount].getProperty(options.where.field);

                    if (tempValue != null) if (tempValue.toString() == options.where.value.toString()) {

                        whereArray.push(this.Experiments_experiment[whereCount]);

                    }

                }
            } else {

                whereArray = this.Experiments_experiment;
            }

            var typeArray;
            if (options && options.xsiType) {

                typeArray = new Array();

                for (var typeCount = 0; typeCount < whereArray.length; typeCount++) {

                    if (whereArray[typeCount].getFullSchemaElementName() == options.xsiType) {

                        typeArray.push(whereArray[typeCount]);

                    }

                }
            } else {

                typeArray = whereArray;
            }
            if (typeArray.length > index) {
                typeArray[index].setProperty(xmlPath, value);
            } else {
                var newChild;
                if (options && options.xsiType) {
                    newChild = instanciateObject(options.xsiType);//omUtils.js
                } else {
                    newChild = instanciateObject("xnat:experimentData");//omUtils.js
                }
                this.addExperiments_experiment(newChild);
                if (options && options.where) newChild.setProperty(options.where.field, options.where.value);
                newChild.setProperty(xmlPath, value);
            }
        } else if (xmlPath == "meta") {
            this.Meta = value;
        } else {
            return this.extension.setProperty(xmlPath, value);
        }
    }

    /**
     * Sets the value for a field via the XMLPATH.
     * @param v Value to Set.
     */
    this.setReferenceField = function (xmlPath, v) {
        this.extension.setReferenceField(xmlPath, v);
    }

    /**
     * Gets the value for a field via the XMLPATH.
     * @param v Value to Set.
     */
    this.getReferenceFieldName = function (xmlPath) {
        return this.extension.getReferenceFieldName(xmlPath);
    }

    /**
     * Returns whether or not this is a reference field
     */
    this.getFieldType = function (xmlPath) {
        if (xmlPath == "subjects/subject") {
            return "field_multi_reference";
        } else if (xmlPath == "experiments/experiment") {
            return "field_multi_reference";
        } else {
            return this.extension.getFieldType(xmlPath);
        }
    }


    this.toXML = function (xmlTxt, preventComments) {
        xmlTxt += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        xmlTxt += "\n<xnat:abstractProjectAsset";
        xmlTxt += this.getXMLAtts();
        xmlTxt += " xmlns:arc=\"http://nrg.wustl.edu/arc\"";
        xmlTxt += " xmlns:cat=\"http://nrg.wustl.edu/catalog\"";
        xmlTxt += " xmlns:pipe=\"http://nrg.wustl.edu/pipe\"";
        xmlTxt += " xmlns:prov=\"http://www.nbirn.net/prov\"";
        xmlTxt += " xmlns:scr=\"http://nrg.wustl.edu/scr\"";
        xmlTxt += " xmlns:val=\"http://nrg.wustl.edu/val\"";
        xmlTxt += " xmlns:wrk=\"http://nrg.wustl.edu/workflow\"";
        xmlTxt += " xmlns:xdat=\"http://nrg.wustl.edu/security\"";
        xmlTxt += " xmlns:xnat=\"http://nrg.wustl.edu/xnat\"";
        xmlTxt += " xmlns:xnat_a=\"http://nrg.wustl.edu/xnat_assessments\"";
        xmlTxt += " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
        xmlTxt += ">";
        xmlTxt += this.getXMLBody(preventComments)
        xmlTxt += "\n</xnat:abstractProjectAsset>";
        return xmlTxt;
    }

    this.getXMLComments = function (preventComments) {
        var str = "";
        if ((preventComments == undefined || !preventComments) && this.hasXMLComments()) {
        }
        return str;
    }

    this.getXMLAtts = function () {
        return this.extension.getXMLAtts();
    }

    this.getXMLBody = function (preventComments) {
        var xmlTxt = this.getXMLComments(preventComments);
        xmlTxt += this.extension.getXMLBody(preventComments);

        var child0 = 0;
        var att0 = 0;
        child0 += this.Subjects_subject.length;
        if (child0 > 0 || att0 > 0) {
            xmlTxt += "\n<xnat:subjects";
            if (child0 == 0) {
                xmlTxt += "/>";
            } else {
                xmlTxt += ">";
                for (var Subjects_subjectCOUNT = 0; Subjects_subjectCOUNT < this.Subjects_subject.length; Subjects_subjectCOUNT++) {
                    xmlTxt += "\n<xnat:subject ";
                    xmlTxt += this.Subjects_subject[Subjects_subjectCOUNT].getXMLAtts();
                    if (this.Subjects_subject[Subjects_subjectCOUNT].xsiType != "xnat:subjectData") {
                        xmlTxt += " xsi:type=\"" + this.Subjects_subject[Subjects_subjectCOUNT].xsiType + "\"";
                    }
                    if (this.Subjects_subject[Subjects_subjectCOUNT].hasXMLBodyContent()) {
                        xmlTxt += ">";
                        xmlTxt += this.Subjects_subject[Subjects_subjectCOUNT].getXMLBody(preventComments);
                        xmlTxt += "</xnat:experiment>";
                    } else {
                        xmlTxt += "/>";
                    }
                }
                xmlTxt += "\n</xnat:experiments>";
            }
        }

        var child1 = 0;
        var att1 = 0;
        child1 += this.Experiments_experiment.length;
        if (child1 > 0 || att1 > 0) {
            xmlTxt += "\n<xnat:experiments";
            if (child1 == 0) {
                xmlTxt += "/>";
            } else {
                xmlTxt += ">";
                for (var Experiments_experimentCOUNT = 0; Experiments_experimentCOUNT < this.Experiments_experiment.length; Experiments_experimentCOUNT++) {
                    xmlTxt += "\n<xnat:experiment";
                    xmlTxt += this.Experiments_experiment[Experiments_experimentCOUNT].getXMLAtts();
                    if (this.Experiments_experiment[Experiments_experimentCOUNT].xsiType != "xnat:experimentData") {
                        xmlTxt += " xsi:type=\"" + this.Experiments_experiment[Experiments_experimentCOUNT].xsiType + "\"";
                    }
                    if (this.Experiments_experiment[Experiments_experimentCOUNT].hasXMLBodyContent()) {
                        xmlTxt += ">";
                        xmlTxt += this.Experiments_experiment[Experiments_experimentCOUNT].getXMLBody(preventComments);
                        xmlTxt += "</xnat:experiment>";
                    } else {
                        xmlTxt += "/>";
                    }
                }
                xmlTxt += "\n</xnat:experiments>";
            }
        }

        return xmlTxt;
    }

    this.hasXMLComments = function () {
    }

    this.hasXMLBodyContent = function () {
        return this.Subjects_subject != null && this.Subjects_subject.length > 0 && this.Experiments_experiment != null && this.Experiments_experiment.length > 0;
    }
}
