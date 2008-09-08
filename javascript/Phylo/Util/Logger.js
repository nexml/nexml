var Logger = {};
Logger.level = 2;
Logger.listeners = [ 
	function(msg) {
		self.status = msg;
	}	
];

Logger.debug = function(msg) {
	if ( this.level >= 4 ) {
		this.broadcast( "DEBUG: " + msg );
	}
};

Logger.info = function (msg) {
	if ( this.level >= 3 ) {
		this.broadcast( "INFO: " + msg );
	}
};

Logger.warn = function(msg) {
	if ( this.level >= 2 ) {
		this.broadcast( "WARN: " + msg );
	}
};

Logger.error = function(msg) {
	if ( this.level >= 1 ) {
		this.broadcast( "ERROR: " + msg );
	}
};

Logger.fatal = function(msg) {
	if ( this.level >= 0 ) {
		this.broadcast( "FATAL: " + msg );
	}
};

Logger.broadcast = function(msg) {
	var listeners = this.listeners;
	for ( var i = 0; i < listeners.length; i++ ) {
		listeners[i](msg);
	}
};

Logger.VERBOSE = function(level) {
	if ( level >= 0 && level <= 4 ) {
		this.level = level;
	}
};

Logger.set_listener = function(listener) {
	this.listeners.push(listener);
};