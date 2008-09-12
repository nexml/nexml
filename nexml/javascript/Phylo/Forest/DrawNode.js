Phylo.Forest.DrawNode = function (args) {
	if (args==null) args = {};
	if ( args["node"] != null ) {
		this.Node(args["node"]);
	}
	else {
		this.Node(args);
	}
	return this;
	
}
copyPrototype(Phylo.Forest.DrawNode,Phylo.Forest.Node);
var proto = Phylo.Forest.DrawNode.prototype;

proto.set_x = function (x) {
    this.x = x;
    return this;
};

proto.set_y = function (y) {
    this.y = y;
    return this;
};

proto.set_radius = function (radius) {
    this.radius = radius;
    return this;
};

proto.set_node_colour = function (node_colour) {
    this.node_colour = node_colour;
    return this;
};

proto.set_node_shape = function (node_shape) {
    this.node_shape = node_shape;
    return this;
};

proto.set_node_image = function (node_image) {
    this.node_image = node_image;
    return this;
};

proto.set_branch_color = function (branch_color) {
    this.branch_color = branch_color;
    return this;
};

proto.set_branch_shape = function (branch_shape) {
    this.branch_shape = branch_shape;
    return this;
};

proto.set_branch_width = function (branch_width) {
    this.branch_width = branch_width;
    return this;
};

proto.set_branch_style = function (branch_style) {
    this.branch_style = branch_style;
    return this;
};

proto.set_font_face = function (font_face) {
    this.font_face = font_face;
    return this;
};

proto.set_font_size = function (font_size) {
    this.font_size = font_size;
    return this;
};

proto.set_font_style = function (font_style) {
    this.font_style = font_style;
    return this;
};

proto.set_url = function (url) {
    this.url = url;
    return this;
};

proto.set_text_horiz_offset = function (text_horiz_offset) {
    this.text_horiz_offset = text_horiz_offset;
    return this;
};

proto.set_text_vert_offset = function (text_vert_offset) {
    this.text_vert_offset = text_vert_offset;
    return this;
};

proto.get_x = function () { return this.x };

proto.get_y = function () { return this.y };

proto.get_radius = function () { return this.radius };

proto.get_node_colour = function () { return this.node_colour };

proto.get_node_shape = function () { return this.node_shape };

proto.get_node_image = function () { return this.node_image };

proto.get_branch_color = function () { return this.branch_color };

proto.get_branch_shape = function () { return this.branch_shape };

proto.get_branch_width = function () { return this.branch_width };

proto.get_branch_style = function () { return this.branch_style };

proto.get_font_face = function () { return this.font_face };

proto.get_font_size = function () { return this.font_size };

proto.get_font_style = function () { return this.font_style };

proto.get_url = function () { return this.url };

proto.get_text_horiz_offset = function () { return this.text_horiz_offset };

proto.get_text_vert_offset = function () { return this.text_vert_offset };