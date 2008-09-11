
/**
* Gets all elements that match supplied class name, analogous to DOM methods
* getElementsByTagName and getElementById
* @param theClass class name
* @return Array of elements
*/
function getElementsByClassName(theClass) {
    var allTags = document.getElementsByTagName('*');
    var tagsInClass = new Array();
    var classRegExp = new RegExp( '\\b' + theClass + '\\b' );    
    for ( var i = 0; i < allTags.length; i++ ) {
        var classes = allTags[i].className;
        if ( classRegExp.test(classes) ) {
            tagsInClass.push(allTags[i]);
        }
    }
    return tagsInClass;
}

/**
* Switches the 'display' style property: if theElement is not displayed, 
* display it - and vice versa.
* @param theElement an element node
*/
function toggleElementDisplay(theElement) {
    if ( theElement.style.display != 'none' ) {
        theElement.style.display = 'none';
    }
    else if ( theElement.style.display == 'none' ) {
        theElement.style.display = 'block';
    }
}

/**
* Switches the 'display' style property for all elements of theClass
* @param theClass a class name
* @see toggleElementDisplay
*/
function toggleClassDisplay(theClass) {
    var theElements = getElementsByClassName(theClass);
    for ( var i = 0; i < theElements.length; i++ ) {
        toggleElementDisplay(theElements[i]);
    }
}

function submitForm () {
    var action;
    var form = document.getElementById('validateForm');
    var inputs = form.getElementsByTagName('input');
    for ( var i = 0; i < inputs.length; i++ ) {
        if ( inputs[i].type == 'radio' ) {
            if ( inputs[i].checked ) {
                action = '/nexml/' + inputs[i].value;
                break;
            }
        }
    }
    if ( action == null ) {
        action = '/nexml/validator';
    }
    form.attributes.getNamedItem('action').value = action;
    form.submit();
}

window.name='nexml_site';
