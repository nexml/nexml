Phylo.Util.Logger = {};
Phylo.Util.Logger.level = 2;
Phylo.Util.Logger.listeners = [ 
	function(msg) {
		self.status = msg;
	}	
];

Phylo.Util.Logger.debug = function(msg) {
	if ( this.level >= 4 ) {
		this.broadcast( "DEBUG: " + msg );
	}
};

Phylo.Util.Logger.info = function (msg) {
	if ( this.level >= 3 ) {
		this.broadcast( "INFO: " + msg );
	}
};

Phylo.Util.Logger.warn = function(msg) {
	if ( this.level >= 2 ) {
		this.broadcast( "WARN: " + msg );
	}
};

Phylo.Util.Logger.error = function(msg) {
	if ( this.level >= 1 ) {
		this.broadcast( "ERROR: " + msg );
	}
};

Phylo.Util.Logger.fatal = function(msg) {
	if ( this.level >= 0 ) {
		this.broadcast( "FATAL: " + msg );
	}
};

Phylo.Util.Logger.broadcast = function(msg) {
	var listeners = this.listeners;
	for ( var i = 0; i < listeners.length; i++ ) {
		listeners[i](msg);
	}
};

Phylo.Util.Logger.VERBOSE = function(level) {
	if ( level >= 0 && level <= 4 ) {
		this.level = level;
	}
};

Phylo.Util.Logger.set_listener = function(listener) {
	this.listeners.push(listener);
};