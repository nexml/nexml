var singleton;

function Logger () {
    if ( singleton == null ) {
        this.level = 2;       
        this.listeners  = [ 
            function(msg) {
                self.status = msg;
            }	
        ];
        singleton = this;
	}
	return singleton;
};

Logger.prototype.debug = function(msg) {
	if ( this.level >= 4 ) {
		this.broadcast( "DEBUG: " + msg );
	}
};

Logger.prototype.info = function (msg) {
	if ( this.level >= 3 ) {
		this.broadcast( "INFO: " + msg );
	}
};

Logger.prototype.warn = function(msg) {
	if ( this.level >= 2 ) {
		this.broadcast( "WARN: " + msg );
	}
};

Logger.prototype.error = function(msg) {
	if ( this.level >= 1 ) {
		this.broadcast( "ERROR: " + msg );
	}
};

Logger.prototype.fatal = function(msg) {
	if ( this.level >= 0 ) {
		this.broadcast( "FATAL: " + msg );
	}
};

Logger.prototype.broadcast = function(msg) {
	var listeners = this.listeners;
	for ( var i = 0; i < listeners.length; i++ ) {
		listeners[i](msg);
	}
};

Logger.prototype.VERBOSE = function(level) {
	if ( level >= 0 && level <= 4 ) {
		this.level = level;
	}
};

Logger.prototype.set_listener = function(listener) {
	this.listeners.push(listener);
};