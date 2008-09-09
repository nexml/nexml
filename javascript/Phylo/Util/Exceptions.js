(function(){
function Exceptions (msg) {
	this.message = msg;
}
Phylo.Util.Exceptions = Exceptions;
copyPrototype(Phylo.Util.Exceptions,Error);

function Generic (msg) {
	this.Exceptions(msg);
	this.description = "No further details about this type of error are available.";
}
Phylo.Util.Exceptions.Generic = Generic;
copyPrototype(Phylo.Util.Exceptions.Generic,Phylo.Util.Exceptions);

function System (msg) {
	this.Generic(msg);
	this.description = "This kind of error happens when there is a system misconfiguration.";
}
Phylo.Util.Exceptions.System = System;
copyPrototype(Phylo.Util.Exceptions.System,Phylo.Util.Exceptions.Generic);

function BadFormat (msg) {
	this.System(msg);
	this.description = "This kind of error happens when a bad\nparse or unparse format was specified.";
}
Phylo.Util.Exceptions.BadFormat = BadFormat;
copyPrototype(Phylo.Util.Exceptions.BadFormat,Phylo.Util.Exceptions.System);

 function API (msg) {
	this.Generic(msg);
	this.description = "No more details about this type of error are available.";
}
Phylo.Util.Exceptions.API = API;
copyPrototype(Phylo.Util.Exceptions.API,Phylo.Util.Exceptions.Generic);

 function UnknownMethod (msg) {
	this.API(msg);
	this.description = "This kind of error happens when a non-existent method is called.";
}
Phylo.Util.Exceptions.UnknownMethod = UnknownMethod;
copyPrototype(Phylo.Util.Exceptions.UnknownMethod,Phylo.Util.Exceptions.API);
    
 function NotImplemented (msg) {
	this.API(msg);
	this.description = "This kind of error happens when a non-implemented\n(interface) method is called.";
}
Phylo.Util.Exceptions.NotImplemented = NotImplemented;
copyPrototype(Phylo.Util.Exceptions.NotImplemented,Phylo.Util.Exceptions.API);

 function Deprecated (msg) {
	this.API(msg);
	this.description = "This kind of error happens when a deprecated method is called.";
}
Phylo.Util.Exceptions.Deprecated = Deprecated;   
copyPrototype(Phylo.Util.Exceptions.Deprecated,Phylo.Util.Exceptions.API);   

 function BadArgs (msg) {
	this.Generic(msg);
	this.description = "This kind of error happens when bad or incomplete arguments\nare provided.";
}
Phylo.Util.Exceptions.BadArgs = BadArgs;
copyPrototype(Phylo.Util.Exceptions.BadArgs,Phylo.Util.Exceptions.Generic);

 function BadString (msg) {
	this.BadArgs(msg);
	this.description = "This kind of error happens when an unsafe string argument is\nprovided.";
}
Phylo.Util.Exceptions.BadString = BadString;
copyPrototype(Phylo.Util.Exceptions.BadString,Phylo.Util.Exceptions.BadArgs);

function OddHash (msg) {
	this.BadArgs(msg);
	this.description = "This kind of error happens when an uneven number\nof arguments (so no key/value pairs) was provided.";
}
Phylo.Util.Exceptions.OddHash = OddHash;
copyPrototype(Phylo.Util.Exceptions.OddHash,Phylo.Util.Exceptions.BadArgs);

 function ObjectMismatch (msg) {
	this.BadArgs(msg);
	this.description = "This kind of error happens when an invalid object\nargument is provided.";
}
Phylo.Util.Exceptions.ObjectMismatch = ObjectMismatch;
copyPrototype(Phylo.Util.Exceptions.ObjectMismatch,Phylo.Util.Exceptions.BadArgs);

 function InvalidData (msg) {
	this.BadString(msg);
	this.description = "This kind of error happens when invalid character data is\nprovided.";
}
Phylo.Util.Exceptions.InvalidData = InvalidData;
copyPrototypeMI(Phylo.Util.Exceptions.InvalidData,[Phylo.Util.Exceptions.BadString,Phylo.Util.Exceptions.BadFormat]);

 function OutOfBounds (msg) {
	this.BadArgs(msg);
    this.description = "This kind of error happens when an index is outside of its range.";
}
Phylo.Util.Exceptions.OutOfBounds = OutOfBounds;
copyPrototype(Phylo.Util.Exceptions.OutOfBounds,Phylo.Util.Exceptions.BadArgs);

 function BadNumber (msg) {
	this.Generic(msg);
	this.description = "This kind of error happens when an invalid numerical argument\nis provided.";
}
Phylo.Util.Exceptions.BadNumber = BadNumber;
copyPrototype(Phylo.Util.Exceptions.BadNumber,Phylo.Util.Exceptions.Generic);

 function FileError (msg) {
	this.System(msg);
	this.description = "This kind of error happens when a file can not be accessed.";
}
Phylo.Util.Exceptions.FileError = FileError;
copyPrototype(Phylo.Util.Exceptions.FileError,Phylo.Util.Exceptions.System);

 function ExtensionError (msg) {
	this.System(msg);
	this.description = "This kind of error happens when an extension module can not be\nloaded.";
}
Phylo.Util.Exceptions.ExtensionError = ExtensionError;
copyPrototypeMI(Phylo.Util.Exceptions.ExtensionError,[Phylo.Util.Exceptions.System,Phylo.Util.Exceptions.BadFormat]);
})();
