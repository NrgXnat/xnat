

/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/project/accessibility.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:12 AM
 */
$(document).ready(function() {
	
	makeTheBalloonMatchTheCheckedButton();
});


function checkAccessibilityRadioButton(buttonDiv, buttonId) {
	
	$("#" + buttonId).prop('checked', true);

	$("#balloon").html(buttonDiv.title);
}


function makeTheBalloonMatchTheCheckedButton() {
	
	var checkedButton = $("input[name='accessibility']:checked").val();
	
	$("#" + checkedButton + "_access_div").click();
}
