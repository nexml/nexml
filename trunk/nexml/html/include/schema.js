
/**
* Called when document loads, hides all 'level2' elements.
*/
function collapse () {
    var theElements = getElementsByClassName('level2');
    var theHeadings = getElementsByClassName('level1');
    for ( var i = 0; i < theElements.length; i++ ) {
        toggleElementDisplay(theElements[i]);
    }
    for ( var i = 0; i < theHeadings.length; i++ ) {
        var theLink = theHeadings[i].getElementsByTagName('a')[0];
        var theId = theHeadings[i].id;
        theLink.href = 'javascript:toggleClassDisplay(\'' + theId + '\')';
    }
}
