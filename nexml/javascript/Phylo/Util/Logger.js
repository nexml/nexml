var singleton;

function Logger () {
    if ( singleton == null ) {
        this.level = 2;
        this.fatalLevel = 0;
        this.errorLevel = 1;
        this.warnLevel  = 2;
        this.infoLevel  = 3;
        this.debugLevel = 4;        
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
	if ( this.level >= this.debugLevel ) {
		this.broadcast( "DEBUG: " + msg );
	}
};

Logger.prototype.info = function(msg) {
	if ( this.level >= this.infoLevel ) {
		this.broadcast( "INFO: " + msg );
	}
};

Logger.prototype.warn = function(msg) {
	if ( this.level >= this.warnLevel ) {
		this.broadcast( "WARN: " + msg );
	}
};

Logger.prototype.error = function(msg) {
	if ( this.level >= this.errorLevel ) {
		this.broadcast( "ERROR: " + msg );
	}
};

Logger.prototype.fatal = function(msg) {
	if ( this.level >= this.fatalLevel ) {
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