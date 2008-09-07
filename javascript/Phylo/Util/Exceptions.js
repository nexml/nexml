function copyPrototype(descendant, parent) {
    var sConstructor = parent.toString();
    var aMatch = sConstructor.match( /\s*function (.*)\(/ );
    if ( aMatch != null ) { descendant.prototype[aMatch[1]] = parent; }
    for (var m in parent.prototype) {
        descendant.prototype[m] = parent.prototype[m];
    }
};

function Exceptions(msg) {
	this.message = msg;
}

copyPrototype(Exceptions,Error);

function Generic(msg) {
	this.Exceptions(msg);
	this.description = "No further details about this type of error are available.";
}

copyPrototype(Generic,Exceptions);

function API(msg) {
	this.Generic(msg);
	this.description = "No more details about this type of error are available.";
}

copyPrototype(API,Generic);

function UnknownMethod(msg) {
	this.API(msg);
	this.description = "This kind of error happens when a non-existent method is called.";
}

copyPrototype(UnknownMethod,API);
    
function NotImplemented(msg) {
	this.API(msg);
	this.description = "This kind of error happens when a non-implemented\n(interface) method is called.";
}

copyPrototype(NotImplemented,API);

function Deprecated(msg) {
	this.API(msg);
	this.description = "This kind of error happens when a deprecated method is called.";
}
   
copyPrototype(Deprecated,API);   

function BadArgs(msg) {
	this.Generic(msg);
	this.description = "This kind of error happens when bad or incomplete arguments\nare provided.";
}

copyPrototype(BadArgs,Generic);

function BadString(msg) {
	this.BadArgs(msg);
	this.description = "This kind of error happens when an unsafe string argument is\nprovided.";
}

copyPrototype(BadString,BadArgs);

function OddHash(msg) {
	this.BadArgs(msg);
	this.description = "This kind of error happens when an uneven number\nof arguments (so no key/value pairs) was provided.";
}

copyPrototype(OddHash,BadArgs);

function ObjectMismatch(msg) {
	this.BadArgs(msg);
	this.description = "This kind of error happens when an invalid object\nargument is provided.";
}

copyPrototype(ObjectMismatch,BadArgs);

function InvalidData(msg) {
	this.BadString(msg);
	this.description = "This kind of error happens when invalid character data is\nprovided.";
}

copyPrototype(InvalidData,BadString);
copyPrototype(InvalidData,BadFormat);

function OutOfBounds(msg) {
	this.BadArgs(msg);
    this.description = "This kind of error happens when an index is outside of its range.";
}

copyPrototype(OutOfBounds,BadArgs);

function BadNumber(msg) {
	this.Generic(msg);
	this.description = "This kind of error happens when an invalid numerical argument\nis provided.";
}

copyPrototype(BadNumber,Generic);

function System(msg) {
	this.Generic(msg);
	this.description = "This kind of error happens when there is a system misconfiguration.";
}

copyPrototype(System,Generic);

function FileError(msg) {
	this.System(msg);
	this.description = "This kind of error happens when a file can not be accessed.";
}

copyPrototype(FileError,System);

function ExtensionError(msg) {
	this.System(msg);
	this.description = "This kind of error happens when an extension module can not be\nloaded.";
}

copyPrototype(ExtensionError,System);
copyPrototype(ExtensionError,BadFormat);

function BadFormat(msg) {
	this.System(msg);
	this.description = "This kind of error happens when a bad\nparse or unparse format was specified.";
}

copyPrototype(BadFormat,System);