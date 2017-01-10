

/*
 * web: accessibility.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
