
/*
 * web: validateUsername.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

function isNumeric(value) {
	return !!value.match(/^[0-9.]+$/);
}

// check to see if input is alphanumeric
function isAlphaNumeric(value) {
  return !!value.match(/^[a-zA-Z0-9]+$/);
}

function appendIcon(object, iconClass, message, style) {
	if (object.appendedIcon === undefined) {
		object.appendedIcon = document.createElement("i");
		object.appendedIcon.className = "fa " + iconClass;
		if (Object.keys(style).length) {
			for (const k in style) {
				object.appendedIcon.style[k] = style[k];
			}
		}
		object.appendedIcon.style.marginLeft = "5px";
		if (object.nextSibling == null) {
			object.parentNode.insertBefore(object.appendedIcon, object.nextSibling);
		} else {
			object.parentNode.appendChild(object.appendedIcon);
		}
	}

	if (message !== undefined) {
		object.appendedIcon.title = message;
	}
}

function appendImage(object, imageName) {
	if (object.appendedImage === undefined) {
		object.appendedImage = document.createElement("img");
		object.appendedImage.style.marginLeft = "5pt";
		if (object.nextSibling == null) {
			object.parentNode.insertBefore(object.appendedImage, object.nextSibling);
		} else {
			object.parentNode.appendChild(object.appendedImage);
		}
	}
	object.appendedImage.src = serverRoot + imageName;
}

function validateUsername(object, buttonId) {
	let valid = false;
	const value = object.value;
	if (value !== "") {
		if (isNumeric(value.charAt(0))) {
			xmodal.message('User Validation', 'Username cannot begin with a number.  Please modify.');
			object.focus();
		} else if (value.length > 40) {
			xmodal.message('User Validation', 'Username cannot exceed 40 characters');
			object.focus();
		} else if (isAlphaNumeric(value)) {
			valid = true;
		} else {
			xmodal.message('User Validation', 'Username cannot contain special characters.  Please modify.');
			object.focus();
		}
	}

	if (valid) {
		if (object.appendedImage !== undefined) {
			appendIcon(object, "fa-check", null, {color: 'green'});
		}
		if (buttonId !== undefined) {
			document.getElementById(buttonId).disabled = false;
		}
	} else {
		appendIcon(object, "fa-asterisk", "Required", {color: '#c66'});
		if (buttonId !== undefined) {
			document.getElementById(buttonId).disabled = true;
		}
	}

	return valid;
}
//# sourceURL=browsertools://scripts/user/validateUsername.js
