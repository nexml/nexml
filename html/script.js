/*
Function to switch invisible elements to visible and vice versa when a link is clicked
*/
function toggle_display(id) {
	var theDiv = document.getElementById(id);
	var theLi = document.getElementById( id + 'li' );
	if ( theDiv != null ) {
		if ( theDiv.style.display == 'none' ) {
			theDiv.style.display = 'block';
			theLi.style.listStyleImage = 'url(\'triangle_down.gif\')';
			theLi.style.borderBottom = '1px solid black';
			theLi.style.borderTop = '1px solid black';
		}
		else if ( theDiv.style.display != 'none' ) {
			theDiv.style.display = 'none';
			theLi.style.listStyleImage = 'url(\'triangle.gif\')';
			theLi.style.borderBottom = '1px solid white';
			theLi.style.borderTop = '1px solid white'; 
		}
	}
}

/*
Function to process yahoo pipes JSON feeds.
*/
var itemCounter = 0;
function process_feed(feed) {
	var theDiv = document.getElementById('feeds');
	var theContent = '<h2>' + feed.value.title + '<\/h2><p>' + feed.value.description + '<\/p>';                             
	theContent += '<ul>';
	for ( var i = 0; i < feed.value.items.length; i++ ) {
		var item = feed.value.items[i];
		var id = 'item' + itemCounter++;
		theContent += '<li id="' + id + 'li">';
		theContent += '<a href="javascript:toggle_display(\'' + id + '\')">' + item.title + '<\/a>';
		theContent += ' <small>by ' + item.author + ' on ' + item.pubDate + '<\/small>';
		theContent += '<div id="' + id + '" style="display:none">' + item.description;
		theContent += '<br/><small><a href="' + item.link + '">more...<\/a><\/small><\/div>';
		theContent += '<\/li>\n';
	}
	theContent += '<\/ul>';
	theDiv.innerHTML += theContent;
}

var W3CDOM = (document.createElement && document.getElementsByTagName);

function initFileUploads() {
	if (!W3CDOM) return;
	var fakeFileUpload = document.createElement('div');
	var fakeFileInput = document.createElement('input');
	fakeFileInput.className = 'fakeinput';
	fakeFileUpload.className = 'fakefile';	
	fakeFileUpload.appendChild(fakeFileInput);
	var image = document.createElement('img');
	image.className = 'fakeimage';
	image.src='img/browse.png';
	fakeFileUpload.appendChild(image);
	var x = document.getElementsByTagName('input');
	for (var i=0;i<x.length;i++) {
		if (x[i].type != 'file') continue;
		if (x[i].parentNode.className != 'fileinputs') continue;
		x[i].className = 'file hidden';
		var clone = fakeFileUpload.cloneNode(true);
		x[i].parentNode.appendChild(clone);
		x[i].relatedElement = clone.getElementsByTagName('input')[0];
		x[i].onchange = x[i].onmouseout = function () {
			this.relatedElement.value = this.value;
		}
	}
	var theSubmit = document.getElementById('validateSubmit');
	theSubmit.type = 'image';
	theSubmit.src = 'img/validate.png';
}