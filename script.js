function toggle(theClass) {
	var pre_elts = document.getElementsByTagName('pre');
	for ( var i = 0; i < pre_elts.length; i++ ) {
		var this_class = pre_elts[i].className;
		if ( this_class == theClass ) {
			if ( pre_elts[i].style.display != 'block' ) {
				pre_elts[i].style.display = 'block';
			}
			else if ( pre_elts[i].style.display == 'block' ) {
				pre_elts[i].style.display = 'none';
			}
		}
	}
}
